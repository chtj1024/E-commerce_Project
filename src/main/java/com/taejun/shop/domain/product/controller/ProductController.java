package com.taejun.shop.domain.product.controller;

import com.taejun.shop.domain.product.dto.*;
import com.taejun.shop.domain.product.service.ProductService;
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

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/api/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return productService.create(request);
    }

    @GetMapping("/api/products")
    public Page<ProductResponse> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
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
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "지원하지 않는 방식입니다."
            );
        };

        ProductSearchCondition condition;

        try {
            condition = new ProductSearchCondition(keyword, category, minPrice, maxPrice);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage(),
                    exception
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

    @GetMapping("/api/admin/products")
    public List<ProductResponse> findAllForAdmin() {
        return productService.findAllForAdmin();
    }

    @PutMapping("/api/admin/products/{productId}")
    public ProductResponse update(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return productService.update(productId, request);
    }

    @PatchMapping("api/admin/products/{productId}/stock")
    public ProductResponse updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody ProductStockUpdateRequest request
    ) {
        return productService.updateStock(productId, request);
    }

    @PatchMapping("api/admin/products/{productId}/status")
    public ProductResponse updateStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return productService.updateStatus(productId, request);
    }

    @DeleteMapping("/api/admin/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId) {
        productService.delete(productId);
    }
}
