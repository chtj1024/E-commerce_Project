package com.taejun.shop.domain.cart.repository;

import com.taejun.shop.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
