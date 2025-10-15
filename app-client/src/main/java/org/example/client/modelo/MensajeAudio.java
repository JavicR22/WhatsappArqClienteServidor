package org.example.client.modelo;

import java.io.File;
import java.io.Serializable;

/**
 * MODELO DE DOMINIO
 * Representa un mensaje de audio.
 */
public class MensajeAudio extends Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private String rutaArchivo;
    private long duracionSegundos;
    private long tamanoBytes;

    public MensajeAudio() {
        super();
        setTipoInterno("MensajeAudio");
    }

    public MensajeAudio(String id, Usuario remitente, String rutaArchivo, long duracionSegundos) {
        super(id, remitente);
        setTipoInterno("MensajeAudio");
        this.rutaArchivo = rutaArchivo;
        this.duracionSegundos = duracionSegundos;
        File f = new File(rutaArchivo);
        this.tamanoBytes = f.exists() ? f.length() : 0;
    }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public long getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(long duracionSegundos) { this.duracionSegundos = duracionSegundos; }
    public long getTamanoBytes() { return tamanoBytes; }

    @Override
    public String getTipo() { return "MensajeAudio"; }

    @Override
    public String toString() {
        return "MensajeAudio{" +
                "rutaArchivo='" + rutaArchivo + '\'' +
                ", duracionSegundos=" + duracionSegundos +
                ", tamanoBytes=" + tamanoBytes +
                '}';
    }
}
