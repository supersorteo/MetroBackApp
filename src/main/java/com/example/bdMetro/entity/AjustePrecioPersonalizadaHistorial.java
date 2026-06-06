package com.example.bdMetro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuste_precio_personalizada_historial")
public class AjustePrecioPersonalizadaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userCode;

    @Column(nullable = false)
    private String tipo;

    @Column(precision = 10, scale = 4)
    private BigDecimal porcentaje;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal factorResultado;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public BigDecimal getFactorResultado() { return factorResultado; }
    public void setFactorResultado(BigDecimal factorResultado) { this.factorResultado = factorResultado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
