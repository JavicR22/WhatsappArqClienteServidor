package org.example.client.modelo;

import java.time.LocalDateTime;

/**
 * Modelo para persistir la sesión del usuario en la base de datos local
 */
public class Sesion {
    private String id;
    private String correoUsuario;
    private String token;
    private LocalDateTime fechaInicio;
    private LocalDateTime ultimaActividad;
    private boolean activa;

    public Sesion(String id, String correoUsuario, String token) {
        this.id = id;
        this.correoUsuario = correoUsuario;
        this.token = token;
        this.fechaInicio = LocalDateTime.now();
        this.ultimaActividad = LocalDateTime.now();
        this.activa = true;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(LocalDateTime ultimaActividad) { this.ultimaActividad = ultimaActividad; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public void actualizarActividad() {
        this.ultimaActividad = LocalDateTime.now();
    }
}
