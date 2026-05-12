package com.example.bdMetro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tarea_personalizada")
public class TareaPersonalizada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userCode;

    private String tarea;
    private String descripcion;
    private Double costo;

    @Column(nullable = false)
    private boolean deleted = false;

    public TareaPersonalizada() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getTarea() { return tarea; }
    public void setTarea(String tarea) { this.tarea = tarea; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
