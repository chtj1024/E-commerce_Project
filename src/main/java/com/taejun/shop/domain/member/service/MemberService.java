package com.taejun.shop.domain.member.service;

import com.taejun.shop.domain.member.dto.MemberSignupRequest;
import com.taejun.shop.domain.member.dto.MemberSignupResponse;
import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

}
