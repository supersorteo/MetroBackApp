package com.example.bdMetro.services;

import com.example.bdMetro.entity.CalculoMaterial;
import com.example.bdMetro.entity.Cliente;
import com.example.bdMetro.entity.Presupuesto;
import com.example.bdMetro.repository.CalculoMaterialRepository;
import com.example.bdMetro.repository.PresupuestoRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ArchiToolsService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MAX_CALCULOS = 10;

    private final ClienteService clienteService;
    private final PresupuestoRepository presupuestoRepository;
    private final CalculoMaterialRepository calculoMaterialRepository;

    public ArchiToolsService(ClienteService clienteService,
                              PresupuestoRepository presupuestoRepository,
                              CalculoMaterialRepository calculoMaterialRepository) {
        this.clienteService = clienteService;
        this.presupuestoRepository = presupuestoRepository;
        this.calculoMaterialRepository = calculoMaterialRepository;
    }

    public String execute(String functionName, Map<String, Object> args, String userCode) {
        return switch (functionName) {
            case "consultarClientes"          -> consultarClientes(userCode);
            case "consultarPresupuestos"      -> consultarPresupuestos(userCode);
            case "consultarHistorialCalculos" -> consultarHistorialCalculos(userCode);
            case "crearCliente"              -> crearCliente(args, userCode);
            default -> "Función desconocida: " + functionName;
        };
    }

    private String consultarClientes(String userCode) {
        if (userCode == null || userCode.isBlank()) return "No hay sesión activa para consultar clientes.";
        List<Cliente> clientes = clienteService.getClienteByUserCode(userCode);
        if (clientes.isEmpty()) return "No hay clientes registrados para este usuario.";
        var sb = new StringBuilder("Clientes registrados (" + clientes.size() + "):\n");
        for (Cliente c : clientes) {
            sb.append("• ").append(c.getName());
            if (c.getContact() != null && !c.getContact().isBlank())
                sb.append(" — Tel: ").append(c.getContact());
            if (c.getEmail() != null && !c.getEmail().isBlank())
                sb.append(" — Email: ").append(c.getEmail());
            sb.append("\n");
        }
        return sb.toString();
    }

    private String consultarPresupuestos(String userCode) {
        if (userCode == null || userCode.isBlank()) return "No hay sesión activa para consultar presupuestos.";
        List<Presupuesto> lista = presupuestoRepository.findByClienteUserCode(userCode);
        if (lista.isEmpty()) return "No hay presupuestos registrados para este usuario.";
        var sb = new StringBuilder("Presupuestos registrados (" + lista.size() + "):\n");
        for (Presupuesto p : lista) {
            sb.append("• ").append(p.getName());
            if (p.getCliente() != null) sb.append(" — Cliente: ").append(p.getCliente().getName());
            if (p.getCreatedAt() != null) sb.append(" — Fecha: ").append(p.getCreatedAt().format(FMT));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String consultarHistorialCalculos(String userCode) {
        if (userCode == null || userCode.isBlank()) return "No hay sesión activa para consultar el historial.";
        List<CalculoMaterial> calculos = calculoMaterialRepository
                .findByUserCodeOrderByCreatedAtDescIdDesc(userCode);
        if (calculos.isEmpty()) return "No hay cálculos guardados para este usuario.";
        int limite = Math.min(calculos.size(), MAX_CALCULOS);
        var sb = new StringBuilder("Últimos " + limite + " cálculos guardados:\n");
        for (int i = 0; i < limite; i++) {
            CalculoMaterial c = calculos.get(i);
            sb.append("• ").append(c.getTareaTitulo())
              .append(" — ").append(c.getValorIngresado()).append(" ").append(c.getUnidad());
            if (c.getCreatedAt() != null) sb.append(" — ").append(c.getCreatedAt().format(FMT));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String crearCliente(Map<String, Object> args, String userCode) {
        if (userCode == null || userCode.isBlank()) return "No hay sesión activa para crear el cliente.";
        String nombre = (String) args.get("nombre");
        String email  = (String) args.get("email");
        if (nombre == null || nombre.isBlank()) return "No se pudo crear el cliente: falta el nombre.";
        if (email == null || email.isBlank())   return "No se pudo crear el cliente: falta el email.";
        String telefono = args.containsKey("telefono") ? (String) args.get("telefono") : "";

        Cliente c = new Cliente();
        c.setName(nombre.trim());
        c.setEmail(email.trim());
        c.setContact(telefono != null ? telefono.trim() : "");
        c.setUserCode(userCode);

        try {
            clienteService.saveCliente(c);
            return "Cliente '" + nombre.trim() + "' creado correctamente en MetroApp.";
        } catch (IllegalArgumentException e) {
            return "No se pudo crear el cliente: " + e.getMessage();
        }
    }
}
