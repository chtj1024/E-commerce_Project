package com.taejun.shop.domain.member.dto;

import com.taejun.shop.domain.member.entity.Member;

public record MemberSignupResponse(
        Long id,
        String email,
        String name
) {

    public static MemberSignupResponse from(Member member) {
        return new MemberSignupResponse(
                member.getId(),
                member.getEmail(),
                member.getName()
        );
    }
}
