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

    @Modifying
    @Query("UPDATE UserSession s SET s.lastHeartbeat = :now WHERE s.id = :id AND s.kicked = false")
    int touchHeartbeat(@Param("id") String id, @Param("now") Instant now);

    @Query("SELECT COUNT(s) > 0 FROM UserSession s WHERE s.id = :id AND s.kicked = true")
    boolean existsKickedById(@Param("id") String id);

    @Modifying
    @Query("UPDATE UserSession s SET s.kicked = true WHERE s.accessCode.code = :code")
    void kickByAccessCodeCode(@Param("code") String code);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.kicked = true AND s.lastHeartbeat < :cutoff")
    void deleteKickedSessionsBefore(@Param("cutoff") Instant cutoff);
}
