package com.taejun.shop.domain.product.entity;

import com.taejun.shop.domain.common.entity.BaseEntity;
import com.taejun.shop.domain.product.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version // 낙관적 락
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(nullable = false, length = 50)
    private String category;

    public Product(String name, String category, Long price, Integer stockQuantity, String description, String imageUrl) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = stockQuantity == 0
                ? ProductStatus.SOLD_OUT
                : ProductStatus.ACTIVE;
    }

    public void update(
            String name,
            String category,
            Long price,
            String description,
            String imageUrl
    ) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void updateStock(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;

        if (stockQuantity == 0) {
            this.status = ProductStatus.SOLD_OUT;
        } else if (this.status == ProductStatus.SOLD_OUT) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public void updateStatus(ProductStatus status) {
        if (status == ProductStatus.ACTIVE && stockQuantity == 0) {
            throw new IllegalArgumentException("재고가 없는 상품은 판매 중으로 변경할 수 없습니다.");
        }

        this.status = status;
    }
}
