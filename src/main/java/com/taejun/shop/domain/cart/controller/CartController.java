package com.taejun.shop.domain.cart.controller;

import com.taejun.shop.domain.cart.dto.CartItemAddRequest;
import com.taejun.shop.domain.cart.dto.CartItemResponse;
import com.taejun.shop.domain.cart.dto.CartQuantityUpdateRequest;
import com.taejun.shop.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "장바구니 API",
        description = "인증 회원의 장바구니 조회 및 상품 관리"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/user/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "장바구니 조회"
    )
    @GetMapping
    public List<CartItemResponse> findAll(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        return cartService.findAll(
                authentication.getName()
        );
    }

    @Operation(
            summary = "장바구니 상품 추가"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse add(
            @Parameter(hidden = true)
            Authentication authentication,

            @Valid @RequestBody
            CartItemAddRequest request
    ) {
        return cartService.add(
                authentication.getName(),
                request
        );
    }

    @Operation(
            summary = "장바구니 수량 변경",
            description = "장바구니 항목의 수량을 1개 이상으로 변경합니다."
    )
    @PatchMapping("/{cartItemId}")
    public CartItemResponse updateQuantity(
            @Parameter(hidden = true)
            Authentication authentication,

            @Parameter(description = "장바구니 항목 ID", example = "1")
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartQuantityUpdateRequest request
    ) {
        return cartService.updateQuantity(
                authentication.getName(),
                cartItemId,
                request
        );
    }

    @Operation(
            summary = "장바구니 상품 삭제"
    )
    @DeleteMapping("/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(hidden = true)
            Authentication authentication,

            @Parameter(description = "장바구니 항목 ID", example = "1")
            @PathVariable Long cartItemId
    ) {
        cartService.delete(
                authentication.getName(),
                cartItemId
        );
    }
}
