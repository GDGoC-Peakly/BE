package com.example.peakly.domain.auth.jwt;

import io.jsonwebtoken.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-exp-seconds}") long accessExpSeconds,
            @Value("${jwt.refresh-token-exp-seconds}") long refreshExpSeconds,
            @Value("${jwt.issuer:peakly}") String issuer
    ) {
        this.key = KeysCompat.hmacShaKey(secret);
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;
        this.issuer = issuer;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessExpSeconds, "ACCESS");
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshExpSeconds, "REFRESH");
    }

    private String createToken(Long userId, long expSeconds, String typ) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("typ", typ)
                .signWith(key)
                .compact();
    }

    public Long parseAccessTokenAndGetUserId(String token) {
        Claims claims = parseClaims(token);

        Object typ = claims.get("typ");
        if (typ == null || !"ACCESS".equals(String.valueOf(typ))) {
            throw new JwtException("Not an access token");
        }

        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new JwtException("Missing subject");
        }
        return Long.parseLong(sub);
    }

    public boolean isValidAccessToken(String token) {
        try {
            parseAccessTokenAndGetUserId(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static final class KeysCompat {
        private KeysCompat() {}

        static SecretKey hmacShaKey(String secret) {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            return io.jsonwebtoken.security.Keys.hmacShaKeyFor(bytes);
        }
    }
}
