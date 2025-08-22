package com.example.bdMetro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
public class Tarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tarea; // Renamed from nombre
    private Double costo;
    private String rubro; // Added
    private String categoria; // Added
    private String pais; // Added for country link
    private String descripcion;
    private Double descuento;
    private Double area;

    public Tarea() {
    }

    public Tarea(String tarea, Double costo, String rubro, String categoria, String pais, String descripcion, Double descuento, Double area) {
        this.tarea = tarea;
        this.costo = costo;
        this.rubro = rubro;
        this.categoria = categoria;
        this.pais = pais;
        this.descripcion = descripcion;
        this.descuento = descuento;
        this.area = area;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTarea() {
        return tarea;
    }

    public void setTarea(String tarea) {
        this.tarea = tarea;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }
}
