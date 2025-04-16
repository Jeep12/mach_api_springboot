package com.juan.curso.springboot.crud.crud_springboot.services;

import java.util.List;
import java.util.Map;

import com.juan.curso.springboot.crud.crud_springboot.dto.UserDto;
import com.juan.curso.springboot.crud.crud_springboot.entities.User;

public interface UserService {

    List<UserDto> findAll();
    User save(User user);
    boolean existsByEmail(String email);
    String verifyEmail(String token);
    Map<String, Object> sendPasswordResetEmail(String email);
    Map<String, Object> resetPassword(String tokenUser,String password);
    boolean toggleUserStatus(Long id);
    Map<String, String> deleteUserById(Long id);
    Map<String, Object> updateUserRoles(Long id, List<String> roleNames);

}
