package com.taejun.shop.domain.product.dto;

import com.taejun.shop.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
        @NotNull
        ProductStatus status
) {
}
