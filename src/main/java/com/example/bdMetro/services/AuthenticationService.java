package com.example.bdMetro.services;

import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.repository.AccessCodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
@Service
public class AuthenticationService {
   @Autowired
    private AccessCodeRepository accessCodeRepository;

    private static final List<String> PROMOCIONALES = Arrays.asList(
            "gPgkyN1", "96XizD2", "pu39k73", "P2QZ954", "VpMwGb5", "2eoHmo6", "oay1o67", "zKHlVm8", "Klsyw09", "VKm5Mc10", "MwUQNu11", "LBaAwg12", "1zEFRj13", "EC42hW14", "S1IATP15", "mdACsM16", "quO4mi17", "YgUCVN18", "qypqH119", "s0xEZX20", "lSlov921", "PzPfbU22", "HWDG3o23", "MmJCGo24", "vB49t925", "jGMSNZ26", "cOCOBO27", "IP8AbG28", "RcPUtD29", "e0a5uT30" );


    private static final List<String> CONTRASENAS = Arrays.asList(
            "Andr0meda656", "Yo9eiP", "NsUUbN", "ONkJCl", "03qEbm", "9ErI7P", "W3nnik", "rE5LIs", "U2uNlC", "AsIjg1", "ZEOqc7", "HjUjzp", "SIpeud", "TUpc5S", "CeeM6E", "RpfCY1", "EVCaxp", "T4OXZ2", "8BAg3W", "GcNEET", "lctnJq", "YqJS15", "0Uw9PB", "XURkW0", "5xb3PL", "ORfpbg", "MQsghI", "opUXHQ", "tZTZmn", "QQxfi3", "o3dqkU" );


  /*  @PostConstruct
    public void initDatabase() {
        PROMOCIONALES.forEach(code -> {
            if (accessCodeRepository.findByCode(code) == null) {
                AccessCode accessCode = new AccessCode();
                accessCode.setCode(code);
                accessCode.setEmail("promo@example.com");
                accessCodeRepository.save(accessCode);
            }
        });
        CONTRASENAS.forEach(code -> {
            if (accessCodeRepository.findByCode(code) == null) {
                AccessCode accessCode = new AccessCode();
                accessCode.setCode(code);
                accessCode.setEmail("user@example.com");
                accessCodeRepository.save(accessCode);
            }
        });
    }*/

    public String login(String code) {
        AccessCode accessCode = accessCodeRepository.findByCode(code);
        if (accessCode != null) {
            return accessCode.getEmail();
        } else {
            return "Código no encontrado";
        }
    }


    public List<AccessCode> getAllCodes() {
        return accessCodeRepository.findAll();
    }

    public AccessCode getCode(String code) {
        return accessCodeRepository.findByCode(code);
    }


   public AccessCode updateCode(String code, String email, String username, String telefono, String provincia) {
       AccessCode accessCode = accessCodeRepository.findByCode(code);
       if (accessCode == null) {
           throw new IllegalArgumentException("Este código no existe en la base de datos");
       }

       // Verificar que el email no esté ya asignado a otro código
       AccessCode emailAssignedCode = accessCodeRepository.findByEmail(email);
       if (emailAssignedCode != null && !emailAssignedCode.getCode().equals(code)) {
           throw new IllegalArgumentException("Este email está en la base de datos");
       }

       // Actualizar los campos
       accessCode.setEmail(email);
       accessCode.setUsername(username);
       accessCode.setTelefono(telefono);
       accessCode.setProvincia(provincia);
       return accessCodeRepository.save(accessCode);
   }



    public AccessCode agregarCode(AccessCode accessCode) {
        AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
        AccessCode existingEmail = accessCodeRepository.findByEmail(accessCode.getEmail());
        if (existingCode != null) {
            throw new IllegalArgumentException("Este código ya está creado");
        } if (existingEmail != null) {
            throw new IllegalArgumentException("Este email ya está creado");
        } accessCode.setFechaRegistro(LocalDate.now());
        accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
        return accessCodeRepository.save(accessCode);
    }



    public List<AccessCode> agregarCodes(List<AccessCode> accessCodes) {
        for (AccessCode accessCode : accessCodes) {
            AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
            if (existingCode != null) {
                throw new IllegalArgumentException("El código " + accessCode.getCode() + " ya está creado");
            } accessCode.setFechaRegistro(LocalDate.now());
            accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
        } return accessCodeRepository.saveAll(accessCodes);
    }


    public AccessCode addCode(AccessCode accessCode) {
        AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
        if (existingCode == null) {
            throw new IllegalArgumentException("Código no encontrado");
        } else if (existingCode.getEmail() != null) {
            throw new IllegalArgumentException("Código ya tiene un email asignado");
        } AccessCode emailAssignedCode = accessCodeRepository.findAll().stream() .filter(code -> accessCode.getEmail().equals(code.getEmail())) .findFirst() .orElse(null);
        if (emailAssignedCode != null) {
            throw new IllegalArgumentException("El email ya está asignado a otro código");
        }
        existingCode.setEmail(accessCode.getEmail());
        existingCode.setUsername(accessCode.getUsername());
        existingCode.setTelefono(accessCode.getTelefono());
        existingCode.setProvincia(accessCode.getProvincia());
        existingCode.setFechaRegistro(LocalDate.now());
        existingCode.setFechaVencimiento(calcularFechaVencimiento(existingCode.getCode()));
        return accessCodeRepository.save(existingCode);
    }

    private LocalDate calcularFechaVencimiento(String code) {
        LocalDate fechaRegistro = LocalDate.now();
        return code.length() == 5 ? fechaRegistro.plusMonths(3) : fechaRegistro.plusMonths(6);
    }

    public void deleteCode(String code) {
        accessCodeRepository.deleteById(code);
    }
}
