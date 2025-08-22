package com.example.bdMetro.services;

import com.example.bdMetro.entity.Empresa;
import com.example.bdMetro.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {
    @Autowired
    private EmpresaRepository empresaRepository;



    public List<Empresa> getEmpresasByUserCode(String userCode) {  // Plural y List
        return empresaRepository.findByUserCode(userCode);
    }


    public Optional<Empresa> getEmpresaById(Long id) {
        return empresaRepository.findById(id);
    }

    public List<Empresa> getAllEmpresas() {
        return empresaRepository.findAll();
    }

    /*
    public Empresa saveEmpresa(Empresa empresa) {
        return empresaRepository.save(empresa);
    }*/


    /*
    public Empresa saveEmpresa(Empresa empresa) {
        if (empresa.getUserCode() == null || empresa.getUserCode().isEmpty()) {
            throw new IllegalArgumentException("El userCode es requerido para guardar la empresa");
        }
        Empresa existingEmpresa = empresaRepository.findByUserCodeAndIdNot(empresa.getUserCode(), -1L);
        if (existingEmpresa != null) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el userCode '" + empresa.getUserCode() + "'");
        }
        return empresaRepository.save(empresa);
    }*/



    public Empresa saveEmpresa(Empresa empresa) {
        if (empresa.getUserCode() == null || empresa.getUserCode().isEmpty()) {
            throw new IllegalArgumentException("El userCode es requerido para guardar la empresa");
        }
        return empresaRepository.save(empresa);
    }

    public Empresa updateEmpresa(Long id, Empresa empresaDetails) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        empresa.setName(empresaDetails.getName());
        empresa.setPhone(empresaDetails.getPhone());
        empresa.setEmail(empresaDetails.getEmail());
        empresa.setDescription(empresaDetails.getDescription());
        empresa.setLogoUrl(empresaDetails.getLogoUrl());
        return empresaRepository.save(empresa);
    }

    /*
    public void deleteEmpresa(Long id) {
        empresaRepository.deleteById(id);
    }*/



    public void deleteEmpresa(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new RuntimeException("Empresa no encontrada");
        }
        empresaRepository.deleteById(id);
    }


}
