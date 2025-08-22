package com.example.bdMetro.repository;

import com.example.bdMetro.entity.UserTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTareaRepository extends JpaRepository<UserTarea, Long> {

    List<UserTarea> findByUserCode(String userCode);
}
