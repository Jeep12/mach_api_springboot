package com.juan.curso.springboot.crud.crud_springboot.services.users;

import java.util.*;
import java.util.stream.Collectors;

import com.juan.curso.springboot.crud.crud_springboot.dto.users.UserDto;
import com.juan.curso.springboot.crud.crud_springboot.entities.users.ActiveToken;
import com.juan.curso.springboot.crud.crud_springboot.repositories.ActiveTokenRepository;
import com.juan.curso.springboot.crud.crud_springboot.services.utils.EmailService;
import com.juan.curso.springboot.crud.crud_springboot.utils.EmailContent;
import com.juan.curso.springboot.crud.crud_springboot.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juan.curso.springboot.crud.crud_springboot.entities.users.Role;
import com.juan.curso.springboot.crud.crud_springboot.entities.users.User;
import com.juan.curso.springboot.crud.crud_springboot.utils.CodeGenerator;
import com.juan.curso.springboot.crud.crud_springboot.repositories.RoleRepository;
import com.juan.curso.springboot.crud.crud_springboot.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Value("${frontend.url.config}")
    private String urlFrontEnd;

    @Autowired
    private UserRepository userRepository;

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
        List<User> users = (List<User>) userRepository.findAll();

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
        if (userRepository.existsByEmail(user.getEmail())) {
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

        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token no válido o expirado"));

        if (user.getTokenExpiration().before(new Date())) {
            throw new RuntimeException("El código de verificación ha expirado. Por favor, solicita un nuevo código.");
        }

        // Si todo es válido, marcar al usuario como verificado
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);

        return emailContent.buildVerifyEmailBody();
    }

    public Map<String, Object> sendPasswordResetEmail(String email) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOptional = userRepository.findByEmail(email);

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

        userRepository.save(user);

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

        Optional<User> userOptional = userRepository.findByPasswordResetToken(tokenUser);

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
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Contraseña restablecida exitosamente.");
        return response;
    }

    @Override
    @Transactional
    public boolean toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if(user.isAdmin()){
            return false;
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        return user.isEnabled(); // Retorna el nuevo estado
    }

    public Map<String, String> deleteUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
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
            userRepository.delete(user);
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

        // Validación básica - debe venir al menos un rol
        if (roleNames == null || roleNames.isEmpty()) {
            response.put("success", false);
            response.put("message", "Debe proporcionar al menos un rol");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return response;
        }

        // Validar roles permitidos
        List<String> validRoles = Arrays.asList("ROLE_USER", "ROLE_EMPLOYEE");
        for (String roleName : roleNames) {
            if (!validRoles.contains(roleName)) {
                response.put("success", false);
                response.put("message", "Rol no válido. Solo se permiten ROLE_USER o ROLE_EMPLOYEE");
                response.put("status", HttpStatus.BAD_REQUEST.value());
                return response;
            }
        }

        // Asegurarse que ROLE_USER esté incluido
        if (!roleNames.contains("ROLE_USER")) {
            roleNames.add("ROLE_USER");
        }

        // Buscar usuario
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            response.put("status", HttpStatus.NOT_FOUND.value());
            return response;
        }
        User user = userOpt.get();

        // Verificar si ya tiene exactamente esos roles
        List<String> currentRoles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        if (new HashSet<>(currentRoles).equals(new HashSet<>(roleNames))) {
            response.put("success", false);
            response.put("message", "El usuario ya tiene los roles asignados");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return response;
        }

        // Manejo de relación bidireccional: eliminar roles viejos
        for (Role oldRole : user.getRoles()) {
            oldRole.getUsers().remove(user);
            roleRepository.save(oldRole);
        }
        user.getRoles().clear();

        // Asignar nuevos roles
        for (String roleName : roleNames) {
            Optional<Role> newRoleOpt = roleRepository.findByName(roleName);
            if (newRoleOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Rol " + roleName + " no encontrado en la base de datos");
                response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                return response;
            }
            Role role = newRoleOpt.get();
            user.getRoles().add(role);
            role.getUsers().add(user);
            roleRepository.save(role);
        }

        // Guardar cambios
        userRepository.save(user);

        // Eliminar tokens viejos
        activeTokenRepository.deleteOldTokensByUserId(user.getId());

        // Generar token nuevo
        String newToken = jwtTokenUtil.generateAccessToken(
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
        );

        ActiveToken activeToken = new ActiveToken();
        activeToken.setToken(newToken);
        activeToken.setUser(user);
        activeTokenRepository.save(activeToken);

        // Respuesta exitosa
        response.put("success", true);
        response.put("message", "Roles actualizados exitosamente");
        response.put("status", HttpStatus.OK.value());

        return response;
    }



    public Map<String, Object> resendRecoveryEmail(String email) {
        Map<String, Object> response = new HashMap<>();

        // Verificamos si el usuario existe

        System.out.println(email);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Si no existe, respondemos con un error
            System.out.println(userOptional);
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            response.put("expiresAt", 0);
            response.put("status", HttpStatus.NOT_FOUND.value());
            return response;
        }

        User user = userOptional.get();
        Date now = new Date();
        Date resetTokenExpiration = user.getPasswordResetExpiration();
        long expiresAt = 0;

        // Si tiene un token de recuperación ya, verificamos si ha expirado
        if (resetTokenExpiration != null) {
            expiresAt = resetTokenExpiration.getTime() - now.getTime();
            if (expiresAt < 0) expiresAt = 0; // Si ya expiró, lo dejamos en 0
        }

        // Si el token aún es válido, no podemos enviar otro
        if (resetTokenExpiration != null && resetTokenExpiration.after(now)) {
            response.put("success", false);
            response.put("message", "Ya se ha enviado un correo de recuperación. Espera a que expire.");
            response.put("expiresAt", expiresAt);
            response.put("status", HttpStatus.CONFLICT.value());
            return response;
        }

        // Generar nuevo token de recuperación
        String resetToken = CodeGenerator.generateVerificationCode();
        user.setPasswordResetToken(resetToken);

        // Establecer nueva expiración (por ejemplo 15 minutos)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        user.setPasswordResetExpiration(calendar.getTime());

        // Guardar el usuario con el nuevo token y expiración
        try {
            userRepository.save(user);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Hubo un error al guardar el usuario.");
            response.put("error", e.getMessage());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }

        // Componer el enlace de recuperación
        String url = this.urlFrontEnd + "/reset-password?token=";
        String resetLink = url + resetToken;

        // Aquí creas el email y envíaslo
        String subject = "Recuperación de Contraseña";
        String body = emailContent.buildPasswordResetEmailBody(resetLink, user.getName(), user.getLastname());

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
            response.put("success", true);
            response.put("message", "Se ha enviado el enlace de recuperación al correo.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Hubo un error al enviar el correo. Inténtelo más tarde.");
            response.put("error", e.getMessage()); // Opcional para debugging
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }

        // Se devuelve el token y la expiración
        response.put("token", resetToken);
        response.put("expiration", user.getPasswordResetExpiration());

        return response;
    }


}
