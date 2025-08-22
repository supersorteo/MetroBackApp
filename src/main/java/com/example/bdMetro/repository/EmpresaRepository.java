package com.example.bdMetro.repository;

import com.example.bdMetro.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    List<Empresa> findByUserCode(String userCode);
    Empresa findByUserCodeAndIdNot(String userCode, Long id);
}
