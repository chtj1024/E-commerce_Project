package com.taejun.shop.domain.order.controller;

import com.taejun.shop.domain.order.dto.OrderCreateRequest;
import com.taejun.shop.domain.order.dto.OrderResponse;
import com.taejun.shop.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "주문 API",
        description = "인증 회원의 주문 생성 및 관리"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/user/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "주문 생성",
            description = """
                    상품 ID와 수량을 입력하여 주문을 생성합니다.
                    주문 생성 과정에서 상품 상태와 재고를 확인합니다.
                    """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Parameter(hidden = true)
            Authentication authentication,

            @Valid @RequestBody OrderCreateRequest request
    ) {
        return orderService.create(
                authentication.getName(),
                request
        );
    }

}
