package com.example.bdMetro.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_tarea")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tarea;
    private Double costo;
    private Double area;
    private String descripcion;
    private Double descuento;
    private Double totalCost;

    @Column(name = "user_code")
    private String userCode;

    private String pais;
    private String rubro;
    private String categoria;

    @ManyToMany(mappedBy = "tareas", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Presupuesto> presupuestos = new ArrayList<>();

    @Column(nullable = false)
    private boolean deleted = false;


    public UserTarea() {}

    public UserTarea(Long id) {
        this.id = id;
    }

    public UserTarea(Long id, String tarea, Double costo, Double area, String descripcion, Double descuento, Double totalCost, String userCode, String pais, String rubro, String categoria, List<Presupuesto> presupuestos, boolean deleted) {
        this.id = id;
        this.tarea = tarea;
        this.costo = costo;
        this.area = area;
        this.descripcion = descripcion;
        this.descuento = descuento;
        this.totalCost = totalCost;
        this.userCode = userCode;
        this.pais = pais;
        this.rubro = rubro;
        this.categoria = categoria;
        this.presupuestos = presupuestos;
        this.deleted = deleted;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTarea() { return tarea; }
    public void setTarea(String tarea) { this.tarea = tarea; }

    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getDescuento() { return descuento; }
    public void setDescuento(Double descuento) { this.descuento = descuento; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getRubro() { return rubro; }
    public void setRubro(String rubro) { this.rubro = rubro; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<Presupuesto> getPresupuestos() { return presupuestos; }
    public void setPresupuestos(List<Presupuesto> presupuestos) { this.presupuestos = presupuestos; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
