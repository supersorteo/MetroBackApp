package com.example.bdMetro.dto;

import java.util.ArrayList;
import java.util.List;

public class CalculoMaterialRequest {
    private String userCode;
    private Long tareaId;
    private String tareaTitulo;
    private String categoria;
    private String unidad;
    private Double valorIngresado;
    private List<ResultadoMaterialDto> resultados = new ArrayList<>();

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

    public List<ResultadoMaterialDto> getResultados() {
        return resultados;
    }

    public void setResultados(List<ResultadoMaterialDto> resultados) {
        this.resultados = resultados;
    }
}
