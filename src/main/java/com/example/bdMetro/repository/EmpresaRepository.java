package com.example.bdMetro.repository;

import com.example.bdMetro.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    List<Empresa> findByUserCode(String userCode);
    Empresa findByUserCodeAndIdNot(String userCode, Long id);
    long countByUserCode(String userCode);

    @Query("SELECT e.id FROM Empresa e WHERE e.userCode = :userCode")
    List<Long> findIdsByUserCode(@Param("userCode") String userCode);

    void deleteByUserCode(String userCode);
}
