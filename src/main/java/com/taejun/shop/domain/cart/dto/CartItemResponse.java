package com.taejun.shop.domain.cart.dto;

import com.taejun.shop.domain.cart.entity.CartItem;
import com.taejun.shop.domain.product.entity.Product;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Long price,
        String imageUrl,
        Integer stockQuantity,
        Integer quantity,
        Long totalPrice
) {
    public static CartItemResponse from(CartItem cartItem) {
        Product product = cartItem.getProduct();

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStockQuantity(),
                cartItem.getQuantity(),
                product.getPrice() * cartItem.getQuantity()
        );
    }
}
