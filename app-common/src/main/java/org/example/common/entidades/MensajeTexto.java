package org.example.common.entidades;

public class MensajeTexto extends Mensaje {
    private String contenido;

    public MensajeTexto(String id, Usuario remitente, String contenido) {
        super(id, remitente);
        this.contenido = contenido;
    }

    public String getContenido() { return contenido; }
}
