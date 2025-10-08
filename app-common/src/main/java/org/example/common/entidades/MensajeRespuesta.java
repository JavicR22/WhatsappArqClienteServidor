package org.example.common.entidades;

public class MensajeRespuesta extends Mensaje {
    private boolean exito;
    private String mensaje;
    private String usuarioId; // si exito, devuelve id de usuario autenticado

    public MensajeRespuesta(String id, Usuario remitente, boolean exito, String mensaje, String usuarioId) {
        super(id, remitente);
        this.exito = exito;
        this.mensaje = mensaje;
        this.usuarioId = usuarioId;
    }

    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public String getUsuarioId() { return usuarioId; }
}
