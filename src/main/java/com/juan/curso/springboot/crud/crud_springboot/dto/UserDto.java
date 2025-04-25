package com.juan.curso.springboot.crud.crud_springboot.dto;
import com.juan.curso.springboot.crud.crud_springboot.entities.users.Role;

import java.util.Date;
import java.util.List;

public class UserDto {

    private Long id;
    private boolean enabled;

    private String email;
    private String name;
    private String lastname;
    private boolean emailVerified;
    private Date createdAt;
    private List<Role> roles;

    public UserDto(Long id, List<Role> roles, Date createdAt, boolean emailVerified, String lastname, String name, String email, boolean enabled) {
        this.id = id;
        this.roles = roles;
        this.createdAt = createdAt;
        this.emailVerified = emailVerified;
        this.lastname = lastname;
        this.name = name;
        this.email = email;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
