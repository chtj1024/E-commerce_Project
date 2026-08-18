package com.taejun.shop.domain.order.service;

import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.member.repository.MemberRepository;
import com.taejun.shop.domain.order.dto.OrderCreateRequest;
import com.taejun.shop.domain.order.dto.OrderItemRequest;
import com.taejun.shop.domain.order.dto.OrderResponse;
import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.repository.OrderRepository;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final long PAYMENT_TIMEOUT_MINUTES = 15L;

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse create(
            String memberEmail,
            OrderCreateRequest request
    ) {
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "인증된 회원을 찾을 수 없습니다."
                ));

        // 동일 상품 요청에 여러번 포함된 경우 수량을 합산
        // 장바구니 로직을 완벽하게 짜 놨어도 혹시모르니 중복 문제를 서비스 경계에서 최종 방어하는 코드
        Map<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();

        for (OrderItemRequest item : request.items()) {
            quantitiesByProductId.merge(
                    item.productId(),
                    item.quantity(),
                    Math::addExact
            );
        }

        CustomerOrder order = new CustomerOrder(
                member,
                LocalDateTime.now().plusMinutes(PAYMENT_TIMEOUT_MINUTES)
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

        return OrderResponse.from(savedOrder);
    }

    private void reserveStockAndAddItem(
            CustomerOrder order,
            Long productId,
            int quantity
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "상품을 찾을 수 없습니다: " + productId
                ));

        String productName = product.getName();
        Long unitPrice = product.getPrice();

        // 동시성 처리 : 조건부 update.
        int updatedRows = productRepository.decreaseStock(
                productId,
                quantity
        );

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "재고가 부족하거나 판매할 수 없는 상품입니다: " + productId
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
