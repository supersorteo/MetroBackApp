package com.example.bdMetro.services;

import com.example.bdMetro.dto.CalculoMaterialRequest;
import com.example.bdMetro.dto.CalculoMaterialResponse;
import com.example.bdMetro.dto.ResultadoMaterialDto;
import com.example.bdMetro.dto.TareaCalculadaResumenResponse;
import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.entity.CalculoMaterial;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.example.bdMetro.repository.CalculoMaterialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculoMaterialService {

    private static final int MAX_HISTORIAL = 10;

    private final CalculoMaterialRepository calculoMaterialRepository;
    private final AccessCodeRepository accessCodeRepository;
    private final ObjectMapper objectMapper;

    public CalculoMaterialService(
            CalculoMaterialRepository calculoMaterialRepository,
            AccessCodeRepository accessCodeRepository,
            ObjectMapper objectMapper) {
        this.calculoMaterialRepository = calculoMaterialRepository;
        this.accessCodeRepository = accessCodeRepository;
        this.objectMapper = objectMapper;
    }

    public CalculoMaterialResponse guardar(CalculoMaterialRequest request) {
        String userCode = normalizarUserCode(request.getUserCode());
        validarRequest(request, userCode);

        AccessCode accessCode = accessCodeRepository.findByCode(userCode);
        if (accessCode == null || accessCode.getEmail() == null || accessCode.getEmail().isBlank()) {
            throw new IllegalArgumentException("El usuario autenticado no es valido para guardar calculos.");
        }

        CalculoMaterial entity = new CalculoMaterial();
        entity.setUserCode(userCode);
        entity.setTareaId(request.getTareaId());
        entity.setTareaTitulo(request.getTareaTitulo().trim());
        entity.setCategoria(request.getCategoria().trim());
        entity.setUnidad(request.getUnidad().trim());
        entity.setValorIngresado(request.getValorIngresado());
        entity.setResultadosJson(serializarResultados(request.getResultados()));
        entity.setCreatedAt(LocalDateTime.now());

        CalculoMaterial saved = calculoMaterialRepository.save(entity);
        podarHistorial(userCode);
        return toResponse(saved);
    }

    public List<CalculoMaterialResponse> obtenerHistorial(String userCode, int limit) {
        String normalizedUserCode = normalizarUserCode(userCode);
        int safeLimit = normalizarLimite(limit, MAX_HISTORIAL);

        return calculoMaterialRepository.findByUserCodeOrderByCreatedAtDescIdDesc(normalizedUserCode).stream()
                .limit(safeLimit)
                .map(this::toResponse)
                .toList();
    }

    public List<TareaCalculadaResumenResponse> obtenerUltimasTareas(String userCode, int limit) {
        String normalizedUserCode = normalizarUserCode(userCode);
        int safeLimit = normalizarLimite(limit, 5);

        Map<Long, TareaCalculadaResumenResponse> orderedUniqueTasks = new LinkedHashMap<>();
        for (CalculoMaterial calculo : calculoMaterialRepository.findByUserCodeOrderByCreatedAtDescIdDesc(normalizedUserCode)) {
            if (orderedUniqueTasks.containsKey(calculo.getTareaId())) {
                continue;
            }

            TareaCalculadaResumenResponse resumen = new TareaCalculadaResumenResponse();
            resumen.setTareaId(calculo.getTareaId());
            resumen.setTareaTitulo(calculo.getTareaTitulo());
            resumen.setCategoria(calculo.getCategoria());
            resumen.setUnidad(calculo.getUnidad());
            resumen.setLastCalculatedAt(calculo.getCreatedAt());
            orderedUniqueTasks.put(calculo.getTareaId(), resumen);

            if (orderedUniqueTasks.size() >= safeLimit) {
                break;
            }
        }

        return new ArrayList<>(orderedUniqueTasks.values());
    }

    private void podarHistorial(String userCode) {
        List<CalculoMaterial> historial = calculoMaterialRepository.findByUserCodeOrderByCreatedAtDescIdDesc(userCode);
        if (historial.size() <= MAX_HISTORIAL) {
            return;
        }
        calculoMaterialRepository.deleteAll(historial.subList(MAX_HISTORIAL, historial.size()));
    }

    private void validarRequest(CalculoMaterialRequest request, String userCode) {
        if (userCode.isBlank()) {
            throw new IllegalArgumentException("El userCode es obligatorio.");
        }
        if (request.getTareaId() == null || request.getTareaId() <= 0) {
            throw new IllegalArgumentException("La tarea es obligatoria.");
        }
        if (request.getTareaTitulo() == null || request.getTareaTitulo().isBlank()) {
            throw new IllegalArgumentException("El titulo de la tarea es obligatorio.");
        }
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoria es obligatoria.");
        }
        if (request.getUnidad() == null || request.getUnidad().isBlank()) {
            throw new IllegalArgumentException("La unidad es obligatoria.");
        }
        if (request.getValorIngresado() == null || request.getValorIngresado() <= 0) {
            throw new IllegalArgumentException("El valor ingresado debe ser mayor que cero.");
        }
        if (request.getResultados() == null || request.getResultados().isEmpty()) {
            throw new IllegalArgumentException("Los resultados del calculo son obligatorios.");
        }
    }

    private String serializarResultados(List<ResultadoMaterialDto> resultados) {
        try {
            return objectMapper.writeValueAsString(resultados);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudieron serializar los resultados del calculo.", e);
        }
    }

    private List<ResultadoMaterialDto> deserializarResultados(String resultadosJson) {
        try {
            return objectMapper.readValue(resultadosJson, new TypeReference<List<ResultadoMaterialDto>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudieron leer los resultados guardados del calculo.", e);
        }
    }

    private CalculoMaterialResponse toResponse(CalculoMaterial entity) {
        CalculoMaterialResponse response = new CalculoMaterialResponse();
        response.setId(entity.getId());
        response.setUserCode(entity.getUserCode());
        response.setTareaId(entity.getTareaId());
        response.setTareaTitulo(entity.getTareaTitulo());
        response.setCategoria(entity.getCategoria());
        response.setUnidad(entity.getUnidad());
        response.setValorIngresado(entity.getValorIngresado());
        response.setResultados(deserializarResultados(entity.getResultadosJson()));
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    private String normalizarUserCode(String userCode) {
        return userCode == null ? "" : userCode.trim().toUpperCase();
    }

    private int normalizarLimite(int requestedLimit, int maxLimit) {
        if (requestedLimit <= 0) {
            return maxLimit;
        }
        return Math.min(requestedLimit, maxLimit);
    }
}
