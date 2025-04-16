package com.juan.curso.springboot.crud.crud_springboot.controllers;

import java.util.*;

import com.juan.curso.springboot.crud.crud_springboot.dto.UserDto;
import com.juan.curso.springboot.crud.crud_springboot.dto.UserRequestDto;
import com.juan.curso.springboot.crud.crud_springboot.dto.UserRolesRequest;
import com.juan.curso.springboot.crud.crud_springboot.entities.ActiveToken;
import com.juan.curso.springboot.crud.crud_springboot.repositories.ActiveTokenRepository;
import com.juan.curso.springboot.crud.crud_springboot.repositories.RoleRepository;
import com.juan.curso.springboot.crud.crud_springboot.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.juan.curso.springboot.crud.crud_springboot.dto.PasswordResetRequestDto;
import com.juan.curso.springboot.crud.crud_springboot.entities.User;
import com.juan.curso.springboot.crud.crud_springboot.services.CaptchaService;
import com.juan.curso.springboot.crud.crud_springboot.services.UserService;
import com.juan.curso.springboot.crud.crud_springboot.utils.JwtTokenUtil;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;

import static com.juan.curso.springboot.crud.crud_springboot.security.config.TokenJwtConfig.ACCESS_TOKEN_EXPIRATION;

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
    private ActiveTokenRepository activeTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<UserDto> list() {
        return service.findAll();
    }


    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDto userRequestDto,
                                      BindingResult result,
                                      @RequestParam("captchaToken") String captchaToken) {

        // Validar CAPTCHA primero
        if (!captchaService.verifyCaptcha(captchaToken)) {
            return ResponseEntity.badRequest().body("Captcha validation failed.");
        }

        if (result.hasErrors()) {
            return validation(result);
        }

        // Convertir DTO a Entidad
        User user = new User();
        user.setName(userRequestDto.getName());
        user.setLastname(userRequestDto.getLastname());
        user.setEmail(userRequestDto.getEmail());
        user.setPassword(userRequestDto.getPassword()); // La encriptación se hará en el servicio
        user.setAdmin(false); // Valor por defecto

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

            // Eliminar el viejo access token del usuario (si lo deseas)
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                activeTokenRepository.deleteOldTokensByUserId(optionalUser.get().getId());
            }

            // Guardar el nuevo access token en la base de datos
            ActiveToken newActiveToken = new ActiveToken();
            Optional<User> user = userRepository.findByEmail(email);  // Obtienes al usuario por su email
            if (user.isPresent()) {
                newActiveToken.setToken(newAccessToken);  // Guardar el nuevo access token
                newActiveToken.setUser(user.get());
                newActiveToken.setCreatedAt(new Date());
                newActiveToken.setExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION));  // Establecer su fecha de expiración
                activeTokenRepository.save(newActiveToken);  // Guarda el nuevo access token
            }

            // Crear respuesta con el nuevo access token
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", refreshToken);  // Mantener el mismo refresh token, si el frontend lo necesita

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


    @PutMapping("/toggle-status")
    public ResponseEntity<Map<String, String>> toggleUserStatus(@RequestBody Map<String, Long> request) {
        Long userId = request.get("id");
        boolean enabled = service.toggleUserStatus(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", enabled ? "Usuario habilitado" : "Usuario deshabilitado");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<Map<String, String>> deleteUserById(@RequestBody Map<String, Long> request) {
        Long userId = request.get("id");

        // Llamar al servicio para eliminar el usuario
        Map<String, String> response = service.deleteUserById(userId);

        // Siempre devolver 200 OK, el mensaje varía según el resultado
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-roles")
    public ResponseEntity<Map<String, Object>> updateUserRoles(
            @RequestBody UserRolesRequest request,
            HttpServletRequest httpRequest) {


        Map<String, Object> response = service.updateUserRoles(request.getId(), request.getRoles());

        return ResponseEntity.status((Integer) response.get("status")).body(response);
    }

}
