package com.taejun.shop.domain.order.dto;

import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.enums.OrderStatus;

import java.time.Instant;

public record OrderResponse(
        Long orderId,
        OrderStatus status,
        Long totalPrice,
        Instant expiresAt
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getExpiresAt()
        );
    }
}
