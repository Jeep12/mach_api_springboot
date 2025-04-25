package com.juan.curso.springboot.crud.crud_springboot.services.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class CaptchaService {

    private final RestTemplate restTemplate;

    @Value("${spring.recaptcha.secret.key}")
    private String recaptchaSecretKey;

    private static final String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";

    public CaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("null")
    public boolean verifyCaptcha(String token) {
        // Parámetros de la solicitud POST
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", recaptchaSecretKey);
        body.add("response", token);

        // Configuración de la solicitud HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        // Enviar la solicitud POST a Google
        ResponseEntity<String> response = restTemplate.exchange(RECAPTCHA_URL, HttpMethod.POST, entity, String.class);

        // Aquí puedes procesar la respuesta de Google, que está en formato JSON
        // Por ejemplo, si la respuesta contiene "success": true, significa que el captcha fue válido
        return response.getBody().contains("\"success\": true");
    }
}
