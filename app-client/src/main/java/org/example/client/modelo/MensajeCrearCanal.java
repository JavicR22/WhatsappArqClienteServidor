package org.example.client.modelo;

import java.io.Serializable;

/**
 * MODELO DE DOMINIO
 * Representa la solicitud de creación de un canal.
 */
public class MensajeCrearCanal extends Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String descripcion;
    private boolean privado;

    public MensajeCrearCanal() {
        super();
        setTipoInterno("MensajeCrearCanal");
    }

    public MensajeCrearCanal(String id, Usuario remitente, String nombre, String descripcion, boolean privado) {
        super(id, remitente);
        setTipoInterno("MensajeCrearCanal");
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.privado = privado;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isPrivado() { return privado; }
    public void setPrivado(boolean privado) { this.privado = privado; }

    @Override
    public String getTipo() { return "MensajeCrearCanal"; }
}
