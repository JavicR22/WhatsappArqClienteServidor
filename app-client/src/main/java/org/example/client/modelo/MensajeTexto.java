package org.example.client.modelo;

/**
 * MODELO DE DOMINIO
 * Representa un mensaje de texto enviado por un usuario.
 */
public class MensajeTexto extends Mensaje {
    private static final long serialVersionUID = 1L;

    private String contenido;
    private String destinatario; // correo del destinatario (null si es para canal)

    public MensajeTexto() {
        super();
        setTipoInterno("MensajeTexto");
    }

    public MensajeTexto(String id, Usuario remitente, String contenido) {
        super(id, remitente);
        setTipoInterno("MensajeTexto");
        this.contenido = contenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    @Override
    public String getTipo() {
        return "MensajeTexto";
    }

    @Override
    public String toString() {
        return "MensajeTexto{" +
                "id='" + getId() + '\'' +
                ", remitente=" + getRemitente() +
                ", contenido='" + contenido + '\'' +
                ", destinatario='" + destinatario + '\'' +
                ", fechaHora=" + getFechaHora() +
                '}';
    }
}
