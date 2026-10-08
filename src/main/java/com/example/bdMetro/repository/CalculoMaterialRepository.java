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
    void deleteByUserCode(String userCode);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE CalculoMaterial c SET c.userCode = :newCode WHERE c.userCode = :oldCode")
    void updateUserCode(@org.springframework.data.repository.query.Param("oldCode") String oldCode, @org.springframework.data.repository.query.Param("newCode") String newCode);
}
