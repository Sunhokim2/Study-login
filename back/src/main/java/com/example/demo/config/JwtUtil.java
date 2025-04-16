package com.example.demo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // 토큰 유효 시간 (밀리초)
    private long expiration;


    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(email) // payload에 사용자 이메일 저장
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret) // secret 문자열 직접 사용
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret) // secret 문자열 직접 사용
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 토큰의 유효성 검증. 만료되지않았는지, 변조되지 않았는지를 체크
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token); // secret 문자열 직접 사용
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}