package com.babalegends.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateJwtToken(Authentication authentication) {
        UserPrincipal principal = Objects.requireNonNull((UserPrincipal) authentication.getPrincipal());
        return buildJwtToken(principal.getId().toString());
    }

    public boolean isTokenValid(String jwtToken) {
        try {
            getClaims(jwtToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserId(String jwtToken) {
        return UUID.fromString(getClaims(jwtToken).getSubject());
    }

    private String buildJwtToken(String userId) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expirationTime)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims getClaims(String jwtToken) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }
}
