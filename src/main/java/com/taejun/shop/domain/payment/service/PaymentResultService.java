package com.taejun.shop.domain.payment.service;

import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.entity.OrderItem;
import com.taejun.shop.domain.order.enums.OrderStatus;
import com.taejun.shop.domain.order.repository.OrderRepository;
import com.taejun.shop.domain.order.service.OrderService;
import com.taejun.shop.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentResultService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void completePayment(Long orderId) {
        // 동시성 처리 : 조건부 update
        // PAID이지만 결제 완료 됐다고 PG사에서 한 번 더 요청할 수 있으므로 방지하는 코드
        int updatedRows = orderRepository.transitionStatus(
                orderId,
                OrderStatus.PAYMENT_PENDING,
                OrderStatus.PAID
        );

        if (updatedRows == 0) {
            CustomerOrder order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "주문을 찾을 수 없습니다."
                    ));

            if (order.getStatus() == OrderStatus.PAID) {
                return;
            }

            // 결제가 승인됐을 수 있으므로 PG 결제 취소/환불 요청
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 종료된 주문입니다. 승인된 결제가 있다면 취소해야 합니다."
            );
        }
    }

    @Transactional
    public void failPayment(Long orderId) {
        // 동시성 처리 : 조건부 update
        int updatedRows = orderRepository.transitionStatus(
                orderId,
                OrderStatus.PAYMENT_PENDING,
                OrderStatus.PAYMENT_FAILED
        );

        if (updatedRows == 0) {
            return;
        }

        // 재고 롤백/복구 로직 : 결제 실패

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
                        "재고 복구에 실패했습니다. productId=" + item.getProductId()
                );
            }


        }

    }

}
