package com.example.bdMetro.repository;

import com.example.bdMetro.entity.TareaPersonalizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaPersonalizadaRepository extends JpaRepository<TareaPersonalizada, Long> {
    List<TareaPersonalizada> findByUserCodeAndDeletedFalse(String userCode);
}
