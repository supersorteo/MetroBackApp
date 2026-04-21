package com.example.bdMetro.repository;

import com.example.bdMetro.entity.CalculoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalculoMaterialRepository extends JpaRepository<CalculoMaterial, Long> {
    List<CalculoMaterial> findByUserCodeOrderByCreatedAtDescIdDesc(String userCode);
    Optional<CalculoMaterial> findByIdAndUserCode(Long id, String userCode);
}
