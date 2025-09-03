package com.example.bdMetro.entity;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String contact;
    private LocalDate budgetDate;
    private String additionalDetails;
    private String userCode;
    private String email;
    private String clave; // o CUIT
    private String direccion;
    private Long empresaId;

    public Cliente() {
    }

    // Constructor completo
   /* public Cliente(String name, String contact, LocalDate budgetDate, String additionalDetails, String userCode, String email, String clave, String direccion) {
        this.name = name;
        this.contact = contact;
        this.budgetDate = budgetDate;
        this.additionalDetails = additionalDetails;
        this.userCode = userCode;
        this.email = email;
        this.clave = clave;
        this.direccion = direccion;
    }*/

    public Cliente(String name, String contact, LocalDate budgetDate, String additionalDetails, String userCode, String email, String clave, String direccion, Long empresaId) {
        this.name = name;
        this.contact = contact;
        this.budgetDate = budgetDate;
        this.additionalDetails = additionalDetails;
        this.userCode = userCode;
        this.email = email;
        this.clave = clave;
        this.direccion = direccion;
        this.empresaId = empresaId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public LocalDate getBudgetDate() {
        return budgetDate;
    }

    public void setBudgetDate(LocalDate budgetDate) {
        this.budgetDate = budgetDate;
    }

    public String getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(String additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
}
