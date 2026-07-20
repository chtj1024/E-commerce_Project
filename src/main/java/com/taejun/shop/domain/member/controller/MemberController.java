package com.taejun.shop.domain.member.controller;

import com.taejun.shop.domain.member.dto.MemberSignupRequest;
import com.taejun.shop.domain.member.dto.MemberSignupResponse;
import com.taejun.shop.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberSignupResponse signup(@Valid @RequestBody MemberSignupRequest request) {
        return memberService.signup(request);
    }
}
