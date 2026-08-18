package com.taejun.shop.domain.payment.service;

import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.order.dto.OrderCreateRequest;
import com.taejun.shop.domain.order.dto.OrderItemRequest;
import com.taejun.shop.domain.order.dto.OrderResponse;
import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.enums.OrderStatus;
import com.taejun.shop.domain.order.service.OrderService;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentResultServiceTest extends IntegrationTestSupport {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentResultService paymentResultService;

    @Test
    void 결제실패시_차감된_재고가_복구된다() {
        Member member = saveMember();
        Product product = saveProduct(10);

        OrderResponse order = createOrder(
                member,
                product,
                3
        );

        assertThat(findStock(product.getId())).isEqualTo(7);

        paymentResultService.failPayment(order.orderId());

        CustomerOrder failedOrder =
                orderRepository.findById(order.orderId()).orElseThrow();

        // 재고 롤백/복구 로직 검증 : 결제 실패
        assertThat(failedOrder.getStatus())
                .isEqualTo(OrderStatus.PAYMENT_FAILED);

        assertThat(findStock(product.getId()))
                .isEqualTo(10);

    }

    @Test
    void 결제실패가_중복호출되어도_재고는_한번만_복구된다() {

        Member member = saveMember();
        Product product = saveProduct(10);

        OrderResponse order = createOrder(
                member,
                product,
                3
        );

        paymentResultService.failPayment(order.orderId());
        paymentResultService.failPayment(order.orderId());
        paymentResultService.failPayment(order.orderId());

        CustomerOrder result =
                orderRepository.findById(order.orderId()).orElseThrow();

        // 동시성 처리 : 조건부 update
        // 첫 호출만 PAYMENT_PENDING -> PAYMENT_FAIL 변경에 성공하므로 재고도 한 번만 복구
        assertThat(result.getStatus())
                .isEqualTo(OrderStatus.PAYMENT_FAILED);

        assertThat(findStock(product.getId()))
                .isEqualTo(10);

    }

    private Member saveMember() {
        return memberRepository.saveAndFlush(
                new Member(
                        "payment@test.com",
                        "encoded-password",
                        "결제회원"
                )
        );
    }

    private Product saveProduct(int stock) {
        return productRepository.saveAndFlush(
                new Product(
                        "결제상품",
                        "TEST",
                        10_000L,
                        stock,
                        "결제 테스트 상품",
                        null
                )
        );
    }

    private OrderResponse createOrder(
            Member member,
            Product product,
            int quantity
    ) {
        return orderService.create(
                member.getEmail(),
                new OrderCreateRequest(List.of(
                        new OrderItemRequest(
                                product.getId(),
                                quantity
                        )
                ))
        );
    }

    private int findStock(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow()
                .getStockQuantity();
    }

}
