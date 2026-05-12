package com.example.bdMetro.services;

import com.example.bdMetro.entity.TareaPersonalizada;
import com.example.bdMetro.repository.TareaPersonalizadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TareaPersonalizadaService {

    @Autowired
    private TareaPersonalizadaRepository repository;

    public List<TareaPersonalizada> getByUserCode(String userCode) {
        return repository.findByUserCodeAndDeletedFalse(userCode);
    }

    public TareaPersonalizada create(TareaPersonalizada tarea) {
        tarea.setDeleted(false);
        return repository.save(tarea);
    }

    public TareaPersonalizada update(Long id, TareaPersonalizada details) {
        TareaPersonalizada existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TareaPersonalizada no encontrada"));
        existing.setTarea(details.getTarea());
        existing.setDescripcion(details.getDescripcion());
        existing.setCosto(details.getCosto());
        return repository.save(existing);
    }

    public void delete(Long id) {
        TareaPersonalizada existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TareaPersonalizada no encontrada"));
        existing.setDeleted(true);
        repository.save(existing);
    }
}
