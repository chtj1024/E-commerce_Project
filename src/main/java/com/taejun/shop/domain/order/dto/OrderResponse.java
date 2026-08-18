package com.taejun.shop.domain.order.dto;

import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        OrderStatus status,
        Long totalPrice,
        LocalDateTime expiresAt
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
