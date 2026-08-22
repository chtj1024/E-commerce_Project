package com.taejun.shop.domain.member.controller;

import com.taejun.shop.domain.member.dto.*;
import com.taejun.shop.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "회원 API", description = "회원가입, 로그인, 토큰 갱신 및 로그아웃")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "회원가입"
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberSignupResponse signup(@Valid @RequestBody MemberSignupRequest request) {
        return memberService.signup(request);
    }

    @Operation(
            summary = "로그인",
            description = """
                    이메일과 비밀번호를 검증하고 access token을 반환합니다.
                    refresh token은 HttpOnly 쿠키로 발급됩니다.
                    """
    )
    @PostMapping("/login")
    public MemberLoginResponse login(@Valid @RequestBody MemberLoginRequest request,
                                     HttpServletResponse response
    ) {
        LoginTokenResult result = memberService.login(request);

        addRefreshTokenCookie(response, result.refreshToken());

        return new MemberLoginResponse(result.accessToken());
    }

    @Operation(
            summary = "Access Token 갱신",
            description = """
                   Refresh Token을 검증하고 토큰을 재발급합니다.
                    """
    )
    @PostMapping("/refresh")
    public MemberLoginResponse refresh(
            @Parameter(
                    description = "로그인 시 HttpOnly 쿠키로 발급된 refresh token"
            )
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        LoginTokenResult result = memberService.refresh(refreshToken);

        addRefreshTokenCookie(response, result.refreshToken());

        return new MemberLoginResponse(result.accessToken());
    }

    @Operation(
            summary = "로그아웃",
            description = """
                    Refresh Token을 무효화하고 인증 쿠키를 삭제합니다.
                    """
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Parameter(
                    description = "로그인 시 HttpOnly 쿠키로 발급된 refresh token"
            )
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        memberService.logout(refreshToken);
        clearRefreshTokenCookie(response);
    }

    private void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken
    ) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // HTTPS 운영 환경에서는 true
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
