package com.example.bdMetro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "admin_panel")
public class AdminPanel {

    @Id
    private String id;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String flag;

    @Column(nullable = false, columnDefinition = "integer default 3")
    private Integer demoMaxEmpresas = 3;

    @Column(nullable = false, columnDefinition = "integer default 1")
    private Integer vip3MaxEmpresas = 1;

    @Column(nullable = false, columnDefinition = "integer default 3")
    private Integer vip6MaxEmpresas = 3;

    @Column(columnDefinition = "integer default 5")
    private Integer vip12MaxEmpresas = 5;

    @Column(nullable = false, columnDefinition = "integer default 6")
    private Integer demoMaxClientes = 6;

    @Column(nullable = false, columnDefinition = "integer default 200")
    private Integer vip3MaxClientes = 200;

    @Column(nullable = false, columnDefinition = "integer default 500")
    private Integer vip6MaxClientes = 500;

    @Column(columnDefinition = "integer default 1000")
    private Integer vip12MaxClientes = 1000;

    @Column(columnDefinition = "integer default 5")
    private Integer demoMaxPresupuestos = 5;

    @Column(columnDefinition = "integer default 30")
    private Integer vip3MaxPresupuestos = 30;

    @Column(columnDefinition = "integer default 120")
    private Integer vip6MaxPresupuestos = 120;

    @Column(columnDefinition = "integer default 250")
    private Integer vip12MaxPresupuestos = 250;

    @Column(columnDefinition = "numeric(15,2)")
    private BigDecimal precio3Meses;

    @Column(columnDefinition = "numeric(15,2)")
    private BigDecimal precio6Meses;

    @Column(columnDefinition = "numeric(15,2)")
    private BigDecimal precio12Meses;

    @Column(length = 10)
    private String moneda;

    public String getId()           { return id; }
    public void   setId(String id)  { this.id = id; }

    public String getPais()             { return pais; }
    public void   setPais(String pais)  { this.pais = pais; }

    public String getNombre()               { return nombre; }
    public void   setNombre(String nombre)  { this.nombre = nombre; }

    public String getUsername()                 { return username; }
    public void   setUsername(String username)  { this.username = username; }

    public String getPassword()                 { return password; }
    public void   setPassword(String password)  { this.password = password; }

    public String getFlag()             { return flag; }
    public void   setFlag(String flag)  { this.flag = flag; }

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

    public BigDecimal getPrecio3Meses() { return precio3Meses; }
    public void setPrecio3Meses(BigDecimal v) { this.precio3Meses = v; }

    public BigDecimal getPrecio6Meses() { return precio6Meses; }
    public void setPrecio6Meses(BigDecimal v) { this.precio6Meses = v; }

    public BigDecimal getPrecio12Meses() { return precio12Meses; }
    public void setPrecio12Meses(BigDecimal v) { this.precio12Meses = v; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String v) { this.moneda = v; }
}
