package com.example.bdMetro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuste_precio_lista",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_code", "pais"}))
public class AjustePrecioLista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_code", nullable = false)
    private String userCode;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal factor = BigDecimal.ONE;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AjustePrecioLista() {}

    public Long getId() { return id; }
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public BigDecimal getFactor() { return factor; }
    public void setFactor(BigDecimal factor) { this.factor = factor; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
