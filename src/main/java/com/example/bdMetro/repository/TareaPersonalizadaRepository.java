package com.example.bdMetro.repository;

import com.example.bdMetro.entity.TareaPersonalizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaPersonalizadaRepository extends JpaRepository<TareaPersonalizada, Long> {
    List<TareaPersonalizada> findByUserCodeAndDeletedFalse(String userCode);
    long countByUserCode(String userCode);
    void deleteByUserCode(String userCode);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE TareaPersonalizada t SET t.userCode = :newCode WHERE t.userCode = :oldCode")
    void updateUserCode(@org.springframework.data.repository.query.Param("oldCode") String oldCode, @org.springframework.data.repository.query.Param("newCode") String newCode);
}
