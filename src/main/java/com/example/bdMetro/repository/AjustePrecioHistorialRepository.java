package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AjustePrecioHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AjustePrecioHistorialRepository extends JpaRepository<AjustePrecioHistorial, Long> {
    List<AjustePrecioHistorial> findByUserCodeAndPaisIgnoreCaseOrderByCreatedAtDesc(String userCode, String pais);
}
