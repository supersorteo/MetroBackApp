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
}
