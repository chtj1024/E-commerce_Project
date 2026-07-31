package com.taejun.shop.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.taejun.shop.domain.product.dto.ProductSearchCondition;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.enums.ProductStatus;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.taejun.shop.domain.product.entity.QProduct.product;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Product> searchVisibleProducts(ProductSearchCondition condition, Pageable pageable) {

        BooleanBuilder conditions = createConditions(condition);

        JPAQuery<Product> contentQuery = queryFactory
                .selectFrom(product)
                .where(conditions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        applySort(contentQuery, pageable.getSort());

        List<Product> content = contentQuery.fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(conditions)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total == null ? 0 : total
        );
    }

    private BooleanBuilder createConditions(ProductSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        // 일반 상품 조회에서는 숨김 상품 제외
        builder.and(product.status.ne(ProductStatus.HIDDEN));

        if (hasText(condition.keyword())) {
            String keyword = condition.keyword().trim();

            builder.and(
                    product.name.containsIgnoreCase(keyword)
                            .or(product.description.containsIgnoreCase(keyword))
            );
        }

        if (hasText(condition.category())) {
            builder.and(
                    product.category.equalsIgnoreCase(
                            condition.category().trim()
                    )
            );
        }

        if (condition.minPrice() != null) {
            builder.and(product.price.goe(condition.minPrice()));
        }

        if (condition.maxPrice() != null) {
            builder.and(product.price.loe(condition.maxPrice()));
        }

        return builder;
    }

    private void applySort(
            JPAQuery<Product> query,
            Sort sort
    ) {
        // Product 엔티티의 필드를 문자열 이름으로 찾아서 표현하겠다.
        PathBuilder<Product> path = new PathBuilder<>(Product.class, product.getMetadata());

        for (Sort.Order sortOrder : sort) {
            Order direction = sortOrder.isAscending()
                    ? Order.ASC
                    : Order.DESC;

            query.orderBy(new OrderSpecifier<>(
                    direction,
                    path.getComparable(
                            sortOrder.getProperty(),
                            Comparable.class
                    )
            ));
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
