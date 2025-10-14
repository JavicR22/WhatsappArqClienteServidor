package org.example.client.modelo;

import java.io.Serializable;

/**
 * MODELO DE DOMINIO
 * Mensaje para aceptar o rechazar una invitación a un canal.
 */
public class MensajeRespuestaInvitacion extends Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idSolicitud;
    private String idCanal;
    private boolean aceptada;

    public MensajeRespuestaInvitacion() {
        super();
        setTipoInterno("MensajeRespuestaInvitacion");
    }

    public MensajeRespuestaInvitacion(String id, Usuario remitente, String idSolicitud, String idCanal, boolean aceptada) {
        super(id, remitente);
        setTipoInterno("MensajeRespuestaInvitacion");
        this.idSolicitud = idSolicitud;
        this.idCanal = idCanal;
        this.aceptada = aceptada;
    }

    public String getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(String idSolicitud) { this.idSolicitud = idSolicitud; }
    public String getIdCanal() { return idCanal; }
    public void setIdCanal(String idCanal) { this.idCanal = idCanal; }
    public boolean isAceptada() { return aceptada; }
    public void setAceptada(boolean aceptada) { this.aceptada = aceptada; }

    @Override
    public String getTipo() { return "MensajeRespuestaInvitacion"; }
}
