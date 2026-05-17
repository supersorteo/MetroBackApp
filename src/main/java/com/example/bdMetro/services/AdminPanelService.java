package com.example.bdMetro.services;

import com.example.bdMetro.dto.AdminMembershipLimitsDto;
import com.example.bdMetro.entity.AdminPanel;
import com.example.bdMetro.repository.AdminPanelRepository;
import com.example.bdMetro.util.CountryCatalog;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AdminPanelService {

    @Autowired
    private AdminPanelRepository adminPanelRepository;

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
        existing.forEach(this::applyDefaultLimits);
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
        return getByPais(pais).map(this::toLimitsDto);
    }

    public Optional<AdminMembershipLimitsDto> updateLimits(String id, AdminMembershipLimitsDto payload) {
        return adminPanelRepository.findById(id).map(admin -> {
            admin.setDemoMaxEmpresas(normalizeLimit(payload.getDemoMaxEmpresas(), admin.getDemoMaxEmpresas()));
            admin.setVip3MaxEmpresas(normalizeLimit(payload.getVip3MaxEmpresas(), admin.getVip3MaxEmpresas()));
            admin.setVip6MaxEmpresas(normalizeLimit(payload.getVip6MaxEmpresas(), admin.getVip6MaxEmpresas()));
            admin.setDemoMaxClientes(normalizeLimit(payload.getDemoMaxClientes(), admin.getDemoMaxClientes()));
            admin.setVip3MaxClientes(normalizeLimit(payload.getVip3MaxClientes(), admin.getVip3MaxClientes()));
            admin.setVip6MaxClientes(normalizeLimit(payload.getVip6MaxClientes(), admin.getVip6MaxClientes()));
            applyDefaultLimits(admin);
            return toLimitsDto(adminPanelRepository.save(admin));
        });
    }

    /** Delete — intentionally only available in the service/controller, not wired to frontend */
    public void delete(String id) {
        adminPanelRepository.deleteById(id);
    }

    private void applyDefaultLimits(AdminPanel admin) {
        if (admin.getDemoMaxEmpresas() == null || admin.getDemoMaxEmpresas() <= 0) admin.setDemoMaxEmpresas(3);
        if (admin.getVip3MaxEmpresas() == null || admin.getVip3MaxEmpresas() <= 0) admin.setVip3MaxEmpresas(1);
        if (admin.getVip6MaxEmpresas() == null || admin.getVip6MaxEmpresas() <= 0) admin.setVip6MaxEmpresas(3);
        if (admin.getDemoMaxClientes() == null || admin.getDemoMaxClientes() <= 0) admin.setDemoMaxClientes(6);
        if (admin.getVip3MaxClientes() == null || admin.getVip3MaxClientes() <= 0) admin.setVip3MaxClientes(30);
        if (admin.getVip6MaxClientes() == null || admin.getVip6MaxClientes() <= 0) admin.setVip6MaxClientes(60);
    }

    private int normalizeLimit(Integer incoming, Integer currentValue) {
        if (incoming == null) {
            return currentValue != null ? currentValue : 1;
        }
        return Math.max(1, incoming);
    }

    private AdminMembershipLimitsDto toLimitsDto(AdminPanel admin) {
        AdminMembershipLimitsDto dto = new AdminMembershipLimitsDto();
        dto.setId(admin.getId());
        dto.setPais(admin.getPais());
        dto.setDemoMaxEmpresas(admin.getDemoMaxEmpresas());
        dto.setVip3MaxEmpresas(admin.getVip3MaxEmpresas());
        dto.setVip6MaxEmpresas(admin.getVip6MaxEmpresas());
        dto.setDemoMaxClientes(admin.getDemoMaxClientes());
        dto.setVip3MaxClientes(admin.getVip3MaxClientes());
        dto.setVip6MaxClientes(admin.getVip6MaxClientes());
        return dto;
    }
}
