package com.juan.curso.springboot.crud.crud_springboot.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.juan.curso.springboot.crud.crud_springboot.repositories.RoleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juan.curso.springboot.crud.crud_springboot.dto.PasswordResetRequestDto;
import com.juan.curso.springboot.crud.crud_springboot.entities.User;
import com.juan.curso.springboot.crud.crud_springboot.services.CaptchaService;
import com.juan.curso.springboot.crud.crud_springboot.services.UserService;
import  com.juan.curso.springboot.crud.crud_springboot.security.JwtTokenUtil;

import jakarta.validation.Valid;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<User> list() {
        return service.findAll();
    }



    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result) {

        if (result.hasFieldErrors()) {
            return validation(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult result,
                                      @RequestParam("captchaToken") String captchaToken) {

        boolean isCaptchaValid = captchaService.verifyCaptcha(captchaToken);

        if (!isCaptchaValid) {
            return ResponseEntity.badRequest().body("Captcha validation failed.");
        }

        if (result.hasErrors()) {
            return validation(result);
        }

        user.setAdmin(false);
        return create(user, result);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request,
                                          HttpServletResponse response) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        try {
            Claims claims = jwtTokenUtil.parseToken(refreshToken);

            // Verificar que sea un refresh token
            if (!"refresh".equals(claims.get("token_type"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token type");
            }

            String email = claims.getSubject();

            // Obtener los roles del usuario de la base de datos (puedes usar el roleRepository)
            List<String> roles = roleRepository.findRolesByEmail(email); // O tu método para obtener roles

            // Generar el nuevo access token con los roles
            String newAccessToken = jwtTokenUtil.generateAccessToken(email, roles);

            // Opcionalmente generar un nuevo refresh token
            // String newRefreshToken = jwtTokenUtil.generateRefreshToken(email);

            // Crear respuesta con el nuevo access token (y el refresh token si lo generas)
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            // tokens.put("refreshToken", newRefreshToken); // Si decides incluir el refresh token

            return ResponseEntity.ok(tokens);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid refresh token");
        }
    }



    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token) {
        return service.verifyEmail(token);
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword2(
            @RequestBody PasswordResetRequestDto request,
            @RequestParam("captchaToken") String captchaToken) {

        // Verificar captcha
        boolean isCaptchaValid = captchaService.verifyCaptcha(captchaToken);
        if (!isCaptchaValid) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Captcha inválido o ya usado.");
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, Object> response = service.sendPasswordResetEmail(request.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam("captchaToken") String captchaToken,
            @RequestBody Map<String, String> request) {

        // Obtener los valores del Map
        String tokenUser = request.get("tokenUser");
        String password = request.get("password");
        System.out.println("\n");
        System.out.println("password" +password);
        System.out.println("tokenUser" +tokenUser);
        // Verificar captcha
        boolean isCaptchaValid = captchaService.verifyCaptcha(captchaToken);
        if (!isCaptchaValid) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Captcha inválido o ya usado.");
            return ResponseEntity.badRequest().body(response);
        }

        // Llamar al servicio para restablecer la contraseña
        Map<String, Object> response = service.resetPassword(tokenUser, password);

        return ResponseEntity.ok(response);
    }


}
