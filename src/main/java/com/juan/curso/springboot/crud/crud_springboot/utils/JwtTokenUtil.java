package com.juan.curso.springboot.crud.crud_springboot.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static com.juan.curso.springboot.crud.crud_springboot.security.config.TokenJwtConfig.*;

@Component
public class JwtTokenUtil {

    public String extractEmail(String token) {
        return parseToken(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    private Date getExpirationDate(String token) {
        return parseToken(token).getExpiration();
    }

    public String generateAccessToken(String email, Collection<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("authorities", roles); // Incluir roles en el token
        claims.put("token_type", "access");

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}