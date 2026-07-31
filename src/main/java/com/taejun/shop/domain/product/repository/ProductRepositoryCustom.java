package com.taejun.shop.domain.product.repository;

import com.taejun.shop.domain.product.dto.ProductSearchCondition;
import com.taejun.shop.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> searchVisibleProducts(
            ProductSearchCondition condition,
            Pageable pageable
    );
}
