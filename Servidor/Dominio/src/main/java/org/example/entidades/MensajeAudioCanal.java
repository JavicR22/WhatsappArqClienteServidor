package org.example.entidades;

import java.time.LocalDateTime;

public class MensajeAudioCanal {
    private Usuario emisor;
    private Canal canal;

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

    private String contenido;

    public MensajeAudioCanal(Usuario emisor, Canal canal, String contenido, String rutaAudio, LocalDateTime fechaEnvio) {
        this.emisor = emisor;
        this.canal = canal;
        this.contenido = contenido;
        this.rutaAudio = rutaAudio;
        this.fechaEnvio = fechaEnvio;
    }

    private String rutaAudio;
    private LocalDateTime fechaEnvio;
}
