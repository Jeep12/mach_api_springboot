package com.juan.curso.springboot.crud.crud_springboot.exceptions;

public class EmailNotFoundException extends RuntimeException {
   
   
    public EmailNotFoundException(String email) {
        super("Usuario no encontrado con el email: " + email);
    }

}
