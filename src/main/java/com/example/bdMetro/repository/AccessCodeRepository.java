package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessCodeRepository extends JpaRepository<AccessCode, String> {
    AccessCode findByCode(String code);
    AccessCode findByEmail(String email);
}
