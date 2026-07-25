package com.taejun.shop.domain.product.controller;

import com.taejun.shop.domain.product.dto.ProductCreateRequest;
import com.taejun.shop.domain.product.dto.ProductResponse;
import com.taejun.shop.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public List<ProductResponse> findAll() {
        return productService.findAllVisible();
    }
}
