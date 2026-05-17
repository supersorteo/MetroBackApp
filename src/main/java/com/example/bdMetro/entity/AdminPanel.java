package com.example.bdMetro.entity;

import jakarta.persistence.*;

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

    @Column(nullable = false, columnDefinition = "integer default 6")
    private Integer demoMaxClientes = 6;

    @Column(nullable = false, columnDefinition = "integer default 30")
    private Integer vip3MaxClientes = 30;

    @Column(nullable = false, columnDefinition = "integer default 60")
    private Integer vip6MaxClientes = 60;

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
    public void setDemoMaxEmpresas(Integer demoMaxEmpresas) { this.demoMaxEmpresas = demoMaxEmpresas; }

    public Integer getVip3MaxEmpresas() { return vip3MaxEmpresas; }
    public void setVip3MaxEmpresas(Integer vip3MaxEmpresas) { this.vip3MaxEmpresas = vip3MaxEmpresas; }

    public Integer getVip6MaxEmpresas() { return vip6MaxEmpresas; }
    public void setVip6MaxEmpresas(Integer vip6MaxEmpresas) { this.vip6MaxEmpresas = vip6MaxEmpresas; }

    public Integer getDemoMaxClientes() { return demoMaxClientes; }
    public void setDemoMaxClientes(Integer demoMaxClientes) { this.demoMaxClientes = demoMaxClientes; }

    public Integer getVip3MaxClientes() { return vip3MaxClientes; }
    public void setVip3MaxClientes(Integer vip3MaxClientes) { this.vip3MaxClientes = vip3MaxClientes; }

    public Integer getVip6MaxClientes() { return vip6MaxClientes; }
    public void setVip6MaxClientes(Integer vip6MaxClientes) { this.vip6MaxClientes = vip6MaxClientes; }
}
