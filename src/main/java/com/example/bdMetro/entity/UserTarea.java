package com.example.bdMetro.entity;

import jakarta.persistence.*;

@Entity
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
   // private String userCode; // Enlace al usuario
    private Long clienteId;
    private String pais; // Para consistencia con el país del usuario
    private String rubro; // Añadido
    private String categoria;

    // Constructor sin argumentos (requerido por JPA)
    public UserTarea() {
    }

    /*
    public UserTarea(String tarea, Double costo, Double area, String descripcion, Double descuento, Double totalCost, String userCode, String pais, String rubro, String categoria) {
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
    }*/

    public UserTarea(String tarea, Double costo, Double area, String descripcion, Double descuento, Double totalCost, Long clienteId, String pais, String rubro, String categoria) {
        this.tarea = tarea;
        this.costo = costo;
        this.area = area;
        this.descripcion = descripcion;
        this.descuento = descuento;
        this.totalCost = totalCost;
        this.clienteId = clienteId;
        this.pais = pais;
        this.rubro = rubro;
        this.categoria = categoria;
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

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
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

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    /*
    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
    */

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
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

}
