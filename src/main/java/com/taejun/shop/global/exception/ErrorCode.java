package com.taejun.shop.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(
            HttpStatus.BAD_REQUEST,
            "COMMON_001",
            "입력값이 올바르지 않습니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON_002",
            "서버 내부 오류가 발생했습니다."
    ),
    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "SECURITY_001",
            "인증이 필요합니다."
    ),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "SECURITY_002",
            "접근 권한이 없습니다."
    ),

    // 회원
    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "MEMBER_001",
            "이미 가입된 이메일입니다."
    ),
    INVALID_LOGIN_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "MEMBER_002",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "MEMBER_003",
            "유효하지 않은 refresh token입니다."
    ),
    MEMBER_NOT_FOUND(
            HttpStatus.UNAUTHORIZED,
            "MEMBER_004",
            "인증된 회원을 찾을 수 없습니다."
    ),

    // 상품
    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PRODUCT_001",
            "상품을 찾을 수 없습니다."
    ),
    PRODUCT_NOT_ACTIVE(
            HttpStatus.BAD_REQUEST,
            "PRODUCT_002",
            "현재 판매 중인 상품이 아닙니다."
    ),
    INSUFFICIENT_STOCK(
            HttpStatus.CONFLICT,
            "PRODUCT_003",
            "상품 재고가 부족합니다."
    ),

    // 장바구니
    CART_ITEM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CART_001",
            "장바구니 상품을 찾을 수 없습니다."
    ),

    // 주문
    ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ORDER_001",
            "주문을 찾을 수 없습니다."
    ),
    ORDER_ALREADY_CLOSED(
            HttpStatus.CONFLICT,
            "ORDER_002",
            "이미 종료된 주문입니다."
    ),

    //재고
    STOCK_RESTORE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "STOCK_001",
            "상품 재고 복구에 실패했습니다."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;

}
