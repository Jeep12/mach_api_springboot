package com.juan.curso.springboot.crud.crud_springboot.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.juan.curso.springboot.crud.crud_springboot.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {

        Optional<Role> findByName(String name);

        @Query("SELECT r.name FROM Role r JOIN r.users u WHERE u.email = :email")
        List<String> findRolesByEmail(String email);
}
