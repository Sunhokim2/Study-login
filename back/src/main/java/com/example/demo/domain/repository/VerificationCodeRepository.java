package com.example.demo.domain.repository;

import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.user.email = :email AND vc.token = :code")
    Optional<VerificationCode> findByEmailAndCode(@Param("email") String email, @Param("code") String code);
//    void deleteByEmail(String email);

//    메일에서 인증버튼으로 하는 방식
    Optional<VerificationCode> findByToken(String token);
    Optional<VerificationCode> findByUserAndToken(User user, String token);

    @Transactional
    void deleteByUserAndExpiryAtBefore(User user, LocalDateTime expiryAt);


//    @Transactional
//    void deleteAllByExpiryAtBefore(LocalDateTime expiryAt);
}