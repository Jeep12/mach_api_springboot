package com.juan.curso.springboot.crud.crud_springboot.services;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juan.curso.springboot.crud.crud_springboot.entities.Role;
import com.juan.curso.springboot.crud.crud_springboot.entities.User;
import com.juan.curso.springboot.crud.crud_springboot.helpers.CodeGenerator;
import com.juan.curso.springboot.crud.crud_springboot.repositories.RoleRepository;
import com.juan.curso.springboot.crud.crud_springboot.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailContentService emailContentService;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return (List<User>) repository.findAll();
    }


    @Override
    @Transactional
    public User save(User user) {

        if (repository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // Generar código de verificación alfanumérico
        String verificationCode = CodeGenerator.generateVerificationCode();
        user.setVerificationToken(verificationCode);

        // Establecer la expiración del token a 15 minutos a partir de ahora
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15); // Agregar 15 minutos
        user.setTokenExpiration(calendar.getTime());

        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
        List<Role> roles = new ArrayList<>();

        optionalRoleUser.ifPresent(role -> roles.add(role));

        if (user.isAdmin()) {
            Optional<Role> optionalRoleAdmin = roleRepository.findByName("ROLE_ADMIN");
            optionalRoleAdmin.ifPresent(role -> roles.add(role));
        }

        user.setRoles(roles);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String subject = "Verificación de correo electrónico";
        String body = emailContentService.buildVerificationEmailBody(verificationCode, user.getName(), user.getLastname());

        emailService.sendEmail(user.getEmail(), subject, body);

        return repository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public String verifyEmail(String token) {
        User user = repository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token no válido o expirado"));

        if (user.getTokenExpiration().before(new Date())) {
            throw new RuntimeException("El código de verificación ha expirado. Por favor, solicita un nuevo código.");
        }

        // Si todo es válido, marcar al usuario como verificado
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiration(null);
        repository.save(user);


        return emailContentService.buildVerifyEmailBody();
    }


    public Map<String, Object> sendPasswordResetEmail(String email) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOptional = repository.findByEmail(email);

        if (!userOptional.isPresent()) {
            response.put("success", false);
            response.put("message", "No se encontró un usuario con ese correo electrónico.");
            return response;
        }

        User user = userOptional.get();
        Date now = new Date();

        // Verificar si ya tiene un token válido
        if (user.getPasswordResetToken() != null && user.getPasswordResetExpiration() != null) {
            if (user.getPasswordResetExpiration().after(now)) {
                response.put("success", false);
                response.put("message", "Ya se ha enviado un enlace de recuperación recientemente. Espere a que expire.");
                return response;
            }
        }

        // Generar nuevo token
        String resetToken = CodeGenerator.generateVerificationCode();
        user.setPasswordResetToken(resetToken);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        user.setPasswordResetExpiration(calendar.getTime());

        repository.save(user);

        String url = "http://localhost:4200/reset-password?token=";
        String resetLink = url + resetToken;

        String subject = "Recuperación de Contraseña";
        String body = emailContentService.buildPasswordResetEmailBody(resetLink, user.getName(), user.getLastname());

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
            response.put("success", true);
            response.put("message", "Se ha enviado el enlace de recuperación al correo.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Hubo un error al enviar el correo. Inténtelo más tarde.");
            response.put("error", e.getMessage()); // Opcional, solo para debug
            return response;
        }

        response.put("token", resetToken); // Opcional, solo para debug
        response.put("expiration", user.getPasswordResetExpiration());

        return response;
    }


    @Override
    public Map<String, Object> resetPassword(String tokenUser, String password) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOptional = repository.findByPasswordResetToken(tokenUser);

        if (!userOptional.isPresent()) {
            response.put("success", false);
            response.put("message", "Token inválido o no encontrado.");
            return response;
        }

        User user = userOptional.get();

        // Validar si el token ya expiró
        Date now = new Date();
        if (user.getPasswordResetExpiration() == null || user.getPasswordResetExpiration().before(now)) {
            response.put("success", false);
            response.put("message", "El token de recuperación ha expirado.");
            return response;
        }

        // Encriptar la nueva contraseña
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        // Eliminar el token y la fecha de expiración
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiration(null);

        // Guardar el usuario con la nueva contraseña
        repository.save(user);

        response.put("success", true);
        response.put("message", "Contraseña restablecida exitosamente.");
        return response;
    }
}
