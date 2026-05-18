package com.example.bdMetro.repository;

import com.example.bdMetro.entity.UserTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTareaRepository extends JpaRepository<UserTarea, Long> {

   // List<UserTarea> findByUserCode(String userCode);

    List<UserTarea> findByClienteId(Long clienteId); // Buscar tareas por clienteId

    //@Query("SELECT ut FROM UserTarea ut JOIN Cliente c ON ut.clienteId = c.id WHERE c.userCode = :userCode")
    //List<UserTarea> findByUserCode(String userCode);

    List<UserTarea> findByClienteIdAndDeletedFalse(Long clienteId);




    @Query("SELECT ut FROM UserTarea ut JOIN Cliente c ON ut.clienteId = c.id WHERE c.userCode = :userCode AND ut.deleted = false")
    List<UserTarea> findByUserCode(String userCode);

    @Query("SELECT ut FROM UserTarea ut WHERE ut.deleted = false")
    List<UserTarea> findAllActive();

    @Modifying
    @Query("UPDATE UserTarea ut SET ut.deleted = true WHERE ut.clienteId = :clienteId AND ut.deleted = false")
    void softDeleteAllByClienteId(@Param("clienteId") Long clienteId);
}
