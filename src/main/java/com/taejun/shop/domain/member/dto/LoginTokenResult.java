package com.taejun.shop.domain.member.dto;

public record LoginTokenResult(
        String accessToken,
        String refreshToken
) {
}
