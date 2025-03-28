package com.juan.curso.springboot.crud.crud_springboot.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.juan.curso.springboot.crud.crud_springboot.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {

        Optional<Role> findByName(String name);

}
