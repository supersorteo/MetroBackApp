package com.example.bdMetro.services;

import com.example.bdMetro.entity.Provincia;
import com.example.bdMetro.repository.ProvinciaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProvinciaService {
    @Autowired
    private ProvinciaRepository provinciaRepository;

    @PostConstruct
    public void initProvincias() {
        if (provinciaRepository.count() == 0) {

            provinciaRepository.deleteAll();
            // Argentina (23 provincias + CABA)
            provinciaRepository.save(new Provincia(null, "Buenos Aires", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Ciudad Autónoma de Buenos Aires", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Catamarca", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Chaco", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Chubut", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Córdoba", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Corrientes", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Entre Ríos", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Formosa", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Jujuy", "Argentina"));
            provinciaRepository.save(new Provincia(null, "La Pampa", "Argentina"));
            provinciaRepository.save(new Provincia(null, "La Rioja", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Mendoza", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Misiones", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Neuquén", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Río Negro", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Salta", "Argentina"));
            provinciaRepository.save(new Provincia(null, "San Juan", "Argentina"));
            provinciaRepository.save(new Provincia(null, "San Luis", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Santa Cruz", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Santa Fe", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Santiago del Estero", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Tierra del Fuego", "Argentina"));
            provinciaRepository.save(new Provincia(null, "Tucumán", "Argentina"));

            // Colombia (32 departamentos + Bogotá D.C.)
            provinciaRepository.save(new Provincia(null, "Amazonas", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Antioquia", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Arauca", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Atlántico", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Bogotá D.C.", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Bolívar", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Boyacá", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Caldas", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Caquetá", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Casanare", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Cauca", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Cesar", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Chocó", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Córdoba", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Cundinamarca", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Guainía", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Guaviare", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Huila", "Colombia"));
            provinciaRepository.save(new Provincia(null, "La Guajira", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Magdalena", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Meta", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Nariño", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Norte de Santander", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Putumayo", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Quindío", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Risaralda", "Colombia"));
            provinciaRepository.save(new Provincia(null, "San Andrés y Providencia", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Santander", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Sucre", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Tolima", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Valle del Cauca", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Vaupés", "Colombia"));
            provinciaRepository.save(new Provincia(null, "Vichada", "Colombia"));

            // Perú (24 departamentos + Callao)
            provinciaRepository.save(new Provincia(null, "Amazonas", "Peru"));
            provinciaRepository.save(new Provincia(null, "Áncash", "Peru"));
            provinciaRepository.save(new Provincia(null, "Apurímac", "Peru"));
            provinciaRepository.save(new Provincia(null, "Arequipa", "Peru"));
            provinciaRepository.save(new Provincia(null, "Ayacucho", "Peru"));
            provinciaRepository.save(new Provincia(null, "Cajamarca", "Peru"));
            provinciaRepository.save(new Provincia(null, "Callao", "Peru"));
            provinciaRepository.save(new Provincia(null, "Cusco", "Peru"));
            provinciaRepository.save(new Provincia(null, "Huancavelica", "Peru"));
            provinciaRepository.save(new Provincia(null, "Huánuco", "Peru"));
            provinciaRepository.save(new Provincia(null, "Ica", "Peru"));
            provinciaRepository.save(new Provincia(null, "Junín", "Peru"));
            provinciaRepository.save(new Provincia(null, "La Libertad", "Peru"));
            provinciaRepository.save(new Provincia(null, "Lambayeque", "Peru"));
            provinciaRepository.save(new Provincia(null, "Lima", "Peru"));
            provinciaRepository.save(new Provincia(null, "Loreto", "Peru"));
            provinciaRepository.save(new Provincia(null, "Madre de Dios", "Peru"));
            provinciaRepository.save(new Provincia(null, "Moquegua", "Peru"));
            provinciaRepository.save(new Provincia(null, "Pasco", "Peru"));
            provinciaRepository.save(new Provincia(null, "Piura", "Peru"));
            provinciaRepository.save(new Provincia(null, "Puno", "Peru"));
            provinciaRepository.save(new Provincia(null, "San Martín", "Peru"));
            provinciaRepository.save(new Provincia(null, "Tacna", "Peru"));
            provinciaRepository.save(new Provincia(null, "Tumbes", "Peru"));
            provinciaRepository.save(new Provincia(null, "Ucayali", "Peru"));
        }
    }





    public Provincia addProvincia(Provincia provincia) {
        return provinciaRepository.save(provincia);
    }
    public List<Provincia> getAllProvincias() {
        return provinciaRepository.findAll();
    }

    public List<Provincia> getProvinciasByPais(String pais) {
        return provinciaRepository.findByPais(pais); // Added for country filtering
    }



    public Optional<Provincia> getProvinciaById(Long id) {
        return provinciaRepository.findById(id);
    }

    public Provincia updateProvincia(Long id, Provincia provinciaDetails) {
        Provincia provincia = provinciaRepository.findById(id).orElseThrow(() -> new RuntimeException("Provincia no encontrada"));
        provincia.setNombre(provinciaDetails.getNombre());
        provincia.setPais(provinciaDetails.getPais()); // Added
        return provinciaRepository.save(provincia);
    }

    public void deleteProvincia(Long id) {
        provinciaRepository.deleteById(id);
    }

}
