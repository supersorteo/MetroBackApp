package com.example.bdMetro.dto;

import java.time.LocalDateTime;

public record PresupuestoSummaryDTO(
    Long id,
    String name,
    LocalDateTime createdAt,
    Long clienteId,
    String clienteName,
    Long empresaId,
    String empresaName,
    long tareasCount
) {}
