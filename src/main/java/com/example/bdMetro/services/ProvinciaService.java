package com.example.bdMetro.services;

import com.example.bdMetro.entity.Provincia;
import com.example.bdMetro.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinciaService {
    @Autowired
    private ProvinciaRepository provinciaRepository;

    public Provincia addProvincia(Provincia provincia) {
        return provinciaRepository.save(provincia);
    }
    public List<Provincia> getAllProvincias() {
        return provinciaRepository.findAll();
    }

    public Provincia getProvinciaById(Long id) {
        return provinciaRepository.findById(id).orElse(null);
    }
}
