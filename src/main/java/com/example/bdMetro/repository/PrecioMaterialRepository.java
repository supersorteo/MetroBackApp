package com.example.bdMetro.repository;

import com.example.bdMetro.entity.PrecioMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrecioMaterialRepository extends JpaRepository<PrecioMaterial, Long> {
    List<PrecioMaterial> findByPaisIgnoreCase(String pais);
    long countByPais(String pais);

    @Query("SELECT DISTINCT p.categoriaNombre FROM PrecioMaterial p WHERE LOWER(p.pais) = LOWER(:pais) ORDER BY p.categoriaNombre")
    List<String> findDistinctCategoriasByPais(@Param("pais") String pais);

    @Query("SELECT p FROM PrecioMaterial p WHERE LOWER(p.pais) = LOWER(:pais) " +
           "AND (:buscar = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%',:buscar,'%')) " +
           "OR LOWER(p.categoriaNombre) LIKE LOWER(CONCAT('%',:buscar,'%')) " +
           "OR LOWER(p.unidad) LIKE LOWER(CONCAT('%',:buscar,'%')))")
    Page<PrecioMaterial> searchAdmin(@Param("pais") String pais, @Param("buscar") String buscar, Pageable pageable);

    @Query("SELECT p FROM PrecioMaterial p WHERE LOWER(p.pais) = LOWER(:pais) " +
           "AND p.categoriaNombre IN :categorias " +
           "AND (:buscar = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%',:buscar,'%')) " +
           "OR LOWER(p.categoriaNombre) LIKE LOWER(CONCAT('%',:buscar,'%')) " +
           "OR LOWER(p.unidad) LIKE LOWER(CONCAT('%',:buscar,'%')))")
    Page<PrecioMaterial> searchAdminByCategorias(@Param("pais") String pais, @Param("categorias") List<String> categorias, @Param("buscar") String buscar, Pageable pageable);
}
