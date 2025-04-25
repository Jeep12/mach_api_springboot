package com.juan.curso.springboot.crud.crud_springboot.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.juan.curso.springboot.crud.crud_springboot.entities.users.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByVerificationToken(String verificationToken);


    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);


    Optional<User> findByPasswordResetToken(String tokenUser);
} 
