package com.juan.curso.springboot.crud.crud_springboot.security.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.juan.curso.springboot.crud.crud_springboot.security.TokenJwtConfig.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
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
protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
        Authentication authResult) throws IOException, ServletException {

    // Obtener los detalles del usuario autenticado
    org.springframework.security.core.userdetails.User userDetails = 
    (org.springframework.security.core.userdetails.User) authResult.getPrincipal();

    
    String email = userDetails.getUsername();
    Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

    // Crear un mapa para los claims
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", email);
    claims.put("authorities", roles.stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .collect(Collectors.toList()));

    // Construir el token JWT
    String token = Jwts.builder()
            .subject(email) // Asignar el sujeto (usuario)
            .claims(claims)  // Agregar los claims
            .issuedAt(new Date()) // Fecha de emisión
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // Expira en 1 hora
            .signWith(SECRET_KEY) // Firmar con la clave secreta
            .compact(); // Generar el token

    // Agregar el token al encabezado de la respuesta
    response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + token);

    // Crear el cuerpo de la respuesta
    Map<String, String> body = new HashMap<>();
    body.put("token", token);
    body.put("email", email);
    body.put("message", String.format("Hola, has iniciado sesión con éxito con %s", email));

    // Escribir la respuesta en formato JSON
    response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    response.setContentType(CONTENT_TYPE);
    response.setStatus(200);
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
