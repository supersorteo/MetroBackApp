package com.example.bdMetro.dto;

public class AdminMembershipLimitsDto {
    private String id;
    private String pais;
    private Integer demoMaxEmpresas;
    private Integer vip3MaxEmpresas;
    private Integer vip6MaxEmpresas;
    private Integer demoMaxClientes;
    private Integer vip3MaxClientes;
    private Integer vip6MaxClientes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

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
