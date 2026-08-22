package com.taejun.shop.domain.product.controller;

import com.taejun.shop.domain.product.dto.*;
import com.taejun.shop.domain.product.service.ProductService;
import com.taejun.shop.global.exception.CustomException;
import com.taejun.shop.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(
        name = "상품 API",
        description = "상품 조회 및 관리자 상품 관리"
)
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "상품 등록",
            description = "관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return productService.create(request);
    }

    @Operation(
            summary = "상품 목록 조회",
            description = "판매 중인 상품을 조건별로 검색합니다."
    )
    @GetMapping("/api/products")
    public Page<ProductResponse> findAll(
            @Parameter(description = "상품명 검색어", example = "키보드")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "상품 카테고리", example = "전자기기")
            @RequestParam(required = false) String category,

            @Parameter(description = "최소 가격", example = "10000")
            @RequestParam(required = false) Long minPrice,

            @Parameter(description = "최대 가격", example = "100000")
            @RequestParam(required = false) Long maxPrice,

            @Parameter(description = "페이지 번호. 음수 입력 시 0으로 보정", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기. 1~100 범위를 벗어나면 가장 가까운 값으로 보정", example = "12")
            @RequestParam(defaultValue = "12") int size,

            @Parameter(
                    description = "정렬 방식",
                    example = "latest",
                    schema = @Schema(
                            allowableValues = {
                                    "latest",
                                    "priceAsc",
                                    "priceDesc"
                            }
                    )
            )
            @RequestParam(defaultValue = "latest") String sort
    ) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Sort productSort = switch (sort) {
            case "priceAsc" -> Sort.by(
                    Sort.Order.asc("price"),
                    Sort.Order.desc("id")
            );
            case "priceDesc" -> Sort.by(
                    Sort.Order.desc("price"),
                    Sort.Order.desc("id")
            );
            case "latest" -> Sort.by(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            );
            default -> throw new CustomException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "지원하지 않는 정렬 방식입니다."
            );
        };

        ProductSearchCondition condition;

        try {
            condition = new ProductSearchCondition(keyword, category, minPrice, maxPrice);
        } catch (IllegalArgumentException exception) {
            throw new CustomException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    exception.getMessage()
            );
        }

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                productSort
        );

        return productService.searchVisibleProducts(
                condition,
                pageable
        );
    }

    @Operation(
            summary = "관리자용 전체 상품 조회",
            description = "판매 상태와 관계없이 모든 상품을 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/admin/products")
    public List<ProductResponse> findAllForAdmin() {
        return productService.findAllForAdmin();
    }

    @Operation(
            summary = "상품 정보 수정"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/admin/products/{productId}")
    public ProductResponse update(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,

            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return productService.update(productId, request);
    }

    @Operation(
            summary = "상품 재고 변경"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/api/admin/products/{productId}/stock")
    public ProductResponse updateStock(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,

            @Valid @RequestBody ProductStockUpdateRequest request
    ) {
        return productService.updateStock(productId, request);
    }

    @Operation(
            summary = "상품 판매 상태 변경"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/api/admin/products/{productId}/status")
    public ProductResponse updateStatus(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,

            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return productService.updateStatus(productId, request);
    }

    @Operation(
            summary = "상품 삭제"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/admin/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable
            Long productId
    ) {
        productService.delete(productId);
    }
}
