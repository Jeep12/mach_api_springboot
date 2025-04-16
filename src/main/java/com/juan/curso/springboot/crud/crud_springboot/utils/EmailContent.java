package com.juan.curso.springboot.crud.crud_springboot.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailContent {

    @Value("${server.url.config}")
    private String urlBackEnd;

    @Value("${frontend.url.config}")
    private String urlFrontEnd;

    public String buildPasswordResetEmailBody(String resetLink, String name, String lastname) {
        StringBuilder body = new StringBuilder();
        body.append("<html><body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">")
                .append("<div style=\"width: 100%; padding: 20px 0;\">")
                .append("<div style=\"width: 80%; max-width: 600px; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); margin: auto; text-align: center;\">")
                .append("<h2 style=\"color:#4CAF50;\">¡Hola, ").append(name).append(" ").append(lastname).append("!</h2>")
                .append("<p>Hemos recibido una solicitud para restablecer tu contraseña. Si fuiste tú, haz clic en el botón.</p>")
                .append("<a href=\"").append(resetLink).append("\" style=\"font-weight: 700; border-radius: 5px; width: 200px; padding: 10px; display: block; margin: auto; text-transform: uppercase; background-color: #4CAF50; text-align: center; color: white; text-decoration: none;\">Restablecer contraseña</a>")
                .append("<p style=\"font-size: 14px; color: #555; margin-top: 15px;\">Este enlace es válido por 15 minutos. Si no lo solicitaste, ignóralo.</p>")
                .append("</div></div></body></html>");

        return body.toString();
    }

    public String buildVerificationEmailBody(String verificationCode, String name, String lastname) {
        StringBuilder body = new StringBuilder();
        body.append("<html><body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">")
                .append("<div style=\"width: 100%; padding: 20px 0;\">")
                .append("<div style=\"width: 80%; max-width: 600px; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); margin: auto; text-align: center;\">")
                .append("<h2 style=\"color:#4CAF50;\">¡Hola, ").append(name).append(" ").append(lastname).append("!</h2>")
                .append("<p>Gracias por registrarte en <strong>Technology DLG</strong>. Para confirmar tu cuenta, haz clic en el botón.</p>")
                .append("<a href=\"").append(urlBackEnd).append("/api/users/verifyEmail?token=").append(verificationCode)
                .append("\" style=\"font-weight: 700; border-radius: 5px; width: 200px; padding: 10px; display: block; margin: auto; text-transform: uppercase; background-color: #4CAF50; text-align: center; color: white; text-decoration: none;\">Verificar mi correo</a>")
                .append("<p>Si no solicitaste este correo, ignóralo.</p></div></div></body></html>");

        return body.toString();
    }

    public String buildVerifyEmailBody() {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><style>")
                .append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; margin: 0; height: 100vh; display: flex; justify-content: center; align-items: center; }")
                .append(".container { background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); width: 100%; max-width: 600px; text-align: center; }")
                .append(".header { color: #4CAF50; font-size: 24px; font-weight: bold; margin-bottom: 20px; }")
                .append(".message { font-size: 18px; color: #333333; margin-top: 20px; }")
                .append(".button { display: inline-block; background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; margin-top: 20px; font-weight: bold; }")
                .append(".footer { font-size: 14px; color: #777777; margin-top: 30px; }")
                .append("</style></head><body><div class='container'>")
                .append("<div class='header'>¡Correo Verificado Exitosamente!</div>")
                .append("<div class='message'><p>¡Felicidades! Tu correo ha sido verificado con éxito.</p>")
                .append("<a href='").append(urlFrontEnd).append("' class='button'>Ir al sitio</a></div>")
                .append("<div class='footer'><p>Si no realizaste esta acción, contacta a soporte.</p></div></div></body></html>");

        return htmlContent.toString();
    }
}
