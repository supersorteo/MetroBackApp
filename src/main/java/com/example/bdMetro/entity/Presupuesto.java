package com.example.bdMetro.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presupuesto")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "presupuesto_tareas",
            joinColumns = @JoinColumn(name = "presupuesto_id"),
            inverseJoinColumns = @JoinColumn(name = "tarea_id")
    )
    private List<UserTarea> tareas = new ArrayList<>();


    // Constructor vacío
    public Presupuesto() {}

    public Presupuesto(Long id, String name, LocalDateTime createdAt, Cliente cliente, List<UserTarea> tareas) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.cliente = cliente;
        this.tareas = tareas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<UserTarea> getTareas() {
        return tareas;
    }

    public void setTareas(List<UserTarea> tareas) {
        this.tareas = tareas;
    }

    public void addTarea(UserTarea tarea) {
        this.tareas.add(tarea);
    }

    public void removeTarea(UserTarea tarea) {
        this.tareas.remove(tarea);
    }
}
