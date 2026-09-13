package com.example.bdMetro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RagConsultaResponse {
    private String respuesta;
    private boolean error;
}
