package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AjustePrecioAdminHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AjustePrecioAdminHistorialRepository extends JpaRepository<AjustePrecioAdminHistorial, Long> {
    List<AjustePrecioAdminHistorial> findByPaisIgnoreCaseOrderByCreatedAtDesc(String pais);
}
