package org.example.client.modelo;

/**
 * MODELO DE DOMINIO
 * Representa una respuesta del servidor a una solicitud del cliente.
 */
public class MensajeRespuesta extends Mensaje {
    private static final long serialVersionUID = 1L;

    private boolean exito;
    private String mensaje;
    private String usuarioId;

    public MensajeRespuesta() {
        super();
        setTipoInterno("MensajeRespuesta");
    }

    public MensajeRespuesta(String id, Usuario remitente, boolean exito, String mensaje) {
        super(id, remitente);
        setTipoInterno("MensajeRespuesta");
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String getTipo() {
        return "MensajeRespuesta";
    }

    @Override
    public String toString() {
        return "MensajeRespuesta{" +
                "id='" + getId() + '\'' +
                ", exito=" + exito +
                ", mensaje='" + mensaje + '\'' +
                ", usuarioId='" + usuarioId + '\'' +
                ", fechaHora=" + getFechaHora() +
                '}';
    }
}
