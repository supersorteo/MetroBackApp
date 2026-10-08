package com.example.bdMetro.repository;

import com.example.bdMetro.entity.LegacyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegacyCodeRepository extends JpaRepository<LegacyCode, Long> {
    Optional<LegacyCode> findByCode(String code);
    boolean existsByCode(String code);
    List<LegacyCode> findAllByClaimedFalse();
}
