package com.example.bdMetro.dto;

public class UserDataSummaryDto {
    private String email;
    private long empresas;
    private long clientes;
    private long presupuestos;
    private long tareasPersonalizadas;
    private boolean hasData;

    public UserDataSummaryDto(String email, long empresas, long clientes, long presupuestos, long tareasPersonalizadas) {
        this.email = email;
        this.empresas = empresas;
        this.clientes = clientes;
        this.presupuestos = presupuestos;
        this.tareasPersonalizadas = tareasPersonalizadas;
        this.hasData = empresas > 0 || clientes > 0 || presupuestos > 0 || tareasPersonalizadas > 0;
    }

    public String getEmail() { return email; }
    public long getEmpresas() { return empresas; }
    public long getClientes() { return clientes; }
    public long getPresupuestos() { return presupuestos; }
    public long getTareasPersonalizadas() { return tareasPersonalizadas; }
    public boolean isHasData() { return hasData; }
}
