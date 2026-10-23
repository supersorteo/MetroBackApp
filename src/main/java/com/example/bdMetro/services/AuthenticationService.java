package com.example.bdMetro.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bdMetro.dto.UserDataSummaryDto;
import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.entity.UserSession;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.example.bdMetro.repository.CalculoMaterialRepository;
import com.example.bdMetro.repository.ClienteRepository;
import com.example.bdMetro.repository.EmpresaRepository;
import com.example.bdMetro.repository.PresupuestoRepository;
import com.example.bdMetro.repository.TareaPersonalizadaRepository;
import com.example.bdMetro.repository.UserSessionRepository;
import com.example.bdMetro.repository.UserTareaRepository;
import com.example.bdMetro.util.CountryCatalog;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {
    @Autowired private AccessCodeRepository accessCodeRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PresupuestoRepository presupuestoRepository;
    @Autowired private UserTareaRepository userTareaRepository;
    @Autowired private TareaPersonalizadaRepository tareaPersonalizadaRepository;
    @Autowired private CalculoMaterialRepository calculoMaterialRepository;
    @Autowired private UserSessionRepository userSessionRepository;

    private static final int MAX_DEVICES = 2;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    private String norm(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    public String login(String code) {
        AccessCode accessCode = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (accessCode == null) {
            return "Codigo no encontrado";
        } else if (accessCode.getEmail() == null) {
            return "Codigo existe pero no asignado a un usuario";
        } else if (accessCode.isDisabled()) {
            return "Codigo desactivado";
        } else {
            return accessCode.getEmail();
        }
    } 

    public List<AccessCode> getAllCodes() {
        return accessCodeRepository.findAll();
    }

    public List<AccessCode> getCodesByPais(String pais) {
        return accessCodeRepository.findByPaisIgnoreCaseAndLegacyFalse(CountryCatalog.displayName(pais));
    }

    public AccessCode getCode(String code) {
        return accessCodeRepository.findByCodeIgnoreCase(norm(code));
    }

    @Transactional
    public AccessCode updateCode(String code, String email, String username, String telefono, String provincia, String pais) {
        String normalizedCode = norm(code);
        AccessCode accessCode = accessCodeRepository.findByCodeIgnoreCase(normalizedCode);
        if (accessCode == null) {
            throw new IllegalArgumentException("Este codigo no existe en la base de datos");
        }

        String normalizedEmail = normalizeEmail(email);
        AccessCode emailAssignedCode = normalizedEmail == null ? null : accessCodeRepository.findByEmail(normalizedEmail);
        if (emailAssignedCode != null && !emailAssignedCode.getCode().equals(normalizedCode)) {
            throw new IllegalArgumentException("Este email ya esta en la base de datos");
        }

        boolean firstAssignment = accessCode.getEmail() == null && normalizedEmail != null;
        accessCode.setEmail(normalizedEmail);
        accessCode.setTelefono(telefono);
        accessCode.setProvincia(provincia);
        accessCode.setPais(resolveCountryForRegistration(accessCode.getPais(), pais));
        if (firstAssignment) {
            accessCode.setFechaRegistro(LocalDate.now());
            accessCode.setFechaVencimiento(calcularFechaVencimiento(normalizedCode));
        }
        return accessCodeRepository.save(accessCode);
    }

    @Transactional
    public AccessCode agregarCode(AccessCode accessCode) {
        accessCode.setCode(norm(accessCode.getCode()));
        validateCodeFormat(accessCode.getCode());
        AccessCode existingCode = accessCodeRepository.findByCodeIgnoreCase(accessCode.getCode());
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
        if (normalizedEmail != null) {
            accessCode.setFechaRegistro(LocalDate.now());
            accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
        }
        return accessCodeRepository.save(accessCode);
    }

    @Transactional
    public List<AccessCode> agregarCodes(List<AccessCode> accessCodes) {
        for (AccessCode accessCode : accessCodes) {
            accessCode.setCode(norm(accessCode.getCode()));
            validateCodeFormat(accessCode.getCode());
            AccessCode existingCode = accessCodeRepository.findByCodeIgnoreCase(accessCode.getCode());
            if (existingCode != null) {
                throw new IllegalArgumentException("El codigo " + accessCode.getCode() + " ya esta creado");
            }
            accessCode.setEmail(normalizeEmail(accessCode.getEmail()));
            accessCode.setPais(CountryCatalog.displayName(accessCode.getPais()));
            if (accessCode.getEmail() != null) {
                accessCode.setFechaRegistro(LocalDate.now());
                accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
            }
        }
        return accessCodeRepository.saveAll(accessCodes);
    }

    @Transactional
    public AccessCode addCode(AccessCode accessCode) {
        accessCode.setCode(norm(accessCode.getCode()));
        AccessCode existingCode = accessCodeRepository.findByCodeIgnoreCase(accessCode.getCode());
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
        return switch (code.length()) {
            case 5  -> fechaRegistro.plusMonths(3);
            case 6  -> fechaRegistro.plusMonths(6);
            default -> fechaRegistro.plusMonths(12);
        };
    }

    @Transactional
    public AccessCode updatePreferences(String code, Long selectedEmpresaId, Long selectedClienteId) {
        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (ac == null) throw new IllegalArgumentException("Codigo no encontrado");
        ac.setSelectedEmpresaId(selectedEmpresaId);
        ac.setSelectedClienteId(selectedClienteId);
        return accessCodeRepository.save(ac);
    }

    @Transactional
    public void deleteCode(String code) {
        accessCodeRepository.deleteById(norm(code));
    }

    @Transactional
    public AccessCode save(AccessCode ac) {
        return accessCodeRepository.save(ac);
    }

    @Transactional
    public AccessCode reactivateCode(String expiredCode, String newCode, String email, String deviceId) {
        String normExpired = norm(expiredCode);
        String normNew = norm(newCode);
        String normEmail = normalizeEmail(email);

        AccessCode expired = accessCodeRepository.findByCodeIgnoreCase(normExpired);
        if (expired == null)
            throw new IllegalArgumentException("El código anterior no existe.");
        if (expired.getEmail() == null)
            throw new IllegalArgumentException("El código anterior no tiene usuario asociado.");
        if (!expired.getEmail().equals(normEmail))
            throw new IllegalArgumentException("El email no coincide con el registrado para ese código.");

        AccessCode newAc = accessCodeRepository.findByCodeIgnoreCase(normNew);
        if (newAc == null)
            throw new IllegalArgumentException("El nuevo código no existe.");
        if (newAc.getEmail() != null)
            throw new IllegalArgumentException("El nuevo código ya está asignado a otro usuario.");

        // Capturar datos antes de modificar el código vencido
        String emailToTransfer     = expired.getEmail();
        String telefonoToTransfer  = expired.getTelefono();
        String provinciaToTransfer = expired.getProvincia();
        String paisToTransfer      = expired.getPais();

        // Transferir todos los datos al nuevo código
        empresaRepository.updateUserCode(normExpired, normNew);
        clienteRepository.updateUserCode(normExpired, normNew);
        presupuestoRepository.updateUserCode(normExpired, normNew);
        tareaPersonalizadaRepository.updateUserCode(normExpired, normNew);
        userTareaRepository.updateUserCode(normExpired, normNew);
        calculoMaterialRepository.updateUserCode(normExpired, normNew);

        // Liberar el email del código vencido ANTES de asignarlo al nuevo para evitar
        // violación del constraint UNIQUE en email (Hibernate puede ordenar el INSERT
        // del nuevo antes del DELETE del viejo dentro del mismo flush)
        expired.setEmail(null);
        accessCodeRepository.saveAndFlush(expired);

        // Copiar perfil al nuevo código
        newAc.setEmail(emailToTransfer);
        newAc.setTelefono(telefonoToTransfer);
        newAc.setProvincia(provinciaToTransfer);
        if (newAc.getPais() == null || newAc.getPais().isBlank()) {
            newAc.setPais(paisToTransfer);
        }
        newAc.setFechaRegistro(LocalDate.now());
        newAc.setFechaVencimiento(calcularFechaVencimiento(normNew));
        accessCodeRepository.save(newAc);

        // Limpiar sesiones del código vencido antes de eliminarlo (FK constraint)
        userSessionRepository.deleteByAccessCodeCode(normExpired);

        // Eliminar el código vencido — el slot ya cumplió su ciclo de vida
        accessCodeRepository.delete(expired);

        // Crear sesión para el dispositivo que hizo la reactivación para que no quede deslogueado
        if (deviceId != null && !deviceId.isBlank()) {
            String token = loginWithDevice(normNew, deviceId);
            newAc.setSessionToken(token);
        }

        return newAc;
    }

    @Transactional
    public int cleanExpiredDataOlderThan(int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        List<AccessCode> toClean = accessCodeRepository.findExpiredWithEmail(cutoff);
        for (AccessCode ac : toClean) {
            deleteUserData(ac.getCode());
        }
        return toClean.size();
    }

    @Transactional
    public AccessCode disableCode(String code) {
        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (ac == null) throw new IllegalArgumentException("Codigo no encontrado");
        ac.setDisabled(true);
        return accessCodeRepository.save(ac);
    }

    @Transactional
    public AccessCode enableCode(String code) {
        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (ac == null) throw new IllegalArgumentException("Codigo no encontrado");
        ac.setDisabled(false);
        return accessCodeRepository.save(ac);
    }

    public UserDataSummaryDto getUserDataSummary(String code) {
        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (ac == null || ac.getEmail() == null) return null;
        long empresas   = empresaRepository.countByUserCode(code);
        long clientes   = clienteRepository.countByUserCode(code);
        long presupuestos = presupuestoRepository.countByClienteUserCode(code);
        long tareas     = tareaPersonalizadaRepository.countByUserCode(code);
        return new UserDataSummaryDto(ac.getEmail(), empresas, clientes, presupuestos, tareas);
    }

    @Transactional
    public void deleteUserData(String code) {
        // 1. IDs de clientes del usuario
        List<Long> clienteIds = clienteRepository.findIdsByUserCode(code);

        // 2. UserTareas (soft-delete por userCode)
        userTareaRepository.softDeleteAllByUserCode(code);

        // 3. Presupuestos (hard-delete — FK a Cliente no puede quedar huérfana)
        if (!clienteIds.isEmpty()) {
            presupuestoRepository.deleteAllByClienteIdIn(clienteIds);
        }

        // 4. Clientes (hard-delete)
        clienteRepository.deleteByUserCode(code);

        // 5. Empresas (hard-delete)
        empresaRepository.deleteByUserCode(code);

        // 6. TareasPersonalizadas y CalculoMateriales
        tareaPersonalizadaRepository.deleteByUserCode(code);
        calculoMaterialRepository.deleteByUserCode(code);

        // 7. Resetear AccessCode al estado libre (conservar el slot)
        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (ac != null) {
            ac.setEmail(null);
            ac.setTelefono(null);
            ac.setProvincia(null);
            ac.setFechaRegistro(null);
            ac.setFechaVencimiento(null);
            ac.setDisabled(false);
            accessCodeRepository.save(ac);
        }
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

    // ── Session management ──────────────────────────────────────────────────

    /** Valida el código y crea o refresca una sesión para el dispositivo dado.
     *  Lanza IllegalStateException (→ 409) si ya hay MAX_DEVICES sesiones activas
     *  de otros dispositivos. */
    @Transactional
    public String loginWithDevice(String code, String deviceId) {
        String normCode = norm(code);
        String effectiveDeviceId = (deviceId == null || deviceId.isBlank())
                ? "unknown-" + UUID.randomUUID()
                : deviceId;

        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(normCode);
        // Usar el código exacto almacenado en DB para queries de sesión (evita mismatch de mayúsculas)
        String codeKey = (ac != null) ? ac.getCode() : normCode;

        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(SESSION_TIMEOUT_MINUTES * 60L);

        // Reusar sesión del mismo dispositivo si ya existe (activa o no)
        Optional<UserSession> existing = userSessionRepository
                .findByAccessCodeCodeAndDeviceId(codeKey, effectiveDeviceId);
        if (existing.isPresent()) {
            UserSession session = existing.get();
            session.setLastHeartbeat(now);
            userSessionRepository.save(session);
            return session.getId();
        }

        // Contar sesiones activas de otros dispositivos
        int limit = (ac != null && ac.getMaxDevices() != null) ? ac.getMaxDevices() : MAX_DEVICES;
        int active = userSessionRepository.countByAccessCodeCodeAndLastHeartbeatAfter(codeKey, cutoff);
        if (active >= limit) {
            throw new IllegalStateException(
                    "Límite de " + limit + " dispositivos activos alcanzado. " +
                    "Cerrá sesión en otro dispositivo para continuar.");
        }

        UserSession session = new UserSession();
        session.setId(UUID.randomUUID().toString());
        session.setAccessCode(ac);
        session.setDeviceId(effectiveDeviceId);
        session.setLastHeartbeat(now);
        session.setCreatedAt(now);
        userSessionRepository.save(session);
        return session.getId();
    }

    /** Actualiza el lastHeartbeat. Devuelve true si la sesión existe, false si fue terminada externamente. */
    @Transactional
    public boolean updateHeartbeat(String sessionToken) {
        return userSessionRepository.findById(sessionToken).map(s -> {
            s.setLastHeartbeat(Instant.now());
            userSessionRepository.save(s);
            return true;
        }).orElse(false);
    }

    /** Elimina la sesión (logout explícito). */
    @Transactional
    public void terminateSession(String sessionToken) {
        userSessionRepository.deleteById(sessionToken);
    }

    /** Cantidad de sesiones activas para un código (ventana de SESSION_TIMEOUT_MINUTES). */
    public int getActiveSessionCount(String code) {
        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (ac == null) return 0;
        Instant cutoff = Instant.now().minusSeconds(SESSION_TIMEOUT_MINUTES * 60L);
        return userSessionRepository.countByAccessCodeCodeAndLastHeartbeatAfter(ac.getCode(), cutoff);
    }

    /** Elimina todas las sesiones activas de un código (kick de todos los dispositivos). */
    @Transactional
    public void terminateAllSessions(String code) {
        AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(norm(code));
        if (ac == null) return;
        userSessionRepository.deleteByAccessCodeCode(ac.getCode());
    }

    /** Borra físicamente sesiones con lastHeartbeat anterior al TTL (limpieza periódica). */
    @Transactional
    public void cleanStaleSessions() {
        Instant cutoff = Instant.now().minusSeconds(24 * 60 * 60L);
        userSessionRepository.deleteStaleSessionsBefore(cutoff);
    }

    private void validateCodeFormat(String code) {
        if (code == null) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }

        String normalizedCode = code.trim().toUpperCase();
        if (!normalizedCode.matches("^[A-Z0-9]{5,7}$")) {
            throw new IllegalArgumentException(
                    "El codigo debe tener solo letras y numeros: 5 caracteres (3 meses), 6 caracteres (6 meses) o 7 caracteres (12 meses)");
        }

        int len = normalizedCode.length();
        if (len != 5 && len != 6 && len != 7) {
            throw new IllegalArgumentException(
                    "El codigo debe tener 5 caracteres (3 meses), 6 caracteres (6 meses) o 7 caracteres (12 meses)");
        }
    }
}
