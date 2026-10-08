package com.example.bdMetro.repository;

import com.example.bdMetro.entity.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccessCodeRepository extends JpaRepository<AccessCode, String> {
    AccessCode findByCode(String code);
    AccessCode findByCodeIgnoreCase(String code);
    AccessCode findByEmail(String email);
    List<AccessCode> findByPaisIgnoreCase(String pais);
    List<AccessCode> findByPaisIgnoreCaseAndLegacyFalse(String pais);

    @Query("SELECT a FROM AccessCode a WHERE a.fechaVencimiento < :cutoff AND a.email IS NOT NULL")
    List<AccessCode> findExpiredWithEmail(@Param("cutoff") LocalDate cutoff);
}
