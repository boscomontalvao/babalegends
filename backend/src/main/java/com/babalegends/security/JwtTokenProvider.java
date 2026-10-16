package com.babalegends.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateJwtToken(Authentication authentication) {
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return buildJwtToken(user.getUsername());
    }

    public boolean isTokenValid(String jwtToken) {
        try {
            getClaims(jwtToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserName(String jwtToken) {
        return getClaims(jwtToken).getSubject();
    }

    private String buildJwtToken(String userName) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userName)
                .issuedAt(now)
                .expiration(expirationTime)
                .signWith(getSingingKey())
                .compact();
    }

    private SecretKey getSingingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private Claims getClaims(String jwtToken) {
        return Jwts.parser()
                .verifyWith(getSingingKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }
}
