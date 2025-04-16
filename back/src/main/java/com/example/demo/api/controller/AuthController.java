package com.example.demo.api.controller;


import com.example.demo.config.JwtUtil;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.domain.service.UserService;
import com.example.demo.dto.LoginRequestDto;
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
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserService userService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * -code로 되어있는데 이메일 보내는 거임. 코드인증이 아닌 이메일 토큰인증 방식을 사용하는 것임
     * @param payload
     * @return
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<RegistrationResponseDto> sendVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            return new ResponseEntity<>(new RegistrationResponseDto("이메일을 입력해주세요."), HttpStatus.BAD_REQUEST);
        }
        try {
            userService.sendVerificationEmail(email);
            return new ResponseEntity<>(new RegistrationResponseDto("인증 이메일을 발송했습니다."), HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new RegistrationResponseDto(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(new RegistrationResponseDto("인증 이메일 발송 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
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

    /**
     * 이메일 인증. 사용자가 이메일 받아서 버튼누르면 시행되는 api. 추후에 프론트엔드 주소를 호출하는 것으로해서
     * 프론트페이지를 보이게 할 수 있다. 지금은 백엔드 메세지만 날아간다.
     * @param token
     * @return
     */
    @GetMapping("/verify-email") // 메소드 이름 및 요청 방식 변경 (GET 요청으로 처리)
    public ResponseEntity<RegistrationResponseDto> verifyEmail(@RequestParam("token") String token) {
        if (token == null || token.trim().isEmpty()) {
            return new ResponseEntity<>(new RegistrationResponseDto("인증 토큰이 유효하지 않습니다."), HttpStatus.BAD_REQUEST);
        }
        if (userService.verifyEmailToken(token)) {
            return new ResponseEntity<>(new RegistrationResponseDto("이메일 인증에 성공했습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new RegistrationResponseDto("이메일 인증에 실패했습니다. 토큰이 유효하지 않거나 만료되었을 수 있습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> registerUser(@Valid @RequestBody RegistrationRequestDto requestDto) {
        try {
            userService.registerUser(requestDto);
            RegistrationResponseDto responseDto = new RegistrationResponseDto();
            responseDto.setMessage("회원가입에 성공했습니다. 이메일인증을 완료해주세요.");
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

    /**
     * 로그인 성공시 jwt토큰 발급한다.
     * @param payload
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto payload) {
        String email = payload.getEmail();
        String password = payload.getPassword();

        if (email == null || password == null) {
            return new ResponseEntity<>("이메일과 비밀번호를 입력해주세요.", HttpStatus.BAD_REQUEST);
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty() || !userOptional.get().isVerified()) { // 이메일 인증 여부 확인 추가
            return new ResponseEntity<>("존재하지 않는 이메일이거나 이메일 인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new ResponseEntity<>("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        // 전부 다 맞을 시 로그인 성공. jwt토큰 발급
        String jwtToken = jwtUtil.generateToken(user.getEmail()); // JWT 토큰 생성 시 이메일(또는 사용자 식별 정보) 포함
        return ResponseEntity.ok(Map.of("token", jwtToken)); // JWT 토큰을 응답 body에 담아서 반환
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
