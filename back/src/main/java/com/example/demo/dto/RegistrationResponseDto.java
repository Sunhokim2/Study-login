package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//응답에 관한 클래스
@Getter
@Setter
@NoArgsConstructor
public class RegistrationResponseDto {
    private String message;
    public RegistrationResponseDto(String message) {
        this.message = message;
    }
}