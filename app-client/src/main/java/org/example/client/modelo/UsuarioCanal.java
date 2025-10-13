package org.example.client.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MODELO DE DOMINIO
 * Relación entre usuario y canal.
 */
public class UsuarioCanal implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idUsuario;
    private String idCanal;
    private LocalDateTime fechaUnion;

    public UsuarioCanal() {
        this.fechaUnion = LocalDateTime.now();
    }

    public UsuarioCanal(String idUsuario, String idCanal) {
        this.idUsuario = idUsuario;
        this.idCanal = idCanal;
        this.fechaUnion = LocalDateTime.now();
    }

    // Getters y Setters
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getIdCanal() { return idCanal; }
    public void setIdCanal(String idCanal) { this.idCanal = idCanal; }
    public LocalDateTime getFechaUnion() { return fechaUnion; }

    @Override
    public String toString() {
        return "UsuarioCanal{" +
                "idUsuario='" + idUsuario + '\'' +
                ", idCanal='" + idCanal + '\'' +
                ", fechaUnion=" + fechaUnion +
                '}';
    }
}
