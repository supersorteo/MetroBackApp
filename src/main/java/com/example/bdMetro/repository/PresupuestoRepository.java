package com.example.bdMetro.repository;

import com.example.bdMetro.entity.Presupuesto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    @Query("SELECT p FROM Presupuesto p WHERE p.cliente.empresaId IN " +
            "(SELECT e.id FROM Empresa e WHERE e.userCode = :userCode)")
    List<Presupuesto> findByUserCode(@Param("userCode") String userCode);

    @EntityGraph(attributePaths = {"tareas"})
    @Query("SELECT p FROM Presupuesto p WHERE p.cliente.id = :clienteId")
    List<Presupuesto> findByClienteIdWithTareas(@Param("clienteId") Long clienteId);

    List<Presupuesto> findByClienteId(Long clienteId);

    @EntityGraph(attributePaths = {"tareas"})
    Optional<Presupuesto> findById(Long id);

}
