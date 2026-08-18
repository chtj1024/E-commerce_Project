package com.taejun.shop.domain.cart.controller;

import com.taejun.shop.domain.cart.dto.CartItemAddRequest;
import com.taejun.shop.domain.cart.dto.CartItemResponse;
import com.taejun.shop.domain.cart.dto.CartQuantityUpdateRequest;
import com.taejun.shop.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public List<CartItemResponse> findAll(
            Authentication authentication
    ) {
        return cartService.findAll(
                authentication.getName()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse add(
            Authentication authentication,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        return cartService.add(
                authentication.getName(),
                request
        );
    }

    @PatchMapping("/{cartItemId}")
    public CartItemResponse updateQuantity(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartQuantityUpdateRequest request
    ) {
        return cartService.updateQuantity(
                authentication.getName(),
                cartItemId,
                request
        );
    }

    @DeleteMapping("/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            Authentication authentication,
            @PathVariable Long cartItemId
    ) {
        cartService.delete(
                authentication.getName(),
                cartItemId
        );
    }
}
