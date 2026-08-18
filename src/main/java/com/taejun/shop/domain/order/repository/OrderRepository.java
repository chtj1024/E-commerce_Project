package com.taejun.shop.domain.order.repository;

import com.taejun.shop.domain.order.entity.CustomerOrder;
import com.taejun.shop.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    // 동시성 처리 : 주문 상태 조건부 update
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
                    update CustomerOrder o
                    set o.status = :nextStatus
                    where o.id = :orderId
                    and o.status = :expectedStatus
                    """)
    int transitionStatus(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("nextStatus") OrderStatus nextStatus
    );

    @Query("""
            select o.id
            from CustomerOrder o
            where o.status = :status
            and o.expiresAt <= :now
            order by o.id asc
            """)
    List<Long> findExpiredOrderIds(
            @Param("status") OrderStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "orderItems")
    @Query("""
            select o
            from CustomerOrder o
            where o.id = :orderId                
            """)
    Optional<CustomerOrder> findWithItemsById(
            @Param("orderId") Long orderId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CustomerOrder o
            set o.status = :expiredStatus
            where o.id = :orderId
            and o.status = :pendingStatus
            and o.expiresAt <= :now                        
            """)
    int expireIfPending(
            @Param("orderId") Long orderId,
            @Param("pendingStatus") OrderStatus pendingStatus,
            @Param("expiredStatus") OrderStatus expiredStatus,
            @Param("now") LocalDateTime now
    );
}
