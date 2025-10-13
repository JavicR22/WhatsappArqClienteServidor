package org.example.client.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MODELO DE DOMINIO
 * Representa una solicitud de ingreso a un canal.
 */
public class Solicitud implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String idUsuario;
    private String idCanal;
    private LocalDateTime fechaSolicitud;
    private String estado; // PENDIENTE, ACEPTADA, RECHAZADA

    public Solicitud() {
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public Solicitud(String id, String idUsuario, String idCanal) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idCanal = idCanal;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public void aceptar() { this.estado = "ACEPTADA"; }
    public void rechazar() { this.estado = "RECHAZADA"; }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getIdCanal() { return idCanal; }
    public void setIdCanal(String idCanal) { this.idCanal = idCanal; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Solicitud{" +
                "id='" + id + '\'' +
                ", idUsuario='" + idUsuario + '\'' +
                ", idCanal='" + idCanal + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
