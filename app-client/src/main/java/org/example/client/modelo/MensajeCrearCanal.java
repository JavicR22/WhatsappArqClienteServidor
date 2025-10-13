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

    public MensajeCrearCanal() {
        super();
        setTipoInterno("MensajeCrearCanal");
    }

    public MensajeCrearCanal(String id, Usuario remitente, String nombre, String descripcion) {
        super(id, remitente);
        setTipoInterno("MensajeCrearCanal");
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String getTipo() { return "MensajeCrearCanal"; }
}
