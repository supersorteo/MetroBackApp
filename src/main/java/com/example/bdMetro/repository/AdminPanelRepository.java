package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AdminPanel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminPanelRepository extends JpaRepository<AdminPanel, String> {
    Optional<AdminPanel> findByPais(String pais);
    Optional<AdminPanel> findByUsername(String username);
}
