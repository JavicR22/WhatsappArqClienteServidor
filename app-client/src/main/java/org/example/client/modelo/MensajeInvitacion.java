package org.example.client.modelo;

import java.io.Serializable;
import java.util.List;

/**
 * MODELO DE DOMINIO
 * Mensaje para invitar usuarios a un canal.
 */
public class MensajeInvitacion extends Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idCanal;
    private String nombreCanal;
    private List<String> correosInvitados;

    public MensajeInvitacion() {
        super();
        setTipoInterno("MensajeInvitacion");
    }

    public MensajeInvitacion(String id, Usuario remitente, String idCanal, String nombreCanal, List<String> correosInvitados) {
        super(id, remitente);
        setTipoInterno("MensajeInvitacion");
        this.idCanal = idCanal;
        this.nombreCanal = nombreCanal;
        this.correosInvitados = correosInvitados;
    }

    public String getIdCanal() { return idCanal; }
    public void setIdCanal(String idCanal) { this.idCanal = idCanal; }
    public String getNombreCanal() { return nombreCanal; }
    public void setNombreCanal(String nombreCanal) { this.nombreCanal = nombreCanal; }
    public List<String> getCorreosInvitados() { return correosInvitados; }
    public void setCorreosInvitados(List<String> correosInvitados) { this.correosInvitados = correosInvitados; }

    @Override
    public String getTipo() { return "MensajeInvitacion"; }
}
