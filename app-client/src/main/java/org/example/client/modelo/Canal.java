package org.example.client.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MODELO DE DOMINIO
 * Representa un canal de comunicación entre usuarios.
 */
public class Canal implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private List<Usuario> miembros = new ArrayList<>();

    public Canal() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Canal(String id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = LocalDateTime.now();
    }

    public void agregarUsuario(Usuario u) {
        if (!miembros.contains(u)) miembros.add(u);
    }

    public void removerUsuario(Usuario u) {
        miembros.remove(u);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<Usuario> getMiembros() { return miembros; }

    @Override
    public String toString() {
        return "Canal{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", miembros=" + miembros.size() +
                '}';
    }
}
