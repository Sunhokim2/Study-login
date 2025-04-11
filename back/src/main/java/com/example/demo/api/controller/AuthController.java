package com.example.demo.api.controller;


import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.domain.service.UserService;
import com.example.demo.dto.RegistrationRequestDto;
import com.example.demo.dto.RegistrationResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // 특정 Origin 허용
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<RegistrationResponseDto> sendVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            return new ResponseEntity<>(new RegistrationResponseDto("이메일을 입력해주세요."), HttpStatus.BAD_REQUEST);
        }
        try {
            userService.sendVerificationCode(email);
            return new ResponseEntity<>(new RegistrationResponseDto("인증 코드를 이메일로 발송했습니다."), HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new RegistrationResponseDto(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(new RegistrationResponseDto("인증 코드 발송 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<RegistrationResponseDto> verifyCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");
        if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return new ResponseEntity<>(new RegistrationResponseDto("이메일과 인증 코드를 입력해주세요."), HttpStatus.BAD_REQUEST);
        }
        if (userService.verifyVerificationCode(email, code)) {
            return new ResponseEntity<>(new RegistrationResponseDto("이메일 인증에 성공했습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new RegistrationResponseDto("이메일 인증에 실패했습니다. 코드가 틀리거나 만료되었을 수 있습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> registerUser(@Valid @RequestBody RegistrationRequestDto requestDto) {
        try {
            userService.registerUser(requestDto);
            RegistrationResponseDto responseDto = new RegistrationResponseDto();
            responseDto.setMessage("회원가입에 성공했습니다.");
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            RegistrationResponseDto responseDto = new RegistrationResponseDto();
            responseDto.setMessage(e.getMessage());
            return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
        } catch (Exception e) {
            RegistrationResponseDto responseDto = new RegistrationResponseDto();
            responseDto.setMessage("회원가입 중 오류가 발생했습니다.");
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        if (email == null || password == null) {
            return new ResponseEntity<>("이메일과 비밀번호를 입력해주세요.", HttpStatus.BAD_REQUEST);
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>("존재하지 않는 이메일입니다.", HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new ResponseEntity<>("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
