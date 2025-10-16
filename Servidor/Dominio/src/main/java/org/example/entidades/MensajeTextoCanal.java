package org.example.entidades;

import java.time.LocalDateTime;

public class MensajeTextoCanal {
    private Usuario emisor;

    public MensajeTextoCanal() {

    }

    public Usuario getEmisor() {
        return emisor;
    }

    public void setEmisor(Usuario emisor) {
        this.emisor = emisor;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    private Canal canal;

    public MensajeTextoCanal(Usuario emisor, Canal canal, String contenido, LocalDateTime fechaEnvio) {
        this.emisor = emisor;
        this.canal = canal;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
    }

    private String contenido;
    private LocalDateTime fechaEnvio;
}
