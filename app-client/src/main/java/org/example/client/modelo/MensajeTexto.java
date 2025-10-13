package org.example.client.modelo;

/**
 * MODELO DE DOMINIO
 * Representa un mensaje de texto enviado por un usuario.
 */
public class MensajeTexto extends Mensaje {
    private static final long serialVersionUID = 1L;

    private String contenido;

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
                ", fechaHora=" + getFechaHora() +
                '}';
    }
}
