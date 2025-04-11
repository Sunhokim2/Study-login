package com.example.demo.domain.service;

import com.example.demo.dto.RegistrationRequestDto;

public interface UserService {
    void registerUser(RegistrationRequestDto requestDto);
    void sendVerificationCode(String email);
    boolean verifyVerificationCode(String email, String code);
}