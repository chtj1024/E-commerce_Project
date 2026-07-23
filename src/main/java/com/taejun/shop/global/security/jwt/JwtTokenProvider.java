package com.taejun.shop.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
// @RequiredArgsConstructor 으로 생성한 생성자에 파라미터에는 @Value를 붙일 수 없기 때문에 생성자를 만든다.

public class JwtTokenProvider {

    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider (
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(String email, String role) {
        return createToken(email, role, "access", accessTokenExpiration);
    }

    public String createRefreshToken(String email) {
        return createToken(email, null, "refresh", refreshTokenExpiration);
    }

    public boolean isAccessToken(String token) {
        return validateToken(token)
                && "access".equals(getClaims(token).get("token_type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return validateToken(token)
                && "refresh".equals(getClaims(token).get("token_type", String.class));
    }

    private String createToken(String email, String role, String tokenType, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        var builder = Jwts.builder()
                .subject(email) // subject : 이 토큰의 주인. 주로 회원ID를 넣는다.
                .claim("token_type", tokenType) // claim : 토큰의 담는 정보로 키-값으로 저장한다.
                .issuedAt(now) // jwt가 언제 발급되었는지를 저장하는 메서드
                .expiration(expiration); // jwt가 언제 만료되는지를 설정하는 메서드

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.signWith(getSigningKey()).compact();
        // signWith(getSigningKey()) : 디지털 서명을 추가하는 메서드
        // compact() : 설정한 jwt의 내용을 하나의 문자열(Token)으로 만들어 반환하는 메서드
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (SecurityException | JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
