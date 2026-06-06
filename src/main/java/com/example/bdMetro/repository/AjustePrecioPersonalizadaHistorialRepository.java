package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AjustePrecioPersonalizadaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AjustePrecioPersonalizadaHistorialRepository extends JpaRepository<AjustePrecioPersonalizadaHistorial, Long> {
    List<AjustePrecioPersonalizadaHistorial> findByUserCodeOrderByCreatedAtDesc(String userCode);
}
