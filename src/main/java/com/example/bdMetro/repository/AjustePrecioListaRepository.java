package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AjustePrecioLista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AjustePrecioListaRepository extends JpaRepository<AjustePrecioLista, Long> {
    Optional<AjustePrecioLista> findByUserCodeAndPaisIgnoreCase(String userCode, String pais);
}
