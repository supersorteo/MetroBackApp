package com.example.bdMetro.repository;

import com.example.bdMetro.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Cliente findByUserCodeAndName(String userCode, String name);

    List<Cliente> findByUserCode(String userCode);

    List<Cliente> findByEmail(String email);
}
