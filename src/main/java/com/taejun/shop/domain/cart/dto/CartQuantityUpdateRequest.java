package com.taejun.shop.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartQuantityUpdateRequest(

        @NotNull
        @Min(1)
        Integer quantity
        ) {
}
