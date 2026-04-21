package com.example.bdMetro.dto;

public class ResultadoMaterialDto {
    private String nombre;
    private Double cantidad;
    private String icono;
    private Integer bolsas;
    private String bolsasLabel;
    private String detalleDias;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public Integer getBolsas() {
        return bolsas;
    }

    public void setBolsas(Integer bolsas) {
        this.bolsas = bolsas;
    }

    public String getBolsasLabel() {
        return bolsasLabel;
    }

    public void setBolsasLabel(String bolsasLabel) {
        this.bolsasLabel = bolsasLabel;
    }

    public String getDetalleDias() {
        return detalleDias;
    }

    public void setDetalleDias(String detalleDias) {
        this.detalleDias = detalleDias;
    }
}
