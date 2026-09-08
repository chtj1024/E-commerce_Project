package com.taejun.shop.domain.order.service;

import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.order.dto.OrderCreateRequest;
import com.taejun.shop.domain.order.dto.OrderItemRequest;
import com.taejun.shop.domain.order.dto.OrderResponse;
import com.taejun.shop.domain.order.enums.OrderStatus;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.global.exception.CustomException;
import com.taejun.shop.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private OrderService orderService;

    @Test
    void 주문을_생성하면_재고가_차감된다() {
        Member member = saveMember("order@test.com");
        Product product = saveProduct("상품", 10);

        OrderResponse response = orderService.create(
                member.getEmail(),
                new OrderCreateRequest(List.of(
                        new OrderItemRequest(product.getId(), 3)
                ))
        );

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertThat(response.status())
                .isEqualTo(OrderStatus.PAYMENT_PENDING);

        assertThat(updatedProduct.getStockQuantity())
                .isEqualTo(7);

        assertThat(orderRepository.count())
                .isEqualTo(1);
    }

    @Test
    void 복수상품_중_하나의_재고가_부족하면_전체_차감이_롤백된다() {
        Member member = saveMember("order@test.com");

        Product firstProduct = saveProduct("재고 충분 상품", 10);
        Product secondProduct = saveProduct("재고 부족 상품", 1);

        OrderCreateRequest request = new OrderCreateRequest(List.of(
                new OrderItemRequest(firstProduct.getId(), 3),
                new OrderItemRequest(secondProduct.getId(), 2)
        ));

        assertThatThrownBy(() ->
                orderService.create(member.getEmail(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage("재고가 부족하거나 판매할 수 없는 상품입니다.");

        Product firstResult =
                productRepository.findById(firstProduct.getId()).orElseThrow();

        Product secondResult =
                productRepository.findById(secondProduct.getId()).orElseThrow();

        assertThat(firstResult.getStockQuantity()).isEqualTo(10);
        assertThat(secondResult.getStockQuantity()).isEqualTo(1);
        assertThat(orderRepository.count()).isZero();

    }

    private Member saveMember(String email) {
        return memberRepository.saveAndFlush(
                new Member(email, "encoded-password", "테스트회원")
        );
    }

    private Product saveProduct(String name, int stock) {
        return productRepository.saveAndFlush(
                new Product(
                        name,
                        "TEST",
                        10_000L,
                        stock,
                        "테스트 상품",
                        null
                )
        );
    }

}
