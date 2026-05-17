package com.example.bdMetro.services;

import com.example.bdMetro.dto.AdminMembershipLimitsDto;
import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.entity.AdminPanel;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.example.bdMetro.repository.AdminPanelRepository;
import com.example.bdMetro.util.CountryCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class MembershipLimitService {

    private static final int DEMO_DEFAULT_MAX_EMPRESAS = 3;
    private static final int DEMO_DEFAULT_MAX_CLIENTES = 6;
    private static final int VIP3_DEFAULT_MAX_EMPRESAS = 1;
    private static final int VIP3_DEFAULT_MAX_CLIENTES = 30;
    private static final int VIP6_DEFAULT_MAX_EMPRESAS = 3;
    private static final int VIP6_DEFAULT_MAX_CLIENTES = 60;
    private static final long VIP6_THRESHOLD_DAYS = 150;

    @Autowired
    private AccessCodeRepository accessCodeRepository;

    @Autowired
    private AdminPanelRepository adminPanelRepository;

    public int resolveEmpresaLimit(String userCode) {
        AccessCode accessCode = requireAccessCode(userCode);
        AdminMembershipLimitsDto limits = resolveLimitsByPais(accessCode.getPais());
        return switch (resolvePlanTier(accessCode)) {
            case VIP_6 -> safeLimit(limits.getVip6MaxEmpresas(), VIP6_DEFAULT_MAX_EMPRESAS);
            case VIP_3 -> safeLimit(limits.getVip3MaxEmpresas(), VIP3_DEFAULT_MAX_EMPRESAS);
        };
    }

    public int resolveClienteLimit(String userCode) {
        AccessCode accessCode = requireAccessCode(userCode);
        AdminMembershipLimitsDto limits = resolveLimitsByPais(accessCode.getPais());
        return switch (resolvePlanTier(accessCode)) {
            case VIP_6 -> safeLimit(limits.getVip6MaxClientes(), VIP6_DEFAULT_MAX_CLIENTES);
            case VIP_3 -> safeLimit(limits.getVip3MaxClientes(), VIP3_DEFAULT_MAX_CLIENTES);
        };
    }

    public AdminMembershipLimitsDto resolveLimitsByPais(String pais) {
        AdminPanel admin = adminPanelRepository.findByPaisIgnoreCase(CountryCatalog.normalizeCode(pais))
                .or(() -> adminPanelRepository.findByPaisIgnoreCase(CountryCatalog.adminKey(pais)))
                .orElse(null);

        AdminMembershipLimitsDto dto = new AdminMembershipLimitsDto();
        dto.setId(admin != null ? admin.getId() : null);
        dto.setPais(admin != null ? admin.getPais() : CountryCatalog.normalizeCode(pais));
        dto.setDemoMaxEmpresas(admin != null ? safeLimit(admin.getDemoMaxEmpresas(), DEMO_DEFAULT_MAX_EMPRESAS) : DEMO_DEFAULT_MAX_EMPRESAS);
        dto.setVip3MaxEmpresas(admin != null ? safeLimit(admin.getVip3MaxEmpresas(), VIP3_DEFAULT_MAX_EMPRESAS) : VIP3_DEFAULT_MAX_EMPRESAS);
        dto.setVip6MaxEmpresas(admin != null ? safeLimit(admin.getVip6MaxEmpresas(), VIP6_DEFAULT_MAX_EMPRESAS) : VIP6_DEFAULT_MAX_EMPRESAS);
        dto.setDemoMaxClientes(admin != null ? safeLimit(admin.getDemoMaxClientes(), DEMO_DEFAULT_MAX_CLIENTES) : DEMO_DEFAULT_MAX_CLIENTES);
        dto.setVip3MaxClientes(admin != null ? safeLimit(admin.getVip3MaxClientes(), VIP3_DEFAULT_MAX_CLIENTES) : VIP3_DEFAULT_MAX_CLIENTES);
        dto.setVip6MaxClientes(admin != null ? safeLimit(admin.getVip6MaxClientes(), VIP6_DEFAULT_MAX_CLIENTES) : VIP6_DEFAULT_MAX_CLIENTES);
        return dto;
    }

    private AccessCode requireAccessCode(String userCode) {
        AccessCode accessCode = accessCodeRepository.findByCode(userCode);
        if (accessCode == null) {
            throw new IllegalArgumentException("No se encontró un código de acceso válido para aplicar los límites.");
        }
        return accessCode;
    }

    private PlanTier resolvePlanTier(AccessCode accessCode) {
        LocalDate fechaRegistro = accessCode.getFechaRegistro();
        LocalDate fechaVencimiento = accessCode.getFechaVencimiento();
        if (fechaRegistro != null && fechaVencimiento != null && !fechaVencimiento.isBefore(fechaRegistro)) {
            long days = ChronoUnit.DAYS.between(fechaRegistro, fechaVencimiento);
            if (days >= VIP6_THRESHOLD_DAYS) {
                return PlanTier.VIP_6;
            }
        }

        String code = accessCode.getCode();
        if (code != null && code.trim().length() >= 6) {
            return PlanTier.VIP_6;
        }

        return PlanTier.VIP_3;
    }

    private int safeLimit(Integer value, int fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private enum PlanTier {
        VIP_3,
        VIP_6
    }
}
