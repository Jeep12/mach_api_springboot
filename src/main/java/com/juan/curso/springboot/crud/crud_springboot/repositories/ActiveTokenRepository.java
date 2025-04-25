package com.juan.curso.springboot.crud.crud_springboot.repositories;

import com.juan.curso.springboot.crud.crud_springboot.entities.users.ActiveToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ActiveTokenRepository extends CrudRepository<ActiveToken, Long> {

    // Verificar si un token existe en la base de datos
    boolean existsByToken(String token);

    // Encontrar todos los tokens de un usuario por su ID
    List<ActiveToken> findByUserId(Long userId);
    ActiveToken findByToken(String token);  // Método para buscar un ActiveToken por el token

    @Transactional
    @Modifying
    @Query("DELETE FROM ActiveToken a WHERE a.user.id = :userId")
    void deleteOldTokensByUserId(Long userId);


}
