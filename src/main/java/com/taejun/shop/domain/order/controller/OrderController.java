package com.taejun.shop.domain.order.controller;

import com.taejun.shop.domain.order.dto.OrderCreateRequest;
import com.taejun.shop.domain.order.dto.OrderResponse;
import com.taejun.shop.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            Authentication authentication,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return orderService.create(
                authentication.getName(),
                request
        );
    }

}
