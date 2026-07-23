package com.taejun.shop.domain.member.service;

import com.taejun.shop.domain.member.dto.*;
import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.member.repository.MemberRepository;
import com.taejun.shop.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberSignupResponse signup(MemberSignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        Member member = new Member(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );

        Member savedMember = memberRepository.save(member); // 변수에 담지 않고, save 메서드만 사용하고 아래에 바로 member 리턴해도 되지만
        // 가독성과 관례 때문에 savedMember에 저장하는 것.
        return MemberSignupResponse.from(savedMember);
    }

    @Transactional
    public LoginTokenResult login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getEmail(),
                member.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        member.updateRefreshToken(refreshToken);

        return new LoginTokenResult(accessToken, refreshToken);
    }

    @Transactional
    public LoginTokenResult refresh(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "유효하지 않은 refresh token입니다."
            );
        }

        String email = jwtTokenProvider.getEmail(refreshToken);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "존재하지 않는 회원입니다."
                ));

        if (!refreshToken.equals(member.getRefreshToken())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "만료되어썩나 로그아웃된 refresh token입니다."
            );
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(
                member.getEmail(),
                member.getRole().name()
        );
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        // 기존 refresh token을 새 값으로 교체
        member.updateRefreshToken(newRefreshToken);

        return new LoginTokenResult(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            return;
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        memberRepository.findByEmail(email)
                .ifPresent(Member::clearRefreshToken);
    }

}
