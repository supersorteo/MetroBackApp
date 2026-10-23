package com.example.bdMetro.services;

import com.example.bdMetro.entity.PrecioMaterial;
import com.example.bdMetro.repository.PrecioMaterialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Service
public class PrecioMaterialService {

    private static final Logger log = LoggerFactory.getLogger(PrecioMaterialService.class);

    @Autowired private PrecioMaterialRepository precioMaterialRepository;
    @Autowired private ObjectMapper objectMapper;

    @PostConstruct
    @Transactional
    public void seedArgentina() {
        if (precioMaterialRepository.countByPais("Argentina") > 0) return;

        try (InputStream is = getClass().getResourceAsStream("/data/precio-materiales-ar.json")) {
            if (is == null) {
                log.warn("[PrecioMaterial] No se encontró /data/precio-materiales-ar.json en el classpath");
                return;
            }
            JsonNode root = objectMapper.readTree(is);
            JsonNode categories = root.get("categories");
            if (categories == null || !categories.isArray()) return;

            List<PrecioMaterial> batch = new ArrayList<>();
            for (JsonNode cat : categories) {
                String catId = cat.path("id").asText();
                String catNombre = cat.path("name").asText();
                JsonNode materials = cat.get("materials");
                if (materials == null || !materials.isArray()) continue;

                for (JsonNode m : materials) {
                    PrecioMaterial p = new PrecioMaterial();
                    p.setNombre(m.path("name").asText());
                    p.setCategoriaId(catId);
                    p.setCategoriaNombre(catNombre);
                    p.setUnidad(m.path("unit").asText());
                    p.setNombreUnidad(m.path("unitName").asText());
                    p.setPrecio(m.path("cost").asDouble());
                    p.setMoneda(m.path("currency").asText("ARS"));
                    p.setPais("Argentina");
                    p.setSourceId(m.path("sourceId").asInt(0));
                    batch.add(p);

                    if (batch.size() == 200) {
                        precioMaterialRepository.saveAll(batch);
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) precioMaterialRepository.saveAll(batch);
            log.info("[PrecioMaterial] Seed Argentina completado: {} materiales cargados",
                    precioMaterialRepository.countByPais("Argentina"));
        } catch (Exception e) {
            log.error("[PrecioMaterial] Error en seed Argentina: {}", e.getMessage(), e);
        }
    }

    public List<PrecioMaterial> getPreciosByPais(String pais) {
        return precioMaterialRepository.findByPaisIgnoreCase(pais);
    }

    public Page<PrecioMaterial> getAdminPage(String pais, int page, int size, String buscar, List<String> categorias) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("categoriaNombre", "nombre"));
        String b = (buscar == null) ? "" : buscar.trim();
        List<String> cats = (categorias == null) ? Collections.emptyList() : categorias;
        if (!cats.isEmpty()) {
            return precioMaterialRepository.searchAdminByCategorias(pais, cats, b, pageable);
        }
        return precioMaterialRepository.searchAdmin(pais, b, pageable);
    }

    public List<String> getCategoriasByPais(String pais) {
        return precioMaterialRepository.findDistinctCategoriasByPais(pais);
    }

    public PrecioMaterial create(PrecioMaterial pm) {
        return precioMaterialRepository.save(pm);
    }

    public PrecioMaterial update(Long id, PrecioMaterial data) {
        PrecioMaterial pm = precioMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado: " + id));
        pm.setNombre(data.getNombre());
        pm.setCategoriaNombre(data.getCategoriaNombre());
        pm.setCategoriaId(data.getCategoriaId());
        pm.setUnidad(data.getUnidad());
        pm.setNombreUnidad(data.getNombreUnidad());
        pm.setPrecio(data.getPrecio());
        pm.setMoneda(data.getMoneda());
        return precioMaterialRepository.save(pm);
    }

    public void delete(Long id) {
        precioMaterialRepository.deleteById(id);
    }
}
