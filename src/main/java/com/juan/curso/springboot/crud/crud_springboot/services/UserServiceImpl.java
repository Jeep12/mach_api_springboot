package com.juan.curso.springboot.crud.crud_springboot.services;

import java.util.*;
import java.util.stream.Collectors;

import com.juan.curso.springboot.crud.crud_springboot.dto.UserDto;
import com.juan.curso.springboot.crud.crud_springboot.entities.ActiveToken;
import com.juan.curso.springboot.crud.crud_springboot.repositories.ActiveTokenRepository;
import com.juan.curso.springboot.crud.crud_springboot.utils.EmailContent;
import com.juan.curso.springboot.crud.crud_springboot.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juan.curso.springboot.crud.crud_springboot.entities.Role;
import com.juan.curso.springboot.crud.crud_springboot.entities.User;
import com.juan.curso.springboot.crud.crud_springboot.utils.CodeGenerator;
import com.juan.curso.springboot.crud.crud_springboot.repositories.RoleRepository;
import com.juan.curso.springboot.crud.crud_springboot.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Value("${frontend.url.config}")
    private String urlFrontEnd;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailContent emailContent;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ActiveTokenRepository activeTokenRepository;


    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        List<User> users = (List<User>) repository.findAll();

        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            UserDto userDto = new UserDto(user.getId(), user.getRoles(), user.getCreatedAt(), user.isEmailVerified(),
                    user.getLastname(), user.getName(), user.getEmail(), user.isEnabled());
            userDtos.add(userDto);
        }

        return userDtos;
    }

    @Override
    @Transactional
    public User save(User user) {
        // Validar email único
        if (repository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // Generar código de verificación
        String verificationCode = CodeGenerator.generateVerificationCode();
        user.setVerificationToken(verificationCode);

        // Establecer expiración (15 minutos)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        user.setTokenExpiration(calendar.getTime());

        // Asignar roles
        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
        List<Role> roles = new ArrayList<>();
        optionalRoleUser.ifPresent(roles::add);

        if (user.isAdmin()) {
            Optional<Role> optionalRoleAdmin = roleRepository.findByName("ROLE_ADMIN");
            optionalRoleAdmin.ifPresent(roles::add);
        }
        user.setRoles(roles);

        // Encriptar contraseña (aquí como querías)
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Enviar email de verificación
        String subject = "Verificación de correo electrónico";
        String body = emailContent.buildVerificationEmailBody(
                verificationCode,
                user.getName(),
                user.getLastname());
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

        return emailContent.buildVerifyEmailBody();
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
                response.put("message",
                        "Ya se ha enviado un enlace de recuperación recientemente. Espere a que expire.");
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

        String url = this.urlFrontEnd + "/reset-password?token=";
        String resetLink = url + resetToken;
        System.out.println(resetLink);
        String subject = "Recuperación de Contraseña";
        String body = emailContent.buildPasswordResetEmailBody(resetLink, user.getName(), user.getLastname());

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

    @Override
    @Transactional
    public boolean toggleUserStatus(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setEnabled(!user.isEnabled());
        repository.save(user);

        return user.isEnabled(); // Retorna el nuevo estado
    }

    public Map<String, String> deleteUserById(Long id) {
        Optional<User> userOpt = repository.findById(id);
        Map<String, String> response = new HashMap<>();
        System.out.println("Entro al servicio");

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Usuario encontrado: " + user);

            // Verificar si el usuario tiene el rol de administrador
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

            if (isAdmin) {
                System.out.println("No se puede eliminar el usuario admin: " + user.getName());
                response.put("message", "No se puede eliminar un usuario administrador.");
                return response;
            }

            // Si no es administrador, proceder con la eliminación
            repository.delete(user);
            response.put("message", "Usuario eliminado exitosamente.");
            return response;
        }

        // Si el usuario no existe
        response.put("message", "Usuario no encontrado.");
        return response;
    }

    @Transactional
    public Map<String, Object> updateUserRoles(Long id, List<String> roleNames) {
        Map<String, Object> response = new HashMap<>();

        // Validación básica - debe venir exactamente un rol
        if (roleNames == null || roleNames.size() != 1) {
            response.put("success", false);
            response.put("message", "Debe proporcionar exactamente un rol");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return response;
        }

        String newRoleName = roleNames.get(0);

        // Validar roles permitidos
        if (!"ROLE_USER".equals(newRoleName) && !"ROLE_ADMIN".equals(newRoleName)) {
            response.put("success", false);
            response.put("message", "Rol no válido. Solo se permiten ROLE_USER o ROLE_ADMIN");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return response;
        }

        // Buscar usuario
        Optional<User> userOpt = repository.findById(id);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return response;
        }
        User user = userOpt.get();

        // Verificar si ya tiene el rol solicitado
        Optional<String> currentRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName);

        if (currentRole.isPresent() && currentRole.get().equals(newRoleName)) {
            response.put("success", false);
            response.put("message", "El usuario ya tiene el rol " + newRoleName);
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return response;
        }

        // Obtener nuevo rol
        Optional<Role> newRoleOpt = roleRepository.findByName(newRoleName);
        if (newRoleOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Rol no encontrado en la base de datos");
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }
        Role newRole = newRoleOpt.get();

        // Manejo de la relación bidireccional
        if (!user.getRoles().isEmpty()) {
            Role oldRole = user.getRoles().get(0);
            oldRole.getUsers().remove(user);
            roleRepository.save(oldRole);
        }

        // Actualizar roles
        user.getRoles().clear();
        user.getRoles().add(newRole);
        newRole.getUsers().add(user);

        // Guardar cambios
        roleRepository.save(newRole);
        repository.save(user);

        // Eliminar el token antiguo de la tabla ActiveToken
        activeTokenRepository.deleteOldTokensByUserId(user.getId());  // Elimina los tokens viejos del usuario

        // Generar un nuevo token JWT con los roles actualizados
        String newToken = jwtTokenUtil.generateAccessToken(user.getEmail(), user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

        // Crear el nuevo ActiveToken para el usuario con el nuevo token generado
        ActiveToken activeToken = new ActiveToken();
        activeToken.setToken(newToken);
        activeToken.setUser(user);

        // Guardar el nuevo token en la base de datos
        activeTokenRepository.save(activeToken);

        // Respuesta exitosa
        response.put("success", true);
        response.put("message", "Rol actualizado exitosamente");
        response.put("status", HttpStatus.OK.value());

        return response;
    }


}
