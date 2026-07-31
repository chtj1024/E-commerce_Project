package com.taejun.shop.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(

        @NotBlank(message = "상품명을 입력해 주세요.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "가격을 입력해 주세요.")
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        Long price,

        @NotNull(message = "재고 수량을 입력해 주세요.")
        @PositiveOrZero(message = "재고 수량은 0개 이상이어야 합니다.")
        Integer stockQuantity,

        @NotBlank(message = "상품 설명을 입력해 주세요.")
        @Size(max = 2000, message = "상품 설명은 2000자 이하여야 합니다.")
        String description,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String imageUrl,

        @NotBlank(message = "카테고리를 입력해 주세요.")
        @Size(max = 50, message = "카테고리는 50자 이하여야 합니다.")
        String category
        ) {
}
