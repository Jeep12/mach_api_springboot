package com.juan.curso.springboot.crud.crud_springboot.services.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String username;

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            // Crear el mensaje MIME
            MimeMessage message = emailSender.createMimeMessage();

            // Crear el helper para configurar el mensaje (true para que sea en formato HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Establecer el remitente, destinatario, asunto y cuerpo del mensaje
            helper.setFrom(username);
            helper.setTo(to);
            helper.setSubject(subject);

            // Establecer el cuerpo del mensaje como HTML (true indica que el cuerpo es HTML)
            helper.setText(body, true);

            // Enviar el mensaje
            emailSender.send(message);
            System.out.println("Correo enviado exitosamente");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
