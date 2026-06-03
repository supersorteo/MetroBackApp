package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AjustePrecioAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AjustePrecioAdminRepository extends JpaRepository<AjustePrecioAdmin, Long> {
    Optional<AjustePrecioAdmin> findByPaisIgnoreCase(String pais);
}
