package org.example.client.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Canal {
    private String id;
    private String nombre;
    private String descripcion; // Agregado campo descripción
    private boolean privado;
    private List<String> miembros;
    private String creadorEmail;
    private long creadoEn;

    public Canal(String nombre, String descripcion, boolean privado, String creadorEmail, List<String> miembros) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.privado = privado;
        this.creadorEmail = creadorEmail;
        this.miembros = (miembros == null) ? new ArrayList<>() : new ArrayList<>(miembros);
        if (!this.miembros.contains(creadorEmail)) {
            this.miembros.add(creadorEmail);
        }
        this.creadoEn = System.currentTimeMillis();
    }

    public Canal(String id, String nombre, String descripcion, boolean privado, String creadorEmail, List<String> miembros, long creadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.privado = privado;
        this.creadorEmail = creadorEmail;
        this.miembros = (miembros == null) ? new ArrayList<>() : new ArrayList<>(miembros);
        this.creadoEn = creadoEn;
    }

    // Getters y setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; } // Nuevo getter
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; } // Nuevo setter
    public boolean isPrivado() { return privado; }
    public List<String> getMiembros() { return miembros; }
    public String getCreadorEmail() { return creadorEmail; }
    public long getCreadoEn() { return creadoEn; }

    public boolean esCreador(String correoUsuario) {
        return creadorEmail != null && creadorEmail.equals(correoUsuario);
    }

    public void addMiembro(String usuarioEmail) {
        if (!miembros.contains(usuarioEmail)) miembros.add(usuarioEmail);
    }

    public void removeMiembro(String usuarioEmail) {
        miembros.remove(usuarioEmail);
    }

    @Override
    public String toString() {
        return nombre + (privado ? " 🔒" : " 🌐");
    }
}
