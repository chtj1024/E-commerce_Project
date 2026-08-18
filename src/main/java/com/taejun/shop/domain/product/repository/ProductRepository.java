package com.taejun.shop.domain.product.repository;

import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    Page<Product> findAllByStatusNot(
            ProductStatus status,
            Pageable pageable
    );

    List<Product> findAllByOrderByCreatedAtDesc();

    // 동시성 처리 : 조건부 update
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE product
            SET status = CASE
                    WHEN stock_quantity - :quantity = 0
                        THEN 'SOLD_OUT'
                    ELSE status
                END,
                stock_quantity = stock_quantity - :quantity,
                version = version + 1
            WHERE id = :productId
            AND status = 'ACTIVE'
            AND stock_quantity >= :quantity
            """, nativeQuery = true)
    int decreaseStock(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    // 재고 롤백/복구 로직
    // 동시성 처리 : 조건부/원자적 update
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE product
                SET stock_quantity = stock_quantity + :quantity,
                    status = CASE
                        WHEN status = 'SOLD_OUT'
                            THEN 'ACTIVE'
                        ELSE status
                    END,
                    version = version + 1
            WHERE id = :productId
            """, nativeQuery = true)
    int restoreStock(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );
}
