package com.taejun.shop.domain.product.dto;

public record ProductSearchCondition(
        String keyword,
        String category,
        Long minPrice,
        Long maxPrice
) {
    public ProductSearchCondition {
        if (minPrice != null && minPrice < 0) {
            throw new IllegalArgumentException("최소 가격은 0 이상이어야 합니다.");
        }

        if (maxPrice != null && maxPrice < 0) {
            throw new IllegalArgumentException("최대 가격은 0 이상이어야 합니다.");
        }

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("최소 가격은 최대 가격보다 클 수 없습니다.");
        }
    }

}
