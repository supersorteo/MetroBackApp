package com.example.bdMetro.services;

import com.example.bdMetro.dto.AdminMembershipLimitsDto;
import com.example.bdMetro.entity.AdminPanel;
import com.example.bdMetro.payments.config.MembershipCatalogProperties;
import com.example.bdMetro.payments.config.FxRateProperties;
import com.example.bdMetro.repository.AdminPanelRepository;
import com.example.bdMetro.util.CountryCatalog;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AdminPanelService {

    @Autowired
    private AdminPanelRepository adminPanelRepository;

    @Autowired
    private MembershipCatalogProperties membershipCatalogProperties;

    @Autowired
    private FxRateProperties fxRateProperties;

    /** Seed 3 default admins on first startup if the table is empty */
    @PostConstruct
    public void seedDefaults() {
        List<AdminPanel> defaults = Arrays.asList(
            buildAdmin("ar", "AR", "Admin Argentina", "admin_ar", "metro2025ar", "\uD83C\uDDE6\uD83C\uDDF7"),
            buildAdmin("uy", "UY", "Admin Uruguay", "admin_uy", "metro2025uy", "\uD83C\uDDFA\uD83C\uDDFE"),
            buildAdmin("co", "CO", "Admin Colombia", "admin_co", "metro2025co", "\uD83C\uDDE8\uD83C\uDDF4")
        );

        if (adminPanelRepository.count() == 0) {
            adminPanelRepository.saveAll(defaults);
            return;
        }

        List<AdminPanel> existing = adminPanelRepository.findAll();
        existing.forEach(admin -> {
            migrateLegacyLimits(admin);
            applyDefaultLimits(admin);
        });
        adminPanelRepository.saveAll(existing);
    }

    private AdminPanel buildAdmin(String id, String pais, String nombre,
                                  String username, String password, String flag) {
        AdminPanel a = new AdminPanel();
        a.setId(id);
        a.setPais(CountryCatalog.normalizeCode(pais));
        a.setNombre(nombre);
        a.setUsername(username);
        a.setPassword(password);
        a.setFlag(flag);
        applyDefaultLimits(a);
        return a;
    }

    public List<AdminPanel> getAll() {
        return adminPanelRepository.findAll();
    }

    public Optional<AdminPanel> getById(String id) {
        return adminPanelRepository.findById(id);
    }

    public Optional<AdminPanel> getByPais(String pais) {
        return adminPanelRepository.findByPaisIgnoreCase(CountryCatalog.normalizeCode(pais))
                .or(() -> adminPanelRepository.findByPaisIgnoreCase(CountryCatalog.adminKey(pais)));
    }

    /** Verify credentials — returns the admin if valid, empty otherwise */
    public Optional<AdminPanel> login(String username, String password) {
        return adminPanelRepository.findByUsername(username)
                .filter(a -> a.getPassword().equals(password));
    }

    /** Update nombre, username and/or password */
    public Optional<AdminPanel> update(String id, String nombre, String username, String password) {
        return adminPanelRepository.findById(id).map(a -> {
            if (nombre   != null && !nombre.isBlank())   a.setNombre(nombre);
            if (username != null && !username.isBlank()) a.setUsername(username);
            if (password != null && !password.isBlank()) a.setPassword(password);
            return adminPanelRepository.save(a);
        });
    }

    public Optional<AdminMembershipLimitsDto> getLimitsByPais(String pais) {
        return getByPais(pais).map(admin -> {
            if (migrateLegacyLimits(admin)) adminPanelRepository.save(admin);
            return toLimitsDto(admin);
        });
    }

    public Optional<AdminMembershipLimitsDto> updateLimits(String id, AdminMembershipLimitsDto payload) {
        return adminPanelRepository.findById(id).map(admin -> {
            admin.setDemoMaxEmpresas(normalizeLimit(payload.getDemoMaxEmpresas(),      admin.getDemoMaxEmpresas()));
            admin.setVip3MaxEmpresas(normalizeLimit(payload.getVip3MaxEmpresas(),      admin.getVip3MaxEmpresas()));
            admin.setVip6MaxEmpresas(normalizeLimit(payload.getVip6MaxEmpresas(),      admin.getVip6MaxEmpresas()));
            admin.setVip12MaxEmpresas(normalizeLimit(payload.getVip12MaxEmpresas(),    admin.getVip12MaxEmpresas()));
            admin.setDemoMaxClientes(normalizeLimit(payload.getDemoMaxClientes(),      admin.getDemoMaxClientes()));
            admin.setVip3MaxClientes(normalizeLimit(payload.getVip3MaxClientes(),      admin.getVip3MaxClientes()));
            admin.setVip6MaxClientes(normalizeLimit(payload.getVip6MaxClientes(),      admin.getVip6MaxClientes()));
            admin.setVip12MaxClientes(normalizeLimit(payload.getVip12MaxClientes(),    admin.getVip12MaxClientes()));
            admin.setDemoMaxPresupuestos(normalizeLimit(payload.getDemoMaxPresupuestos(),   admin.getDemoMaxPresupuestos()));
            admin.setVip3MaxPresupuestos(normalizeLimit(payload.getVip3MaxPresupuestos(),   admin.getVip3MaxPresupuestos()));
            admin.setVip6MaxPresupuestos(normalizeLimit(payload.getVip6MaxPresupuestos(),   admin.getVip6MaxPresupuestos()));
            admin.setVip12MaxPresupuestos(normalizeLimit(payload.getVip12MaxPresupuestos(), admin.getVip12MaxPresupuestos()));
            if (payload.getPrecio3Meses()  != null) admin.setPrecio3Meses(payload.getPrecio3Meses().max(BigDecimal.ZERO));
            if (payload.getPrecio6Meses()  != null) admin.setPrecio6Meses(payload.getPrecio6Meses().max(BigDecimal.ZERO));
            if (payload.getPrecio12Meses() != null) admin.setPrecio12Meses(payload.getPrecio12Meses().max(BigDecimal.ZERO));
            applyDefaultLimits(admin);
            return toLimitsDto(adminPanelRepository.save(admin));
        });
    }

    /** Delete — intentionally only available in the service/controller, not wired to frontend */
    public void delete(String id) {
        adminPanelRepository.deleteById(id);
    }

    private void applyDefaultLimits(AdminPanel admin) {
        if (nullOrZero(admin.getDemoMaxEmpresas()))    admin.setDemoMaxEmpresas(3);
        if (nullOrZero(admin.getVip3MaxEmpresas()))    admin.setVip3MaxEmpresas(1);
        if (nullOrZero(admin.getVip6MaxEmpresas()))    admin.setVip6MaxEmpresas(3);
        if (nullOrZero(admin.getVip12MaxEmpresas()))   admin.setVip12MaxEmpresas(5);
        if (nullOrZero(admin.getDemoMaxClientes()))    admin.setDemoMaxClientes(6);
        if (nullOrZero(admin.getVip3MaxClientes()))    admin.setVip3MaxClientes(200);
        if (nullOrZero(admin.getVip6MaxClientes()))    admin.setVip6MaxClientes(500);
        if (nullOrZero(admin.getVip12MaxClientes()))   admin.setVip12MaxClientes(1000);
        if (nullOrZero(admin.getDemoMaxPresupuestos()))  admin.setDemoMaxPresupuestos(5);
        if (nullOrZero(admin.getVip3MaxPresupuestos()))  admin.setVip3MaxPresupuestos(30);
        if (nullOrZero(admin.getVip6MaxPresupuestos()))  admin.setVip6MaxPresupuestos(120);
        if (nullOrZero(admin.getVip12MaxPresupuestos())) admin.setVip12MaxPresupuestos(250);
        applyDefaultPrices(admin);
    }

    private void applyDefaultPrices(AdminPanel admin) {
        String pais = admin.getPais() != null ? admin.getPais().toUpperCase() : "";
        if (admin.getMoneda() == null) {
            MembershipCatalogProperties.CountryCatalog cat = membershipCatalogProperties.getCatalog().get(pais);
            admin.setMoneda(cat != null && cat.getCurrency() != null ? cat.getCurrency() : "USD");
        }
        if (admin.getPrecio3Meses() == null || admin.getPrecio3Meses().compareTo(BigDecimal.ZERO) == 0) {
            admin.setPrecio3Meses(defaultPrice(pais, "3"));
        }
        if (admin.getPrecio6Meses() == null || admin.getPrecio6Meses().compareTo(BigDecimal.ZERO) == 0) {
            admin.setPrecio6Meses(defaultPrice(pais, "6"));
        }
        if (admin.getPrecio12Meses() == null || admin.getPrecio12Meses().compareTo(BigDecimal.ZERO) == 0) {
            admin.setPrecio12Meses(defaultPrice(pais, "12"));
        }
    }

    private BigDecimal defaultPrice(String paisCode, String planKey) {
        BigDecimal usdBase = membershipCatalogProperties.getBasePlansUsd().getOrDefault(planKey, BigDecimal.ZERO);
        String currency = null;
        MembershipCatalogProperties.CountryCatalog cat = membershipCatalogProperties.getCatalog().get(paisCode);
        if (cat != null) currency = cat.getCurrency();
        if (currency == null) return usdBase;
        BigDecimal rate = fxRateProperties.getFallbackRates().get(currency);
        if (rate == null) return usdBase;
        return usdBase.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }

    // Migra filas antiguas del DB que tienen los valores incorrectos originales.
    // Retorna true si hubo cambios (para que el llamador guarde la entidad).
    private boolean migrateLegacyLimits(AdminPanel admin) {
        boolean dirty = false;
        // Clientes: valores viejos eran 30 y 60; correctos son 200 y 500
        if (admin.getVip3MaxClientes() != null && admin.getVip3MaxClientes() <= 30) {
            admin.setVip3MaxClientes(200); dirty = true;
        }
        if (admin.getVip6MaxClientes() != null && admin.getVip6MaxClientes() <= 60) {
            admin.setVip6MaxClientes(500); dirty = true;
        }
        // Campos nuevos: null en filas existentes antes de la migración
        if (admin.getVip12MaxEmpresas()   == null) { admin.setVip12MaxEmpresas(5);    dirty = true; }
        if (admin.getVip12MaxClientes()   == null) { admin.setVip12MaxClientes(1000); dirty = true; }
        if (admin.getDemoMaxPresupuestos()  == null) { admin.setDemoMaxPresupuestos(5);   dirty = true; }
        if (admin.getVip3MaxPresupuestos()  == null) { admin.setVip3MaxPresupuestos(30);  dirty = true; }
        if (admin.getVip6MaxPresupuestos()  == null) { admin.setVip6MaxPresupuestos(120); dirty = true; }
        if (admin.getVip12MaxPresupuestos() == null) { admin.setVip12MaxPresupuestos(250);dirty = true; }
        if (admin.getMoneda() == null || admin.getPrecio3Meses() == null || admin.getPrecio6Meses() == null || admin.getPrecio12Meses() == null) {
            applyDefaultPrices(admin);
            dirty = true;
        }
        return dirty;
    }

    private boolean nullOrZero(Integer v) { return v == null || v <= 0; }

    private int normalizeLimit(Integer incoming, Integer currentValue) {
        if (incoming == null) return currentValue != null ? currentValue : 1;
        return Math.max(1, incoming);
    }

    private AdminMembershipLimitsDto toLimitsDto(AdminPanel admin) {
        AdminMembershipLimitsDto dto = new AdminMembershipLimitsDto();
        dto.setId(admin.getId());
        dto.setPais(admin.getPais());
        dto.setDemoMaxEmpresas(admin.getDemoMaxEmpresas());
        dto.setVip3MaxEmpresas(admin.getVip3MaxEmpresas());
        dto.setVip6MaxEmpresas(admin.getVip6MaxEmpresas());
        dto.setVip12MaxEmpresas(admin.getVip12MaxEmpresas());
        dto.setDemoMaxClientes(admin.getDemoMaxClientes());
        dto.setVip3MaxClientes(admin.getVip3MaxClientes());
        dto.setVip6MaxClientes(admin.getVip6MaxClientes());
        dto.setVip12MaxClientes(admin.getVip12MaxClientes());
        dto.setDemoMaxPresupuestos(admin.getDemoMaxPresupuestos());
        dto.setVip3MaxPresupuestos(admin.getVip3MaxPresupuestos());
        dto.setVip6MaxPresupuestos(admin.getVip6MaxPresupuestos());
        dto.setVip12MaxPresupuestos(admin.getVip12MaxPresupuestos());
        dto.setPrecio3Meses(admin.getPrecio3Meses());
        dto.setPrecio6Meses(admin.getPrecio6Meses());
        dto.setPrecio12Meses(admin.getPrecio12Meses());
        dto.setMoneda(admin.getMoneda());
        return dto;
    }
}
