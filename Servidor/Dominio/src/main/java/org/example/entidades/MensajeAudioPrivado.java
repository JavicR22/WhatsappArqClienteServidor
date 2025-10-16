package org.example.entidades;

import java.time.LocalDateTime;


public class MensajeAudioPrivado {
    Usuario emisor;
    Usuario receptor;
    String contenidoTexto;

    public MensajeAudioPrivado(Usuario emisor, Usuario receptor, String contenidoTexto, String rutaAudio, LocalDateTime fechaEnvio) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoTexto = contenidoTexto;
        this.rutaAudio = rutaAudio;
        this.fechaEnvio = fechaEnvio;
    }

    String rutaAudio;
    LocalDateTime fechaEnvio;

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

    public String getRutaAudio() {
        return rutaAudio;
    }

    public void setRutaAudio(String rutaAudio) {
        this.rutaAudio = rutaAudio;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}
