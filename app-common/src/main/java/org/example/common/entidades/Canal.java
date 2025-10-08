package org.example.common.entidades;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Canal implements Serializable {
    private String id;
    private String nombre;
    private List<Usuario> miembros;

    // Nuevos atributos
    private String descripcion;
    private LocalDateTime fechaCreacion;

    public Canal() {}

    public Canal(String id, String nombre, List<Usuario> miembros) {
        this.id = id;
        this.nombre = nombre;
        this.miembros = miembros;
    }

    // Nuevo constructor completo
    public Canal(String id, String nombre, List<Usuario> miembros, String descripcion, LocalDateTime fechaCreacion) {
        this(id, nombre, miembros);
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public List<Usuario> getMiembros() { return miembros; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
