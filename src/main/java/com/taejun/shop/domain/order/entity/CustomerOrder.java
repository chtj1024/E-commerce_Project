package com.taejun.shop.domain.order.entity;


import com.taejun.shop.domain.common.entity.BaseEntity;
import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(
                        name = "idx_orders_status_expires_at",
                        columnList = "status, expiresAt"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false)
    private Long totalPrice;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<OrderItem> orderItems = new ArrayList<>();

    public CustomerOrder(Member member, LocalDateTime expiresAt) {
        this.member = member;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.totalPrice = 0L;
        this.expiresAt = expiresAt;
    }

    public void addItem(
            Long productId,
            String productName,
            Long unitPrice,
            int quantity
            ) {
        OrderItem item = new OrderItem(
                this,
                productId,
                productName,
                unitPrice,
                quantity
        );

        orderItems.add(item);

        long itemPrice = Math.multiplyExact(unitPrice, quantity);
        totalPrice = Math.addExact(totalPrice, itemPrice);
    }
}
