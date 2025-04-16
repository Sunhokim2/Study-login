package com.example.demo.domain.service;

import com.example.demo.domain.entity.VerificationCode;
import com.example.demo.domain.repository.VerificationCodeRepository;
import com.example.demo.dto.RegistrationRequestDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserServiceAuth implements UserService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserServiceAuth(UserRepository userRepository, VerificationCodeRepository verificationCodeRepository, JavaMailSender mailSender, BCryptPasswordEncoder passwordEncoder, SpringTemplateEngine templateEngine) {
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.templateEngine = templateEngine;
    }

    @Override
    @Transactional
    public void sendVerificationEmail(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setEmail(email);
        // 비밀번호는 아직 받지 않았으므로 null 또는 임시 값으로 설정
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 임시 비밀번호 저장
        userRepository.save(user); // 먼저 사용자 저장

        // 기존 인증 토큰 삭제 (해당 사용자의)
        verificationCodeRepository.deleteByUserAndExpiryAtBefore(user, LocalDateTime.now());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryAt = LocalDateTime.now().plusHours(1); // 인증 링크 유효시간 1시간

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUser(user); // 사용자 연관관계 설정
        verificationCode.setToken(token);
        verificationCode.setExpiryAt(expiryAt);
        verificationCodeRepository.save(verificationCode);

        sendVerificationEmailHtml(email, token);

//        // 기존 인증 코드 삭제
//        verificationCodeRepository.deleteByEmail(email);
//
//        Random random = new Random();
//        String code = String.format("%06d", random.nextInt(999999));
//        LocalDateTime expiryAt = LocalDateTime.now().plusMinutes(5); // 인증 코드 유효시간 5분


//        💨이건 이메일 인증코드방식에 사용됩니다.(현재잠금)
//        VerificationCode verificationCode = new VerificationCode();
//        verificationCode.setEmail(email);
//        verificationCode.setCode(code);
//        verificationCode.setExpiryAt(expiryAt);
//        verificationCodeRepository.save(verificationCode);
//
//        sendVerificationCodeEmail(email, code);
    }

    private void sendVerificationEmailHtml(String email, String token) {
        Context context = new Context();
        context.setVariable("verificationUrl", baseUrl + "/api/verify-email?token=" + token);

        String html = templateEngine.process("verification-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("이메일 인증");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional
    public boolean verifyEmailToken(String token) {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeRepository.findByToken(token);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode verificationCode = verificationCodeOptional.get();
            if (verificationCode.getExpiryAt().isAfter(LocalDateTime.now()) && verificationCode.getVerifiedAt() == null) {
                User user = verificationCode.getUser();
                user.setVerified(true);
                userRepository.save(user);
                verificationCode.setVerifiedAt(LocalDateTime.now());
                verificationCodeRepository.save(verificationCode); // 인증 시간 업데이트
                return true;
            } else {
                return false; // 토큰 만료 또는 이미 인증됨
            }
        }
        return false; // 유효하지 않은 토큰
    }


    private void sendVerificationCodeEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n\n해당 코드는 5분 동안 유효합니다.");
        mailSender.send(message);
    }

    @Override
    @Transactional
    public boolean verifyVerificationCode(String email, String code) {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeRepository.findByEmailAndCode(email, code);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode verificationCode = verificationCodeOptional.get();
            if (verificationCode.getExpiryAt().isAfter(LocalDateTime.now())) {
                verificationCodeRepository.delete(verificationCode); // 인증 성공 후 코드 삭제
                return true;
            } else {
                verificationCodeRepository.delete(verificationCode); // 만료된 코드 삭제
                return false; // 인증 코드 만료
            }
        }
        return false; // 인증 코드 불일치
    }

    @Override
    @Transactional
    public void registerUser(RegistrationRequestDto requestDto) {
        Optional<User> existingUserOptional = userRepository.findByEmail(requestDto.getEmail());

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            // User 엔티티에 isVerified() 또는 getVerified() 메서드가 있다고 가정
            if (existingUser.isVerified()) {
                // 이미 존재하고 인증된 사용자의 비밀번호 업데이트
                existingUser.setPassword(passwordEncoder.encode(requestDto.getPassword()));
                userRepository.save(existingUser);
                return; // 비밀번호 업데이트 후 메서드 종료
            } else {
                // 이미 존재하지만 아직 인증되지 않은 경우
                throw new IllegalStateException("아직 인증되지 않았습니다.");
            }
        }
    }

}