package org.example.client.modelo;

import java.io.Serializable;

/**
 * MODELO DE DOMINIO
 * Mensaje para solicitar unión a un canal.
 */
public class MensajeSolicitudUnion extends Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idCanal;
    private String estado;

    public MensajeSolicitudUnion() {
        super();
        setTipoInterno("MensajeSolicitudUnion");
    }

    public MensajeSolicitudUnion(String id, Usuario remitente, String idCanal) {
        super(id, remitente);
        setTipoInterno("MensajeSolicitudUnion");
        this.idCanal = idCanal;
        this.estado = "PENDIENTE";
    }

    public String getIdCanal() { return idCanal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String getTipo() { return "MensajeSolicitudUnion"; }
}
