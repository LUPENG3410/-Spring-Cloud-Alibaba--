package com.changxing.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtils {

    private static final String SECRET = "changxing-rental-jwt-secret-key-must-be-at-least-256-bits";
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000L;

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(getKey())
                .compact();
    }

    public static String generateToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(getKey())
                .compact();
    }

    public static String generateToken(String subject, long expireMs) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(getKey())
                .compact();
    }

    public static String generateToken(String subject, Map<String, Object> claims, long expireMs) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(getKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    public static <T> T getClaim(String token, String key, Class<T> type) {
        return parseToken(token).get(key, type);
    }

    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Token无效: {}", e.getMessage());
            return false;
        }
    }

    public static boolean isTokenValid(String token, String subject) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject().equals(subject) && !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Token无效: {}", e.getMessage());
            return false;
        }
    }
}
