package com.taejun.shop.domain.payment.service;

import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.enums.OrderStatus;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpiredOrderProcessorTest extends IntegrationTestSupport {

    @Autowired
    private ExpiredOrderProcessor expiredOrderProcessor;

    @Test
    @Transactional
    void 결제대기시간이_만료되면_재고를_복구한다() {
        Member member = saveMember();
        Product product = saveProduct(10);

        CustomerOrder order = createExpiredOrder(
                member,
                product,
                3
        );

        assertThat(findStock(product.getId()))
                .isEqualTo(7);

        expiredOrderProcessor.expire(order.getId());

        CustomerOrder expiredOrder =
                orderRepository.findById(order.getId()).orElseThrow();


        // 재고 롤백/복구 로직 검증 - 결제 이탈/만료
        assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        assertThat(findStock(product.getId())).isEqualTo(10);

    }

    @Test
    @Transactional
    void 만료처리를_중복실행해도_재고는_한번만_복구한다() {
        Member member = saveMember();
        Product product = saveProduct(10);

        CustomerOrder order = createExpiredOrder(
                member,
                product,
                3
        );

        expiredOrderProcessor.expire(order.getId());
        expiredOrderProcessor.expire(order.getId());
        expiredOrderProcessor.expire(order.getId());

        assertThat(
                orderRepository.findById(order.getId())
                        .orElseThrow()
                        .getStatus()
        ).isEqualTo(OrderStatus.EXPIRED);

        // 주문 상태 선점에 성공한 첫 처리만 재고를 복구
        assertThat(findStock(product.getId()))
                .isEqualTo(10);

    }

    @Test
    @Transactional
    void 아직_만료되지_않은_주문은_처리하지_않는다() {
        Member member = saveMember();
        Product product = saveProduct(10);

        int decreaseRows =
                productRepository.decreaseStock(product.getId(), 3);

        assertThat(decreaseRows).isEqualTo(1);

        CustomerOrder order = new CustomerOrder(
                member,
                LocalDateTime.now().plusMinutes(15)
        );

        order.addItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                3
        );

        orderRepository.saveAndFlush(order);

        expiredOrderProcessor.expire(order.getId());

        CustomerOrder result =
                orderRepository.findById(order.getId()).orElseThrow();

        assertThat(result.getStatus())
                .isEqualTo(OrderStatus.PAYMENT_PENDING);

        assertThat(findStock(product.getId()))
                .isEqualTo(7);

    }

    private CustomerOrder createExpiredOrder(
            Member member,
            Product product,
            int quantity
    ) {
        int decreasedRows =
                productRepository.decreaseStock(product.getId(), quantity);

        assertThat(decreasedRows).isEqualTo(1);

        CustomerOrder order = new CustomerOrder(
                member,
                LocalDateTime.now().minusMinutes(1)
        );

        order.addItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity
        );

        return orderRepository.saveAndFlush(order);
    }

    private Member saveMember() {
        return memberRepository.saveAndFlush(
                new Member(
                        "expired@test.com",
                        "encoded-password",
                        "만료회원"
                )
        );
    }

    private Product saveProduct(int stock) {
        return productRepository.saveAndFlush(
                new Product(
                        "만료상품",
                        "TEST",
                        10_000L,
                        stock,
                        "만료 테스트 상품",
                        null
                )
        );
    }

    private int findStock(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow()
                .getStockQuantity();
    }
}
