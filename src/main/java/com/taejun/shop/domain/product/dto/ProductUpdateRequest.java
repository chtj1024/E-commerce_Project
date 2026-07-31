package com.taejun.shop.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @PositiveOrZero
        Long price,

        @NotBlank
        @Size(max = 2000)
        String description,

        @Size(max = 500)
        String imageUrl,

        @NotBlank
        @Size(max = 50)
        String category

        ) {
}
