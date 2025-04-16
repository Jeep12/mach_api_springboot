package com.juan.curso.springboot.crud.crud_springboot.repositories;

import com.juan.curso.springboot.crud.crud_springboot.entities.ActiveToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ActiveTokenRepository extends CrudRepository<ActiveToken, Long> {

    boolean existsByToken(String token);

    List<ActiveToken> findByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ActiveToken a WHERE a.user.id = :userId")
    void deleteByUserId(Long userId);
}
