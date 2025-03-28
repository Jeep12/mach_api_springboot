package com.juan.curso.springboot.crud.crud_springboot.services;

import org.springframework.stereotype.Service;

@Service
public class EmailContentService {

    public String buildPasswordResetEmailBody(String resetLink, String name, String lastname) {
        StringBuilder body = new StringBuilder();
        body.append("<html><body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">")
                .append("<div style=\"width: 100%; padding: 20px 0;\">")
                .append("<div style=\"width: 80%; max-width: 600px; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); margin: auto; text-align: center;\">")
                .append("<h2 style=\"color:#4CAF50;\">¡Hola, ").append(name).append(" ").append(lastname).append("!</h2>")
                .append("<p>Hemos recibido una solicitud para restablecer tu contraseña. Si fuiste tú, por favor haz clic en el siguiente botón para proceder.</p>")
                .append("<a href=\"").append(resetLink).append("\" style=\"font-weight: 700; border-radius: 5px; width: 200px; padding: 10px; display: block; margin: auto; text-transform: uppercase; background-color: #4CAF50; text-align: center; color: white; text-decoration: none;\">Restablecer contraseña</a>")
                .append("<p style=\"font-size: 14px; color: #555; margin-top: 15px;\">Este enlace es válido por 15 minutos. Si no lo solicitaste, por favor ignóralo.</p>")
                .append("<p style=\"font-size: 14px; color: #555; margin-top: 10px;\">Si no reconoces esta solicitud, tu cuenta no se verá afectada. El enlace solo funcionará durante 15 minutos.</p>")
                .append("<p style=\"font-size: 14px; color: #555; margin-top: 15px;\">Si tienes problemas para restablecer tu contraseña, contacta con soporte.</p>")
                .append("</div></div>")
                .append("</body></html>");

        return body.toString();
    }



    public String buildVerificationEmailBody(String verificationCode, String name, String lastname) {
        StringBuilder body = new StringBuilder();
        body.append(
                        "<html><body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">")
                .append("<div style=\"width: 100%; padding: 20px 0;\">")
                .append("<div style=\"width: 80%; max-width: 600px; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); margin: auto; text-align: center;\">")
                .append("<h2 style=\"color:#4CAF50;\">¡Hola, ")
                .append(name).append(" ").append(lastname)
                .append("!</h2>")
                .append("<p>Gracias por registrarte en <strong>Technology DLG</strong>. Antes de comenzar, necesitamos confirmar que eres tú. </p>")
                .append("<p>Haz clic en el botón a continuación para verificar tu correo electrónico.</p>")
                .append("<a href=\"http://localhost:8080/api/users/verifyEmail?token=")
                .append(verificationCode)
                .append("\" style=\"font-weight: 700; border-radius: 5px; width: 200px; padding: 10px; display: block; margin: auto; text-transform: uppercase; background-color: #4CAF50; text-align: center; color: white; text-decoration: none;\">Verificar mi correo</a>")
                .append("<p>Si no solicitaste este correo, simplemente ignóralo.</p>")
                .append("</div></div>")
                .append("</body></html>");

        return body.toString();
    }

    public String buildVerifyEmailBody (){
        StringBuilder htmlContent = new StringBuilder();

        htmlContent.append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; margin: 0; height: 100vh; display: flex; justify-content: center; align-items: center; }")
                .append(".container { background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); width: 100%; max-width: 600px; text-align: center; }")
                .append(".header { color: #4CAF50; font-size: 24px; font-weight: bold; margin-bottom: 20px; }")

                .append(".message { font-size: 18px; color: #333333; margin-top: 20px; }")
                .append(".button { display: inline-block; background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; margin-top: 20px; font-weight: bold; }")
                .append(".footer { font-size: 14px; color: #777777; margin-top: 30px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>¡Correo Verificado Exitosamente!</div>")
                .append("<div class='message'>")
                .append("<p>¡Felicidades! Tu correo ha sido verificado con éxito. Ahora puedes disfrutar de todos los servicios.</p>")
                .append("<a href='http://localhost:4200/' class='button'>Ir al sitio</a>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Si no realizaste esta acción, por favor contacta a nuestro soporte.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return htmlContent.toString();

    }


}
