package com.example.bdMetro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "calculo_material")
public class CalculoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String userCode;

    @Column(nullable = false)
    private Long tareaId;

    @Column(nullable = false, length = 255)
    private String tareaTitulo;

    @Column(nullable = false, length = 120)
    private String categoria;

    @Column(nullable = false, length = 40)
    private String unidad;

    @Column(nullable = false)
    private Double valorIngresado;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultadosJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public Long getTareaId() {
        return tareaId;
    }

    public void setTareaId(Long tareaId) {
        this.tareaId = tareaId;
    }

    public String getTareaTitulo() {
        return tareaTitulo;
    }

    public void setTareaTitulo(String tareaTitulo) {
        this.tareaTitulo = tareaTitulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Double getValorIngresado() {
        return valorIngresado;
    }

    public void setValorIngresado(Double valorIngresado) {
        this.valorIngresado = valorIngresado;
    }

    public String getResultadosJson() {
        return resultadosJson;
    }

    public void setResultadosJson(String resultadosJson) {
        this.resultadosJson = resultadosJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
