package com.juan.curso.springboot.crud.crud_springboot.security.filter;

import static com.juan.curso.springboot.crud.crud_springboot.security.config.TokenJwtConfig.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
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
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
            String email = claims.getSubject();

            // Obtener el campo "authorities" del token
            Object authoritiesClaims = claims.get("authorities");

            // Verificar si "authorities" es nulo o vacío antes de procesarlo
            List<String> authoritiesList = null;
            if (authoritiesClaims != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                authoritiesList = objectMapper.convertValue(authoritiesClaims, new TypeReference<List<String>>() {
                });
            }

            // Si authoritiesList no es null ni vacío, convertirla a GrantedAuthority
            Collection<? extends GrantedAuthority> authorities = (authoritiesList != null && !authoritiesList.isEmpty())
                    ? authoritiesList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                    : null;

            // Crear el objeto de autenticación
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    email, null, authorities != null ? authorities : List.of());

            // Establecer la autenticación en el contexto de seguridad
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
