package com.juan.curso.springboot.crud.crud_springboot.security.filter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.juan.curso.springboot.crud.crud_springboot.entities.ActiveToken;
import com.juan.curso.springboot.crud.crud_springboot.repositories.ActiveTokenRepository;
import com.juan.curso.springboot.crud.crud_springboot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juan.curso.springboot.crud.crud_springboot.entities.User;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.juan.curso.springboot.crud.crud_springboot.security.config.TokenJwtConfig.*;


public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    private final ActiveTokenRepository activeTokenRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ActiveTokenRepository activeTokenRepository,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.activeTokenRepository = activeTokenRepository;
        this.userRepository = userRepository;
        setFilterProcessesUrl("/login"); // Configura la URL de login
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        User user = null;
        String email = null;
        String password = null;

        try {
            user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            email = user.getEmail();
            password = user.getPassword();
        } catch (StreamReadException e) {
            e.printStackTrace();
        } catch (DatabindException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,
                password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {

        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authResult.getPrincipal();

        String email = userDetails.getUsername();
        Optional<User> optionalUser = userRepository.findByEmail(email);  // Devuelve un Optional<User>

        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        // Access token
        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put("email", email);
        accessClaims.put("authorities", roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        accessClaims.put("token_type", "access");

        String accessToken = Jwts.builder()
                .subject(email)
                .claims(accessClaims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();

        // Refresh token (más simple, sin roles)
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put("token_type", "refresh");

        String refreshToken = Jwts.builder()
                .subject(email)
                .claims(refreshClaims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();

        ActiveToken activeToken = new ActiveToken();

        if (optionalUser.isPresent()) {
            activeTokenRepository.deleteOldTokensByUserId(optionalUser.get().getId());

            activeToken.setToken(accessToken);
            activeToken.setUser(optionalUser.get());
            activeToken.setCreatedAt(new Date());  // Establecer la fecha de creación
            activeToken.setExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION));
            activeTokenRepository.save(activeToken);  // Guardamos el ActiveToken con el usuario asociado


        } else {
            // Maneja el caso donde el usuario no está presente (por ejemplo, devolver un error)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Usuario no encontrado.");
            return;
        }


        // Respuesta
        Map<String, String> body = new HashMap<>();
        body.put("access_token", accessToken);
        body.put("refresh_token", refreshToken);
        body.put("email", email);
        body.put("message", "Autenticación exitosa");

        response.setContentType(CONTENT_TYPE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + accessToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        Map<String, String> body = new HashMap<>();
        body.put("message", "No podemos iniciar sesión. Verifica tu email o contraseña e intenta nuevamente.");
        body.put("error", failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(401);
        response.setContentType(CONTENT_TYPE);

    }

}
