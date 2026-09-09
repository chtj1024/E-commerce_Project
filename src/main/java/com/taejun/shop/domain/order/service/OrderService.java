package com.taejun.shop.domain.order.service;

import com.taejun.shop.domain.cart.repository.CartItemRepository;
import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.member.repository.MemberRepository;
import com.taejun.shop.domain.order.dto.OrderCreateRequest;
import com.taejun.shop.domain.order.dto.OrderItemRequest;
import com.taejun.shop.domain.order.dto.OrderResponse;
import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.repository.OrderRepository;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.repository.ProductRepository;
import com.taejun.shop.global.exception.CustomException;
import com.taejun.shop.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final long PAYMENT_TIMEOUT_MINUTES = 15L;

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public OrderResponse create(
            String memberEmail,
            OrderCreateRequest request
    ) {
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.MEMBER_NOT_FOUND)
                );

        // 동일 상품 요청에 여러번 포함된 경우 수량을 합산
        // 장바구니 로직을 완벽하게 짜 놨어도 혹시모르니 중복 문제를 서비스 경계에서 최종 방어하는 코드
        Map<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();

        try {
            for (OrderItemRequest item : request.items()) {
                quantitiesByProductId.merge(
                        item.productId(),
                        item.quantity(),
                        Math::addExact
                );
            }
        } catch (ArithmeticException exception) {
            throw new CustomException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "상품 주문 수량이 허용 범위를 초과했습니다."
            );
        }

        CustomerOrder order = new CustomerOrder(
                member,
                Instant.now().plus(
                        PAYMENT_TIMEOUT_MINUTES,
                        ChronoUnit.MINUTES
                )
        );

        // 동시성 처리 : 상품 ID 순서 고정
        // 재고 차감 순서를 항상 일정하게 만들어 DB 데드락 가능성을 줄이는 코드
        quantitiesByProductId.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> reserveStockAndAddItem(
                        order,
                        entry.getKey(),
                        entry.getValue()
                ));

        CustomerOrder savedOrder = orderRepository.save(order);

        // 주문한 상품만 현재 회원의 장바구니에서 삭제
        cartItemRepository.deletePurchasedItems(
                member.getId(),
                quantitiesByProductId.keySet()
        );

        return OrderResponse.from(savedOrder);
    }

    private void reserveStockAndAddItem(
            CustomerOrder order,
            Long productId,
            int quantity
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PRODUCT_NOT_FOUND,
                                "상품을 찾을 수 없습니다. productId=" + productId
                        )
                );

        String productName = product.getName();
        Long unitPrice = product.getPrice();

        // 동시성 처리 : 조건부 update.
        int updatedRows = productRepository.decreaseStock(
                productId,
                quantity
        );

        if (updatedRows == 0) {
            throw new CustomException(
                    ErrorCode.INSUFFICIENT_STOCK,
                    "재고가 부족하거나 판매할 수 없는 상품입니다."
            );
        }

        order.addItem(
                productId,
                productName,
                unitPrice,
                quantity
        );
    }
}
