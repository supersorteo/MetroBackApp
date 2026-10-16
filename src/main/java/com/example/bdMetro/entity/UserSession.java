package com.example.bdMetro.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_session", indexes = {
    @Index(name = "idx_us_code_device", columnList = "access_code, device_id"),
    @Index(name = "idx_us_code_heartbeat", columnList = "access_code, last_heartbeat")
})
public class UserSession {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_code", nullable = false)
    private AccessCode accessCode;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Column(name = "last_heartbeat", nullable = false)
    private Instant lastHeartbeat;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public AccessCode getAccessCode() { return accessCode; }
    public void setAccessCode(AccessCode accessCode) { this.accessCode = accessCode; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
