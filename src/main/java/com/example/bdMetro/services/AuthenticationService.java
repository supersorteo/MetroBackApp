package com.example.bdMetro.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.example.bdMetro.util.CountryCatalog;

@Service
public class AuthenticationService {
    @Autowired
    private AccessCodeRepository accessCodeRepository;

    private static final List<String> PROMOCIONALES = Arrays.asList(
            "gPgkyN1", "96XizD2", "pu39k73", "P2QZ954", "VpMwGb5", "2eoHmo6", "oay1o67", "zKHlVm8", "Klsyw09", "VKm5Mc10", "MwUQNu11", "LBaAwg12", "1zEFRj13", "EC42hW14", "S1IATP15", "mdACsM16", "quO4mi17", "YgUCVN18", "qypqH119", "s0xEZX20", "lSlov921", "PzPfbU22", "HWDG3o23", "MmJCGo24", "vB49t925", "jGMSNZ26", "cOCOBO27", "IP8AbG28", "RcPUtD29", "e0a5uT30");

    private static final List<String> CONTRASENAS = Arrays.asList(
            "Andr0meda656", "Yo9eiP", "NsUUbN", "ONkJCl", "03qEbm", "9ErI7P", "W3nnik", "rE5LIs", "U2uNlC", "AsIjg1", "ZEOqc7", "HjUjzp", "SIpeud", "TUpc5S", "CeeM6E", "RpfCY1", "EVCaxp", "T4OXZ2", "8BAg3W", "GcNEET", "lctnJq", "YqJS15", "0Uw9PB", "XURkW0", "5xb3PL", "ORfpbg", "MQsghI", "opUXHQ", "tZTZmn", "QQxfi3", "o3dqkU");

    public String login(String code) {
        AccessCode accessCode = accessCodeRepository.findByCode(code);
        if (accessCode == null) {
            return "Codigo no encontrado";
        } else if (accessCode.getEmail() == null) {
            return "Codigo existe pero no asignado a un usuario";
        } else {
            return accessCode.getEmail();
        }
    }

    public List<AccessCode> getAllCodes() {
        return accessCodeRepository.findAll();
    }

    public List<AccessCode> getCodesByPais(String pais) {
        return accessCodeRepository.findByPaisIgnoreCase(CountryCatalog.displayName(pais));
    }

    public AccessCode getCode(String code) {
        return accessCodeRepository.findByCode(code);
    }

    public AccessCode updateCode(String code, String email, String username, String telefono, String provincia, String pais) {
        AccessCode accessCode = accessCodeRepository.findByCode(code);
        if (accessCode == null) {
            throw new IllegalArgumentException("Este codigo no existe en la base de datos");
        }

        String normalizedEmail = normalizeEmail(email);
        AccessCode emailAssignedCode = normalizedEmail == null ? null : accessCodeRepository.findByEmail(normalizedEmail);
        if (emailAssignedCode != null && !emailAssignedCode.getCode().equals(code)) {
            throw new IllegalArgumentException("Este email ya esta en la base de datos");
        }

        accessCode.setEmail(normalizedEmail);
        accessCode.setTelefono(telefono);
        accessCode.setProvincia(provincia);
        accessCode.setPais(resolveCountryForRegistration(accessCode.getPais(), pais));
        return accessCodeRepository.save(accessCode);
    }

    public AccessCode agregarCode(AccessCode accessCode) {
        validateCodeFormat(accessCode.getCode());
        AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
        String normalizedEmail = normalizeEmail(accessCode.getEmail());
        AccessCode existingEmail = normalizedEmail == null ? null : accessCodeRepository.findByEmail(normalizedEmail);
        if (existingCode != null) {
            throw new IllegalArgumentException("Este codigo ya esta creado");
        }
        if (existingEmail != null) {
            throw new IllegalArgumentException("Este email ya esta creado");
        }
        accessCode.setEmail(normalizedEmail);
        accessCode.setPais(CountryCatalog.displayName(accessCode.getPais()));
        accessCode.setFechaRegistro(LocalDate.now());
        accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
        return accessCodeRepository.save(accessCode);
    }

    public List<AccessCode> agregarCodes(List<AccessCode> accessCodes) {
        for (AccessCode accessCode : accessCodes) {
            validateCodeFormat(accessCode.getCode());
            AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
            if (existingCode != null) {
                throw new IllegalArgumentException("El codigo " + accessCode.getCode() + " ya esta creado");
            }
            accessCode.setEmail(normalizeEmail(accessCode.getEmail()));
            accessCode.setPais(CountryCatalog.displayName(accessCode.getPais()));
            accessCode.setFechaRegistro(LocalDate.now());
            accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
        }
        return accessCodeRepository.saveAll(accessCodes);
    }

    public AccessCode addCode(AccessCode accessCode) {
        AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
        if (existingCode == null) {
            throw new IllegalArgumentException("Codigo no encontrado");
        } else if (existingCode.getEmail() != null) {
            throw new IllegalArgumentException("Codigo ya tiene un email asignado");
        }

        String normalizedEmail = normalizeEmail(accessCode.getEmail());
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("El email es obligatorio");
        }

        AccessCode emailAssignedCode = accessCodeRepository.findByEmail(normalizedEmail);
        if (emailAssignedCode != null) {
            throw new IllegalArgumentException("El email ya esta asignado a otro codigo");
        }

        existingCode.setEmail(normalizedEmail);
        existingCode.setTelefono(accessCode.getTelefono());
        existingCode.setProvincia(accessCode.getProvincia());
        existingCode.setPais(resolveCountryForRegistration(existingCode.getPais(), accessCode.getPais()));
        existingCode.setFechaRegistro(LocalDate.now());
        existingCode.setFechaVencimiento(calcularFechaVencimiento(existingCode.getCode()));
        return accessCodeRepository.save(existingCode);
    }

    private LocalDate calcularFechaVencimiento(String code) {
        validateCodeFormat(code);
        LocalDate fechaRegistro = LocalDate.now();
        return code.length() == 5 ? fechaRegistro.plusMonths(3) : fechaRegistro.plusMonths(6);
    }

    public void deleteCode(String code) {
        accessCodeRepository.deleteById(code);
    }

    private String resolveCountryForRegistration(String existingCountry, String requestedCountry) {
        String normalizedExistingCountry = CountryCatalog.displayName(existingCountry);
        String normalizedRequestedCountry = CountryCatalog.displayName(requestedCountry);

        if (normalizedExistingCountry != null && !normalizedExistingCountry.isBlank()) {
            if (normalizedRequestedCountry != null
                    && !normalizedRequestedCountry.isBlank()
                    && !normalizedExistingCountry.equals(normalizedRequestedCountry)) {
                throw new IllegalArgumentException(
                        "El pais seleccionado no coincide con el pais del codigo. Este codigo pertenece a "
                                + normalizedExistingCountry + ".");
            }
            return normalizedExistingCountry;
        }

        return normalizedRequestedCountry;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private void validateCodeFormat(String code) {
        if (code == null) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }

        String normalizedCode = code.trim().toUpperCase();
        if (!normalizedCode.matches("^[A-Z0-9]{5,6}$")) {
            throw new IllegalArgumentException(
                    "El codigo manual debe tener solo letras y numeros, con 5 caracteres para 3 meses o 6 para 6 meses");
        }

        if (normalizedCode.length() != 5 && normalizedCode.length() != 6) {
            throw new IllegalArgumentException(
                    "El codigo manual debe tener 5 caracteres para 3 meses o 6 para 6 meses");
        }
    }
}
