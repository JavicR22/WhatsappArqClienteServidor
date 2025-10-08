package org.example.common.entidades;

public class MensajeAutenticacion extends Mensaje {
    private String correo;
    private String contrasena; // en texto plano aquí; se enviará por TCP (mejor TLS en producción)

    public MensajeAutenticacion(String id, Usuario remitente, String correo, String contrasena) {
        super(id, remitente);
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
}
