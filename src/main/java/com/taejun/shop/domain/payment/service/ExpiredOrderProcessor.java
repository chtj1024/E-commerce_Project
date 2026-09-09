package com.taejun.shop.domain.payment.service;

import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.entity.OrderItem;
import com.taejun.shop.domain.order.enums.OrderStatus;
import com.taejun.shop.domain.order.repository.OrderRepository;
import com.taejun.shop.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ExpiredOrderProcessor {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void expire(Long orderId) {
        // 동시성 처리 : 결제 이탈 주문 선점
        int updatedRows = orderRepository.expireIfPending(
                orderId,
                OrderStatus.PAYMENT_PENDING,
                OrderStatus.EXPIRED,
                Instant.now()
        );

        if (updatedRows == 0) {
            return;
        }

        // 재고 롤백/복구 로직 - 결제 이탈/만료
        CustomerOrder order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "주문을 찾을 수 없습니다."
                ));

        for (OrderItem item : order.getOrderItems()) {
            int restoredRows = productRepository.restoreStock(
                    item.getProductId(),
                    item.getQuantity()
            );

            if (restoredRows != 1) {
                throw new IllegalArgumentException(
                        "만료 주문 재고 복구에 실패했습니다. productId="
                                + item.getProductId()
                );
            }
        }
    }
}
