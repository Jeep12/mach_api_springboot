package com.juan.curso.springboot.crud.crud_springboot.security.config;

import java.util.Arrays;

import com.juan.curso.springboot.crud.crud_springboot.repositories.ActiveTokenRepository;
import com.juan.curso.springboot.crud.crud_springboot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.juan.curso.springboot.crud.crud_springboot.security.filter.JwtAuthenticationFilter;
import com.juan.curso.springboot.crud.crud_springboot.security.filter.JwtValidationFilter;

@Configuration
//@EnableMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig {

    @Value("${frontend.url.config}")
    private String url;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private ActiveTokenRepository activeTokenRepository;

    @Autowired
    private UserRepository userRepository;


    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authz) -> authz
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("ADMIN","EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/login").permitAll() // Permite acceso a /login
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/delete-user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/refresh-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/toggle-status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/change-roles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/resend-recovery-email").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/verifyEmail").permitAll() 
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/category").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/category").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/category").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/category").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), activeTokenRepository, userRepository))
                .addFilter(new JwtValidationFilter(authenticationManager(),activeTokenRepository))
                .csrf(config -> config.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Especificar los orígenes permitidos explícitamente (mejor que "*")
        config.setAllowedOrigins(Arrays.asList(
                this.url
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cabeceras permitidas
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        // Cabeceras expuestas al cliente
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // Permitir credenciales (necesario para cookies/tokens)
        config.setAllowCredentials(true);

        // Tiempo máximo de caché para preflight requests
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }


    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> corsBean = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource())
        );
        corsBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return corsBean;
    }


}
