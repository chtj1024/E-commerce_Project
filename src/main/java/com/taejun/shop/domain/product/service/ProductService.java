package com.taejun.shop.domain.product.service;

import com.taejun.shop.domain.product.dto.*;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.repository.ProductRepository;
import com.taejun.shop.domain.product.repository.ProductRepositoryCustom;
import com.taejun.shop.global.config.RedisCacheConfig;
import com.taejun.shop.global.exception.CustomException;
import com.taejun.shop.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product(
                request.name(),
                request.category(),
                request.price(),
                request.stockQuantity(),
                request.description(),
                request.imageUrl()
        );

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchVisibleProducts(
            ProductSearchCondition condition,
            Pageable pageable
    ) {
        return productRepository
                .searchVisibleProducts(condition, pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllForAdmin() {
        return productRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    @CacheEvict(
            cacheNames = RedisCacheConfig.PRODUCT_CACHE,
            key = "#productId"
    )
    public ProductResponse update(
            Long productId,
            ProductUpdateRequest request
    ) {
        Product product = findProduct(productId);

        product.update(
                request.name(),
                request.category(),
                request.price(),
                request.description(),
                request.imageUrl()
        );

        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(
            cacheNames = RedisCacheConfig.PRODUCT_CACHE,
            key = "#productId"
    )
    public ProductResponse updateStock(
            Long productId,
            ProductStockUpdateRequest request
    ) {
        Product product = findProduct(productId);
        product.updateStock(request.stockQuantity());

        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(
            cacheNames = RedisCacheConfig.PRODUCT_CACHE,
            key = "#productId"
    )
    public ProductResponse updateStatus(
            Long productId,
            ProductStatusUpdateRequest request
    ) {
        Product product = findProduct(productId);
        product.updateStatus(request.status());

        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(
            cacheNames = RedisCacheConfig.PRODUCT_CACHE,
            key = "#productId"
    )
    public void delete(Long productId) {
        Product product = findProduct(productId);
        productRepository.delete(product);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PRODUCT_NOT_FOUND)
                );
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = RedisCacheConfig.PRODUCT_CACHE,
            key = "#productId",
            sync = true
    )
    public ProductResponse findById(Long productId) {
        Product product = findProduct(productId);
        return ProductResponse.from(product);
    }
}
