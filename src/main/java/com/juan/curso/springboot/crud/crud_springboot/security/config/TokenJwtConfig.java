package com.juan.curso.springboot.crud.crud_springboot.security.config;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;

public class TokenJwtConfig {

    public static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
    // Tiempos de expiración en milisegundos
    public static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1 hora
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 días
}
