package com.taejun.shop.domain.product.repository;

import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByStatusNotOrderByCreatedAtDesc(ProductStatus status);
}
