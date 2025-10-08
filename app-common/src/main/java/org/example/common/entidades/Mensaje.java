package org.example.common.entidades;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class Mensaje implements Serializable {
    private String id;
    private Usuario remitente;
    private LocalDateTime fechaHora;

    public Mensaje(String id, Usuario remitente) {
        this.id = id;
        this.remitente = remitente;
        this.fechaHora = LocalDateTime.now();
    }

    public String getId() { return id; }
    public Usuario getRemitente() { return remitente; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
