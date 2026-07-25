package com.taejun.shop.domain.product.service;

import com.taejun.shop.domain.product.dto.ProductCreateRequest;
import com.taejun.shop.domain.product.dto.ProductResponse;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.enums.ProductStatus;
import com.taejun.shop.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product(
                request.name(),
                request.price(),
                request.stockQuantity(),
                request.description(),
                request.imageUrl()
        );

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllVisible() {
        return productRepository.findAllByStatusNotOrderByCreatedAtDesc(ProductStatus.HIDDEN)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }
}
