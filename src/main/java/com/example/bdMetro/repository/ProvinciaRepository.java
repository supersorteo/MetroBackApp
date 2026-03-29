package com.example.bdMetro.repository;

import com.example.bdMetro.entity.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {

    List<Provincia> findByPais(String pais);
    List<Provincia> findByPaisIgnoreCase(String pais);

}
