package org.example.entidades;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MensajeTextoPrivado {
    public MensajeTextoPrivado() {

    }

    public Usuario getEmisor() {
        return emisor;
    }

    public void setEmisor(Usuario emisor) {
        this.emisor = emisor;
    }

    public Usuario getReceptor() {
        return receptor;
    }

    public void setReceptor(Usuario receptor) {
        this.receptor = receptor;
    }

    public String getContenidoTexto() {
        return contenidoTexto;
    }

    public void setContenidoTexto(String contenidoTexto) {
        this.contenidoTexto = contenidoTexto;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public MensajeTextoPrivado(Usuario emisor, Usuario receptor, String contenidoTexto, LocalDateTime fechaEnvio) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoTexto = contenidoTexto;
        this.fechaEnvio = fechaEnvio;
    }

    private Usuario emisor;
    private Usuario receptor;
    private String contenidoTexto;
    private LocalDateTime fechaEnvio;

}
