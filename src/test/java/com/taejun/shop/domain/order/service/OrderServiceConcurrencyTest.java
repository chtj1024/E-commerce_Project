package com.taejun.shop.domain.order.service;

import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.order.dto.OrderCreateRequest;
import com.taejun.shop.domain.order.dto.OrderItemRequest;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.global.exception.CustomException;
import com.taejun.shop.global.exception.ErrorCode;
import com.taejun.shop.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderServiceConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    private OrderService orderService;

    @Test
    void 재고보다_많은_동시주문이_들어와도_초과판매되지_않는다() throws Exception {

        Member member = memberRepository.saveAndFlush(
                new Member(
                        "concurrency@test.com",
                        "encoded-password",
                        "동시성회원"
                )
        );

        Product product = productRepository.saveAndFlush(
                new Product(
                        "한정상품",
                        "TEST",
                        10_000L,
                        10,
                        "동시성 테스트 상품",
                        null
                )
        );

        int requestCount = 20;

        // ExecutorService : 여러 작업을 스레드에게 나눠서 실행해주는 도구
        // 현재 newFiexedThreadPool : 최대 20개의 스레드가 동시에 작업할 수 있는 풀을 만들겠다
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);

        // CountDownLatch : countDown()으로 호출해서 카운트가 0이될 때 까지 기다릴 수 있는 Latch
        CountDownLatch ready = new CountDownLatch(requestCount);

        CountDownLatch start = new CountDownLatch(1);

        // Future -> 현재 실행 중이거나 앞으로 완료될 작업의 '최종 결과'를 나중에 받을 수 있게 해주는 객체
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < requestCount; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();

                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalArgumentException(
                                "동시성 테스트 시작 대기시간을 초과했습니다."
                        );
                    }

                    try {
                        orderService.create(
                                member.getEmail(),
                                new OrderCreateRequest(List.of(
                                        new OrderItemRequest(
                                                product.getId(),
                                                1
                                        )
                                ))
                        );

                        return true;
                    } catch (CustomException exception) {
                        if (exception.getErrorCode() == ErrorCode.INSUFFICIENT_STOCK) {
                            return false;
                        }

                        throw exception;
                    }
                }));
            }

            assertThat(ready.await(10, TimeUnit.SECONDS))
                    .isTrue();

            start.countDown();

            int successCount = 0;

            for (Future<Boolean> future : futures) {
                if (future.get(30, TimeUnit.SECONDS)) {
                    successCount++;
                }
            }

            Product result =
                    productRepository.findById(product.getId()).orElseThrow();

            assertThat(successCount).isEqualTo(10);
            assertThat(orderRepository.count()).isEqualTo(10);
            assertThat(result.getStockQuantity()).isZero();
        } finally {
            executor.shutdownNow();
        }
    }
}
