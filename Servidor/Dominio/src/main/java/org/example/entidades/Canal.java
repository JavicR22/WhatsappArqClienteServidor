package org.example.entidades;

import java.time.LocalDateTime;
import java.util.List;

public class Canal {
    private Integer idCanal;

    private String nombreCanal;
    private List<Usuario> usuarios;
    private List<MensajeTextoCanal> mensajeTextoCanales;
    private LocalDateTime fechaCreacion;

    public Canal(int idCanal, String nombreCanal, LocalDateTime fechaCreacion) {
        this.idCanal=idCanal;
        this.nombreCanal=nombreCanal;
        this.fechaCreacion=fechaCreacion;
    }

    public Canal(String nombreCanal) {
        this.nombreCanal=nombreCanal;
    }

    public Canal(int i) {
        this.idCanal=i;
    }


    public String getNombreCanal() {
        return nombreCanal;
    }

    public void setNombreCanal(String nombreCanal) {
        this.nombreCanal = nombreCanal;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public List<MensajeTextoCanal> getMensajeTextoCanales() {
        return mensajeTextoCanales;
    }

    public void setMensajeTextoCanales(List<MensajeTextoCanal> mensajeTextoCanales) {
        this.mensajeTextoCanales = mensajeTextoCanales;
    }

    public List<MensajeAudioCanal> getMensajeAudioCanales() {
        return mensajeAudioCanales;
    }

    public Integer getIdCanal() {
        return idCanal;
    }

    public void setIdCanal(Integer idCanal) {
        this.idCanal = idCanal;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setMensajeAudioCanales(List<MensajeAudioCanal> mensajeAudioCanales) {
        this.mensajeAudioCanales = mensajeAudioCanales;
    }

    public Canal(String nombreCanal, List<Usuario> usuarios, List<MensajeTextoCanal> mensajeTextoCanales, List<MensajeAudioCanal> mensajeAudioCanales) {
        this.nombreCanal = nombreCanal;
        this.usuarios = usuarios;
        this.mensajeTextoCanales = mensajeTextoCanales;
        this.mensajeAudioCanales = mensajeAudioCanales;
    }

    private List<MensajeAudioCanal> mensajeAudioCanales;
}
