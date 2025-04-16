package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token; // 인증 코드 대신 고유한 토큰을 저장

    @Column(nullable = false)
    private LocalDateTime expiryAt;

    private LocalDateTime verifiedAt; // 인증 완료 시간
}