package org.example.common.entidades;

public class MensajeAudio extends Mensaje {
    private String rutaArchivo;

    public MensajeAudio(String id, Usuario remitente, String rutaArchivo) {
        super(id, remitente);
        this.rutaArchivo = rutaArchivo;
    }

    public String getRutaArchivo() { return rutaArchivo; }
}
