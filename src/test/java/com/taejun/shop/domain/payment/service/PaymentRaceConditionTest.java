package com.taejun.shop.domain.payment.service;

import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.enums.OrderStatus;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentRaceConditionTest extends IntegrationTestSupport {

    @Autowired
    private PaymentResultService paymentResultService;

    @Autowired
    private ExpiredOrderProcessor expiredOrderProcessor;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void 결제성공과_만료처리가_동시에_실행되어도_상태와_재고가_일치한다() throws Exception{

        TestFixture fixture = Objects.requireNonNull(
                transactionTemplate.execute(status -> {
                    Member member = memberRepository.saveAndFlush(
                            new Member(
                                    "race@test.com",
                                    "encoded-password",
                                    "경합회원"
                            )
                    );

                    Product product = productRepository.saveAndFlush(
                            new Product(
                                    "경합상품",
                                    "TEST",
                                    10_000L,
                                    10,
                                    "경합 테스트 상품",
                                    null
                            )
                    );

                    CustomerOrder order = new CustomerOrder(
                            member,
                            LocalDateTime.now().minusMinutes(1)
                    );

                    order.addItem(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            3
                    );

                    orderRepository.saveAndFlush(order);

                    assertThat(
                            productRepository.decreaseStock(product.getId(), 3)
                    ).isEqualTo(1);


                    return new TestFixture(
                            order.getId(),
                            product.getId()
                    );
                })
        );


        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<?> paymentFuture = executor.submit(() -> {
                ready.countDown();
                await(start);

                try {
                    paymentResultService.completePayment(fixture.orderId());
                } catch (RuntimeException ignored) {
                    // 만료 처리가 먼저 성공할 수 있다.
                }
            });

            Future<?> expirationFuture = executor.submit(() -> {
                ready.countDown();
                await(start);
                expiredOrderProcessor.expire(fixture.orderId());
            });

            assertThat(ready.await(10, TimeUnit.SECONDS))
                    .isTrue();

            start.countDown();

            paymentFuture.get(30, TimeUnit.SECONDS);
            expirationFuture.get(30, TimeUnit.SECONDS);

            CustomerOrder result =
                    orderRepository.findById(fixture.orderId()).orElseThrow();

            int finalStock = productRepository.findById(fixture.productId())
                    .orElseThrow()
                    .getStockQuantity();

            // 동시성 처리 검증 : 조건부 update
            // 결제 성공이면 재고 차감, 만료 처리면 재고 복구
            if (result.getStatus() == OrderStatus.PAID) {
                assertThat(finalStock).isEqualTo(7);
            } else {
                assertThat(result.getStatus())
                        .isEqualTo(OrderStatus.EXPIRED);

                assertThat(finalStock).isEqualTo(10);
            }
        } finally {
            executor.shutdownNow();
        }

    }

    // 테스트에 트랜잭션이 없어도, 있어도 안돼고 일부만 지정되야 하므로 레코드 생성
    private record TestFixture(
            Long orderId,
            Long productId
    ) {

    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalArgumentException(
                        "동시성 테스트 시작 대기시간을 초과했습니다."
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(exception);
        }
    }

}
