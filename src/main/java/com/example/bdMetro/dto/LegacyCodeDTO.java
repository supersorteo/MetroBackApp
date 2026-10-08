package com.example.bdMetro.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LegacyCodeDTO {

    private String code;
    private Integer meses;
    private LocalDate fechaAdquisicion;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaCreacion;
    private boolean claimed;
    private String claimedByEmail;
    private LocalDateTime claimedAt;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getMeses() { return meses; }
    public void setMeses(Integer meses) { this.meses = meses; }

    public LocalDate getFechaAdquisicion() { return fechaAdquisicion; }
    public void setFechaAdquisicion(LocalDate fechaAdquisicion) { this.fechaAdquisicion = fechaAdquisicion; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public String getClaimedByEmail() { return claimedByEmail; }
    public void setClaimedByEmail(String claimedByEmail) { this.claimedByEmail = claimedByEmail; }

    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
}
