package com.taejun.shop.domain.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductStockUpdateRequest(
        @NotNull
        @PositiveOrZero
        Integer stockQuantity
) {
}
