package org.example.client.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MODELO DE DOMINIO
 * Clase base abstracta para todos los tipos de mensajes del sistema.
 * Define la estructura común de comunicación entre cliente y servidor.
 */
public abstract class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private String _tipo;
    private String id;
    private Usuario remitente;
    private LocalDateTime fechaHora;

    public Mensaje() {
        this.fechaHora = LocalDateTime.now();
    }

    public Mensaje(String id, Usuario remitente) {
        this.id = id;
        this.remitente = remitente;
        this.fechaHora = LocalDateTime.now();
    }

    protected void setTipoInterno(String tipo) {
        this._tipo = tipo;
    }

    // Getters y Setters
    public String get_tipo() {
        return _tipo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Usuario getRemitente() {
        return remitente;
    }

    public void setRemitente(Usuario remitente) {
        this.remitente = remitente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    /**
     * Método abstracto para obtener el tipo de mensaje
     */
    public abstract String getTipo();
}
