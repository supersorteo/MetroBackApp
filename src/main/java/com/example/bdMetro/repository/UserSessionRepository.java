package com.example.bdMetro.repository;

import com.example.bdMetro.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findByAccessCodeCodeAndDeviceId(String code, String deviceId);

    int countByAccessCodeCodeAndLastHeartbeatAfter(String code, Instant cutoff);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.lastHeartbeat < :cutoff")
    void deleteStaleSessionsBefore(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.accessCode.code = :code")
    void deleteByAccessCodeCode(@Param("code") String code);
}
