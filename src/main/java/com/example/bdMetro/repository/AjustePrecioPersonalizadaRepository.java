package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AjustePrecioPersonalizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AjustePrecioPersonalizadaRepository extends JpaRepository<AjustePrecioPersonalizada, Long> {
    Optional<AjustePrecioPersonalizada> findByUserCode(String userCode);
}
