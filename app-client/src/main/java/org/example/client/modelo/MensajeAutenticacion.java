package org.example.client.modelo;

/**
 * MODELO DE DOMINIO
 * Representa una solicitud de autenticación del cliente al servidor.
 */
public class MensajeAutenticacion extends Mensaje {
    private static final long serialVersionUID = 1L;

    private String correo;
    private String contrasena;

    public MensajeAutenticacion() {
        super();
        setTipoInterno("MensajeAutenticacion");
    }

    public MensajeAutenticacion(String id, Usuario remitente, String correo, String contrasena) {
        super(id, remitente);
        setTipoInterno("MensajeAutenticacion");
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    @Override
    public String getTipo() {
        return "MensajeAutenticacion";
    }

    @Override
    public String toString() {
        return "MensajeAutenticacion{" +
                "id='" + getId() + '\'' +
                ", correo='" + correo + '\'' +
                ", fechaHora=" + getFechaHora() +
                '}';
    }
}
