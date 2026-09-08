-- 현재 애플리케이션의 최초 기준 스키마
-- MySQL 8.4 / utf8mb4 / utf8mb4_0900_ai_ci
--
-- 기존 DB에서는 Flyway baseline version 1로 등록되므로 이 파일이 실행되지 않는다.
-- 신규 또는 테스트용 빈 DB에서는 이 파일이 실행되어 전체 스키마를 생성한다.

CREATE TABLE member
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    name          VARCHAR(30)  NOT NULL,
    password      VARCHAR(255) NOT NULL,
    refresh_token VARCHAR(500) NULL,
    role          ENUM ('ADMIN', 'USER') NOT NULL,

    CONSTRAINT pk_member PRIMARY KEY (id),
    CONSTRAINT uk_member_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE product
(
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    created_at     DATETIME(6)   NOT NULL,
    updated_at     DATETIME(6)   NOT NULL,
    category       VARCHAR(50)   NOT NULL,
    description    VARCHAR(2000) NOT NULL,
    image_url      VARCHAR(500)  NULL,
    name           VARCHAR(100)  NOT NULL,
    price          BIGINT        NOT NULL,
    status         ENUM ('ACTIVE', 'HIDDEN', 'SOLD_OUT') NOT NULL,
    stock_quantity INT           NOT NULL,
    version        BIGINT        NOT NULL,

    CONSTRAINT pk_product PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE orders
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    status      ENUM (
                    'CANCELED',
                    'EXPIRED',
                    'PAID',
                    'PAYMENT_FAILED',
                    'PAYMENT_PENDING'
                ) NOT NULL,
    total_price BIGINT      NOT NULL,
    member_id   BIGINT      NOT NULL,

    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_member
        FOREIGN KEY (member_id)
            REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE INDEX idx_orders_status_expires_at
    ON orders (status, expires_at);

CREATE INDEX idx_orders_member_id
    ON orders (member_id);


CREATE TABLE order_item
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    product_id   BIGINT       NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity     INT          NOT NULL,
    unit_price   BIGINT       NOT NULL,
    order_id     BIGINT       NOT NULL,

    CONSTRAINT pk_order_item PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE INDEX idx_order_item_order_id
    ON order_item (order_id);


CREATE TABLE cart_item
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    quantity   INT         NOT NULL,
    member_id  BIGINT      NOT NULL,
    product_id BIGINT      NOT NULL,

    CONSTRAINT pk_cart_item PRIMARY KEY (id),
    CONSTRAINT uk_cart_item_member_product
        UNIQUE (member_id, product_id),
    CONSTRAINT fk_cart_item_member
        FOREIGN KEY (member_id)
            REFERENCES member (id),
    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id)
            REFERENCES product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE INDEX idx_cart_item_product_id
    ON cart_item (product_id);