package com.example.bdMetro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuste_precio_historial")
public class AjustePrecioHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_code", nullable = false)
    private String userCode;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private String tipo; // "subir" | "bajar" | "reestablecer"

    @Column(precision = 10, scale = 4)
    private BigDecimal porcentaje;

    @Column(name = "factor_resultado", nullable = false, precision = 14, scale = 6)
    private BigDecimal factorResultado;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AjustePrecioHistorial() {}

    public Long getId() { return id; }
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public BigDecimal getFactorResultado() { return factorResultado; }
    public void setFactorResultado(BigDecimal factorResultado) { this.factorResultado = factorResultado; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
