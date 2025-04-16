package com.juan.curso.springboot.crud.crud_springboot.dto;
import java.util.List;

public class UserRolesRequest {
    private Long id;
    private List<String> roles;  // Asumiendo que los roles vienen como lista de strings

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
