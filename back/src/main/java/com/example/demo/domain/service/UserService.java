package com.example.demo.domain.service;

import com.example.demo.dto.RegistrationRequestDto;

public interface UserService {
    void registerUser(RegistrationRequestDto requestDto);
//    void sendVerificationCode(String email);
    boolean verifyVerificationCode(String email, String code);

    void sendVerificationEmail(String email); // 메소드 이름 변경
    boolean verifyEmailToken(String token); // 메소드 이름 및 파라미터 변경

}