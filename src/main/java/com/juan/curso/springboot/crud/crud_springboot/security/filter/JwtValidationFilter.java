package com.juan.curso.springboot.crud.crud_springboot.security.filter;

import static com.juan.curso.springboot.crud.crud_springboot.security.config.TokenJwtConfig.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.juan.curso.springboot.crud.crud_springboot.entities.ActiveToken;
import com.juan.curso.springboot.crud.crud_springboot.repositories.ActiveTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtValidationFilter extends BasicAuthenticationFilter {

    private final ActiveTokenRepository activeTokenRepository;

    public JwtValidationFilter(AuthenticationManager authenticationManager, ActiveTokenRepository activeTokenRepository) {
        super(authenticationManager);
        this.activeTokenRepository = activeTokenRepository;

    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader(HEADER_AUTHORIZATION);

        if (header == null || !header.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace(PREFIX_TOKEN, "");

        try {
            // Verificar si el token está presente en la base de datos
            if (!activeTokenRepository.existsByToken(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(CONTENT_TYPE);
                Map<String, String> body = new HashMap<>();
                body.put("error", "Token no encontrado");
                body.put("message", "Este token no está activo");
                response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                return;
            }

            // Obtener los datos del token
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
            String email = claims.getSubject();

            // Verificar la caducidad del token (exp en JWT)
            Date expirationDate = claims.getExpiration();
            if (expirationDate.before(new Date())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(CONTENT_TYPE);
                Map<String, String> body = new HashMap<>();
                body.put("error", "Token expirado");
                body.put("message", "El token ha expirado");
                response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                return;
            }

            // Verificar si el token ha expirado en la base de datos
            ActiveToken activeToken = activeTokenRepository.findByToken(token);
            if (activeToken != null && activeToken.getExpiresAt().before(new Date())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(CONTENT_TYPE);
                Map<String, String> body = new HashMap<>();
                body.put("error", "Token caducado");
                body.put("message", "Este token ha caducado");
                response.getWriter().write(new ObjectMapper().writeValueAsString(body));
                return;
            }

            // Si todo es válido, sigue con el proceso de autenticación
            Object authoritiesClaims = claims.get("authorities");
            List<String> authoritiesList = null;
            if (authoritiesClaims != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                authoritiesList = objectMapper.convertValue(authoritiesClaims, new TypeReference<List<String>>() {});
            }

            Collection<? extends GrantedAuthority> authorities = (authoritiesList != null && !authoritiesList.isEmpty())
                    ? authoritiesList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                    : List.of();

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);

        } catch (JwtException e) {
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            body.put("message", "Token inválido o expirado");

            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(CONTENT_TYPE);
        }
    }


}
