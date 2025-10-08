package org.example.common.entidades;

import java.io.Serializable;

public class Solicitud implements Serializable {
    private String id;
    private Usuario emisor;
    private Usuario receptor;
    private String estado; // PENDIENTE, ACEPTADA, RECHAZADA

    public Solicitud(String id, Usuario emisor, Usuario receptor, String estado) {
        this.id = id;
        this.emisor = emisor;
        this.receptor = receptor;
        this.estado = estado;
    }

    public String getEstado() { return estado; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
