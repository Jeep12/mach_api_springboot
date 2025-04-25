package com.juan.curso.springboot.crud.crud_springboot.controllers;

import java.util.*;

import com.juan.curso.springboot.crud.crud_springboot.dto.UserDto;
import com.juan.curso.springboot.crud.crud_springboot.dto.UserRequestDto;
import com.juan.curso.springboot.crud.crud_springboot.dto.UserRolesRequest;
import com.juan.curso.springboot.crud.crud_springboot.entities.users.ActiveToken;
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
import com.juan.curso.springboot.crud.crud_springboot.entities.users.User;
import com.juan.curso.springboot.crud.crud_springboot.services.utils.CaptchaService;
import com.juan.curso.springboot.crud.crud_springboot.services.users.UserService;
import com.juan.curso.springboot.crud.crud_springboot.utils.JwtTokenUtil;

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

        // creo un usuario con los objetos del DTO (request)
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

        //Obtengo el refresh token de la request
        String refreshToken = request.get("refreshToken");

        //si es nulo o esta vacio mando bad request
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        try {
            Claims claims = jwtTokenUtil.parseToken(refreshToken);

            // Verifico que sea un refresh token (en el payload esta el token_type)
            if (!"refresh".equals(claims.get("token_type"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token type");
            }

            //Obtengo el email del body
            String email = claims.getSubject();

            // Obtengo los roles del usuario
            List<String> roles = roleRepository.findRolesByEmail(email); // O tu método para obtener roles

            // Genero un nuevo access token con los roles
            String newAccessToken = jwtTokenUtil.generateAccessToken(email, roles);

            // Elimino el viejo token de la tabla active tokens
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                activeTokenRepository.deleteOldTokensByUserId(optionalUser.get().getId());
            }

            // Guardo el nuevo access token en la tabla active_tokens
            ActiveToken newActiveToken = new ActiveToken();
            Optional<User> user = userRepository.findByEmail(email);  // Obtienes al usuario por su email
            if (user.isPresent()) {
                newActiveToken.setToken(newAccessToken);  // Guardar el nuevo access token
                newActiveToken.setUser(user.get());
                newActiveToken.setCreatedAt(new Date());
                newActiveToken.setExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION));  // Establecer su fecha de expiración
                activeTokenRepository.save(newActiveToken);  // Guarda el nuevo access token
            }

            //Genero nueva respuesta con el nuevo token
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

        // Obtengo los valores del Map
        String tokenUser = request.get("tokenUser");
        String password = request.get("password");

        boolean isCaptchaValid = captchaService.verifyCaptcha(captchaToken);
        if (!isCaptchaValid) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Captcha inválido o ya usado.");
            return ResponseEntity.badRequest().body(response);
        }

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

    @PostMapping("/resend-recovery-email")
    public ResponseEntity<Map<String, Object>> resendRecoveryEmail(
            @RequestBody Map<String, String> request,
            @RequestParam("captchaToken") String captchaToken) {

        if (!captchaService.verifyCaptcha(captchaToken)) {
            Map<String, Object> captchaError = new HashMap<>();
            captchaError.put("success", false);
            captchaError.put("message", "Captcha validation failed.");
            captchaError.put("expiresAt", 0);
            captchaError.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(captchaError);
        }


        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "El email es requerido.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        email = email.trim();

        // Llamada al servicio
        Map<String, Object> response = service.resendRecoveryEmail(email);

        // Validación de la respuesta del servicio
        if (response.get("success") == null || !(Boolean) response.get("success")) {
            return ResponseEntity.status(HttpStatus.valueOf((Integer) response.get("status")))
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }


}
