package com.example.bdMetro.repository;

import com.example.bdMetro.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Cliente findByUserCodeAndName(String userCode, String name);
    List<Cliente> findByUserCode(String userCode);
    List<Cliente> findByEmail(String email);
    boolean existsByEmailAndUserCode(String email, String userCode);
    List<Cliente> findByEmpresaId(Long empresaId);
    long countByUserCode(String userCode);

    @Query("SELECT c.id FROM Cliente c WHERE c.userCode = :userCode")
    List<Long> findIdsByUserCode(@Param("userCode") String userCode);

    void deleteByUserCode(String userCode);
}
