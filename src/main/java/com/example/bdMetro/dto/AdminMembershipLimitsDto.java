package com.example.bdMetro.dto;

import java.math.BigDecimal;

public class AdminMembershipLimitsDto {
    private String id;
    private String pais;
    private Integer demoMaxEmpresas;
    private Integer vip3MaxEmpresas;
    private Integer vip6MaxEmpresas;
    private Integer vip12MaxEmpresas;
    private Integer demoMaxClientes;
    private Integer vip3MaxClientes;
    private Integer vip6MaxClientes;
    private Integer vip12MaxClientes;
    private Integer demoMaxPresupuestos;
    private Integer vip3MaxPresupuestos;
    private Integer vip6MaxPresupuestos;
    private Integer vip12MaxPresupuestos;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public Integer getDemoMaxEmpresas() { return demoMaxEmpresas; }
    public void setDemoMaxEmpresas(Integer v) { this.demoMaxEmpresas = v; }

    public Integer getVip3MaxEmpresas() { return vip3MaxEmpresas; }
    public void setVip3MaxEmpresas(Integer v) { this.vip3MaxEmpresas = v; }

    public Integer getVip6MaxEmpresas() { return vip6MaxEmpresas; }
    public void setVip6MaxEmpresas(Integer v) { this.vip6MaxEmpresas = v; }

    public Integer getVip12MaxEmpresas() { return vip12MaxEmpresas; }
    public void setVip12MaxEmpresas(Integer v) { this.vip12MaxEmpresas = v; }

    public Integer getDemoMaxClientes() { return demoMaxClientes; }
    public void setDemoMaxClientes(Integer v) { this.demoMaxClientes = v; }

    public Integer getVip3MaxClientes() { return vip3MaxClientes; }
    public void setVip3MaxClientes(Integer v) { this.vip3MaxClientes = v; }

    public Integer getVip6MaxClientes() { return vip6MaxClientes; }
    public void setVip6MaxClientes(Integer v) { this.vip6MaxClientes = v; }

    public Integer getVip12MaxClientes() { return vip12MaxClientes; }
    public void setVip12MaxClientes(Integer v) { this.vip12MaxClientes = v; }

    public Integer getDemoMaxPresupuestos() { return demoMaxPresupuestos; }
    public void setDemoMaxPresupuestos(Integer v) { this.demoMaxPresupuestos = v; }

    public Integer getVip3MaxPresupuestos() { return vip3MaxPresupuestos; }
    public void setVip3MaxPresupuestos(Integer v) { this.vip3MaxPresupuestos = v; }

    public Integer getVip6MaxPresupuestos() { return vip6MaxPresupuestos; }
    public void setVip6MaxPresupuestos(Integer v) { this.vip6MaxPresupuestos = v; }

    public Integer getVip12MaxPresupuestos() { return vip12MaxPresupuestos; }
    public void setVip12MaxPresupuestos(Integer v) { this.vip12MaxPresupuestos = v; }

    private BigDecimal precio3Meses;
    private BigDecimal precio6Meses;
    private BigDecimal precio12Meses;
    private String moneda;

    public BigDecimal getPrecio3Meses() { return precio3Meses; }
    public void setPrecio3Meses(BigDecimal v) { this.precio3Meses = v; }

    public BigDecimal getPrecio6Meses() { return precio6Meses; }
    public void setPrecio6Meses(BigDecimal v) { this.precio6Meses = v; }

    public BigDecimal getPrecio12Meses() { return precio12Meses; }
    public void setPrecio12Meses(BigDecimal v) { this.precio12Meses = v; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String v) { this.moneda = v; }
}
