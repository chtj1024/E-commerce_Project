package com.taejun.shop.domain.cart.repository;

import com.taejun.shop.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = "product")
    List<CartItem> findAllByMemberIdOrderByCreatedAtDesc(
            Long memberId
    );

    Optional<CartItem> findByMemberIdAndProductId(
            Long memberId,
            Long productId
    );

    @EntityGraph(attributePaths = "product")
    Optional<CartItem> findByIdAndMemberId(
            Long cartItemId,
            Long memberId
    );

    // 주문에 포함된 상품만 회원 장바구니에서 제거
    @Modifying(flushAutomatically = true)
    @Query("""
            delete from CartItem c
            where c.member.id = :memberId
            and c.product.id in :productIds                       
            """)
    int deletePurchasedItems(
            @Param("memberId") Long memberId,
            @Param("productIds") Collection<Long> productIds
    );
}
