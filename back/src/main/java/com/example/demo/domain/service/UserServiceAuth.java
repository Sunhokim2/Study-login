package com.example.demo.domain.service;

import com.example.demo.domain.entity.VerificationCode;
import com.example.demo.domain.repository.VerificationCodeRepository;
import com.example.demo.dto.RegistrationRequestDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceAuth implements UserService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserServiceAuth(UserRepository userRepository, VerificationCodeRepository verificationCodeRepository, JavaMailSender mailSender, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 기존 인증 코드 삭제
        verificationCodeRepository.deleteByEmail(email);

        Random random = new Random();
        String code = String.format("%06d", random.nextInt(999999));
        LocalDateTime expiryAt = LocalDateTime.now().plusMinutes(5); // 인증 코드 유효시간 5분

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpiryAt(expiryAt);
        verificationCodeRepository.save(verificationCode);

        sendVerificationCodeEmail(email, code);
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
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setVerified(true); // 이메일 인증이 완료되었으므로 바로 true 설정
        userRepository.save(user);
    }

}