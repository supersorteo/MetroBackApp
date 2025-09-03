package com.example.bdMetro.repository;

import com.example.bdMetro.entity.UserTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTareaRepository extends JpaRepository<UserTarea, Long> {

   // List<UserTarea> findByUserCode(String userCode);

    List<UserTarea> findByClienteId(Long clienteId); // Buscar tareas por clienteId

    @Query("SELECT ut FROM UserTarea ut JOIN Cliente c ON ut.clienteId = c.id WHERE c.userCode = :userCode")
    List<UserTarea> findByUserCode(String userCode);
}
