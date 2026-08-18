package com.taejun.shop.domain.payment.scheduler;

import com.taejun.shop.domain.order.enums.OrderStatus;
import com.taejun.shop.domain.order.repository.OrderRepository;
import com.taejun.shop.domain.payment.service.ExpiredOrderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredOrderScheduler {

    private static final int BATCH_SIZE = 100;

    private final OrderRepository orderRepository;
    private final ExpiredOrderProcessor expiredOrderProcessor;

    @Scheduled(fixedDelay = 30_000)
    public void restoreExpiredOrders() {
        List<Long> orderIds = orderRepository.findExpiredOrderIds(
                OrderStatus.PAYMENT_PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE)
        );

        for (Long orderId : orderIds) {
            try {
                expiredOrderProcessor.expire(orderId);
            } catch (RuntimeException exception) {
                log.error(
                        "주문 만료 처리 실패. orderId={}",
                        orderId,
                        exception
                );
            }
        }

    }

}
