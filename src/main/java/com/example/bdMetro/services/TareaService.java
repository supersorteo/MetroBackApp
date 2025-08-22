package com.example.bdMetro.services;

import com.example.bdMetro.entity.Tarea;
import com.example.bdMetro.repository.TareaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TareaService {
    @Autowired
    private TareaRepository tareaRepository;


    @PostConstruct
    public void initTareas() {
        tareaRepository.deleteAll(); // Limpiar tabla para evitar duplicados

        // Tareas para Argentina
        tareaRepository.save(new Tarea("Construcción de pared", 100.0, "Construcción", "Mampostería", "Argentina", "Pared de ladrillo", 0.0, 1.0));
        tareaRepository.save(new Tarea("Pintura interior", 50.0, "Pintura", "Acabados", "Argentina", "Pintura látex", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación eléctrica", 200.0, "Electricidad", "Instalaciones", "Argentina", "Cableado básico", 0.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de cerámicos", 80.0, "Construcción", "Pisos", "Argentina", "Cerámicos 40x40", 10.0, 1.0));
        tareaRepository.save(new Tarea("Techo de chapa", 150.0, "Construcción", "Techos", "Argentina", "Chapa galvanizada", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de cloacas", 300.0, "Plomería", "Instalaciones", "Argentina", "Sistema cloacal", 0.0, 1.0));
        tareaRepository.save(new Tarea("Revoque fino", 60.0, "Construcción", "Acabados", "Argentina", "Revoque interior", 5.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de aberturas", 120.0, "Carpintería", "Aberturas", "Argentina", "Ventanas de aluminio", 0.0, 1.0));
        tareaRepository.save(new Tarea("Construcción de losa", 250.0, "Construcción", "Estructura", "Argentina", "Losa de hormigón", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de gas", 180.0, "Plomería", "Instalaciones", "Argentina", "Cañerías de gas", 0.0, 1.0));
        tareaRepository.save(new Tarea("Pintura exterior", 70.0, "Pintura", "Acabados", "Argentina", "Pintura impermeabilizante", 5.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de zócalos", 40.0, "Construcción", "Acabados", "Argentina", "Zócalos cerámicos", 0.0, 1.0));
        tareaRepository.save(new Tarea("Hormigonado de pisos", 90.0, "Construcción", "Pisos", "Argentina", "Piso de hormigón", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de sanitarios", 150.0, "Plomería", "Instalaciones", "Argentina", "Inodoro y bidet", 0.0, 1.0));
        tareaRepository.save(new Tarea("Construcción de cimientos", 200.0, "Construcción", "Estructura", "Argentina", "Cimientos de hormigón", 0.0, 1.0));
        tareaRepository.save(new Tarea("Revestimiento de paredes", 80.0, "Construcción", "Acabados", "Argentina", "Revestimiento cerámico", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de aire acondicionado", 250.0, "Climatización", "Instalaciones", "Argentina", "Split 3000 frigorías", 0.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de techos de madera", 180.0, "Carpintería", "Techos", "Argentina", "Techo de madera machimbrada", 0.0, 1.0));
        tareaRepository.save(new Tarea("Impermeabilización de techos", 100.0, "Construcción", "Techos", "Argentina", "Membrana asfáltica", 0.0, 1.0));
        tareaRepository.save(new Tarea("Tarea genérica", 50.0, "General", "Otros", "Argentina", "Tarea personalizable", 0.0, 1.0));

        // Tareas para Colombia
        tareaRepository.save(new Tarea("Construcción de muro", 90.0, "Construcción", "Mampostería", "Colombia", "Muro de bloque", 0.0, 1.0));
        tareaRepository.save(new Tarea("Pintura interior", 45.0, "Pintura", "Acabados", "Colombia", "Pintura acrílica", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación eléctrica", 180.0, "Electricidad", "Instalaciones", "Colombia", "Cableado residencial", 0.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de baldosas", 70.0, "Construcción", "Pisos", "Colombia", "Baldosas 50x50", 10.0, 1.0));
        tareaRepository.save(new Tarea("Techo de teja", 140.0, "Construcción", "Techos", "Colombia", "Teja de barro", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de alcantarillado", 280.0, "Plomería", "Instalaciones", "Colombia", "Sistema de alcantarillado", 0.0, 1.0));
        tareaRepository.save(new Tarea("Estuco de paredes", 55.0, "Construcción", "Acabados", "Colombia", "Estuco liso", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de ventanas", 110.0, "Carpintería", "Aberturas", "Colombia", "Ventanas de vidrio", 0.0, 1.0));
        tareaRepository.save(new Tarea("Construcción de losa", 230.0, "Construcción", "Estructura", "Colombia", "Losa de concreto", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de gas", 170.0, "Plomería", "Instalaciones", "Colombia", "Cañerías de gas", 0.0, 1.0));
        tareaRepository.save(new Tarea("Pintura exterior", 65.0, "Pintura", "Acabados", "Colombia", "Pintura impermeabilizante", 5.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de rodapiés", 35.0, "Construcción", "Acabados", "Colombia", "Rodapiés cerámicos", 0.0, 1.0));
        tareaRepository.save(new Tarea("Concreto para pisos", 85.0, "Construcción", "Pisos", "Colombia", "Piso de concreto pulido", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de sanitarios", 140.0, "Plomería", "Instalaciones", "Colombia", "Inodoro y lavamanos", 0.0, 1.0));
        tareaRepository.save(new Tarea("Construcción de zapatas", 190.0, "Construcción", "Estructura", "Colombia", "Zapatas de concreto", 0.0, 1.0));
        tareaRepository.save(new Tarea("Revestimiento de muros", 75.0, "Construcción", "Acabados", "Colombia", "Revestimiento cerámico", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de aire acondicionado", 240.0, "Climatización", "Instalaciones", "Colombia", "Split 2500 frigorías", 0.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de techos de madera", 170.0, "Carpintería", "Techos", "Colombia", "Techo de madera", 0.0, 1.0));
        tareaRepository.save(new Tarea("Impermeabilización de techos", 95.0, "Construcción", "Techos", "Colombia", "Membrana líquida", 0.0, 1.0));
        tareaRepository.save(new Tarea("Tarea genérica", 45.0, "General", "Otros", "Colombia", "Tarea personalizable", 0.0, 1.0));

        // Tareas para Perú
        tareaRepository.save(new Tarea("Construcción de pared", 95.0, "Construcción", "Mampostería", "Peru", "Pared de adobe", 0.0, 1.0));
        tareaRepository.save(new Tarea("Pintura interior", 48.0, "Pintura", "Acabados", "Peru", "Pintura látex", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación eléctrica", 190.0, "Electricidad", "Instalaciones", "Peru", "Cableado residencial", 0.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de pisos", 75.0, "Construcción", "Pisos", "Peru", "Pisos cerámicos", 10.0, 1.0));
        tareaRepository.save(new Tarea("Techo de calamina", 145.0, "Construcción", "Techos", "Peru", "Calamina galvanizada", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de desagües", 290.0, "Plomería", "Instalaciones", "Peru", "Sistema de desagüe", 0.0, 1.0));
        tareaRepository.save(new Tarea("Enlucido de paredes", 58.0, "Construcción", "Acabados", "Peru", "Enlucido liso", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de puertas", 115.0, "Carpintería", "Aberturas", "Peru", "Puertas de madera", 0.0, 1.0));
        tareaRepository.save(new Tarea("Construcción de losa", 240.0, "Construcción", "Estructura", "Peru", "Losa de concreto armado", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de gas", 175.0, "Plomería", "Instalaciones", "Peru", "Cañerías de gas", 0.0, 1.0));
        tareaRepository.save(new Tarea("Pintura exterior", 68.0, "Pintura", "Acabados", "Peru", "Pintura impermeabilizante", 5.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de zócalos", 38.0, "Construcción", "Acabados", "Peru", "Zócalos cerámicos", 0.0, 1.0));
        tareaRepository.save(new Tarea("Hormigonado de pisos", 88.0, "Construcción", "Pisos", "Peru", "Piso de concreto", 0.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de sanitarios", 145.0, "Plomería", "Instalaciones", "Peru", "Inodoro y lavabo", 0.0, 1.0));
        tareaRepository.save(new Tarea("Construcción de cimientos", 195.0, "Construcción", "Estructura", "Peru", "Cimientos de concreto", 0.0, 1.0));
        tareaRepository.save(new Tarea("Revestimiento de paredes", 78.0, "Construcción", "Acabados", "Peru", "Revestimiento de piedra", 5.0, 1.0));
        tareaRepository.save(new Tarea("Instalación de aire acondicionado", 245.0, "Climatización", "Instalaciones", "Peru", "Split 3000 frigorías", 0.0, 1.0));
        tareaRepository.save(new Tarea("Colocación de techos de madera", 175.0, "Carpintería", "Techos", "Peru", "Techo de madera", 0.0, 1.0));
        tareaRepository.save(new Tarea("Impermeabilización de techos", 98.0, "Construcción", "Techos", "Peru", "Membrana asfáltica", 0.0, 1.0));
        tareaRepository.save(new Tarea("Tarea genérica", 48.0, "General", "Otros", "Peru", "Tarea personalizable", 0.0, 1.0));

        System.out.println("Tareas cargadas correctamente");
    }


    public List<Tarea> getAllTareas() {
        return tareaRepository.findAll();
    }

    public List<Tarea> getTareasByPais(String pais) {
        return tareaRepository.findByPais(pais); // Added for country filtering
    }

    public Optional<Tarea> getTareaById(Long id) {
        return tareaRepository.findById(id);
    }

    public Tarea addTarea(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public Tarea updateTarea(Long id, Tarea tareaDetails) {
        Tarea tarea = tareaRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        tarea.setTarea(tareaDetails.getTarea());
        tarea.setCosto(tareaDetails.getCosto());
        tarea.setRubro(tareaDetails.getRubro());
        tarea.setCategoria(tareaDetails.getCategoria());
        tarea.setPais(tareaDetails.getPais());
        tarea.setDescripcion(tareaDetails.getDescripcion());
        tarea.setDescuento(tareaDetails.getDescuento());
        tarea.setArea(tareaDetails.getArea());
        return tareaRepository.save(tarea);
    }
    public void deleteTarea(Long id) {
        tareaRepository.deleteById(id);
    }

}
