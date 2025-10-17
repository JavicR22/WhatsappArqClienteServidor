package org.example.client.modelo;

/**
 * MODELO DE DOMINIO
 * Representa un mensaje de audio privado entre dos usuarios
 */
public class MensajeAudioPrivado extends MensajeAudio {
    private static final long serialVersionUID = 1L;

    private String receptorCorreo;
    private byte[] audioData; // Datos del audio en bytes para enviar por red

    public MensajeAudioPrivado() {
        super();
        setTipoInterno("MensajeAudioPrivado");
    }

    public MensajeAudioPrivado(String id, Usuario remitente, Usuario receptor,
                               String rutaArchivo, long duracionSegundos, byte[] audioData) {
        super(id, remitente, rutaArchivo, duracionSegundos);
        setTipoInterno("MensajeAudioPrivado");
        this.receptorCorreo = receptor != null ? receptor.getCorreo() : null;
        this.audioData = audioData;
    }

    public String getReceptorCorreo() {
        return receptorCorreo;
    }

    public void setReceptorCorreo(String receptorCorreo) {
        this.receptorCorreo = receptorCorreo;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    @Override
    public String getTipo() {
        return "MensajeAudioPrivado";
    }

    @Override
    public String toString() {
        return "MensajeAudioPrivado{" +
                "id='" + getId() + '\'' +
                ", remitente=" + (getRemitente() != null ? getRemitente().getCorreo() : "null") +
                ", receptor=" + receptorCorreo +
                ", duracion=" + getDuracionSegundos() + "s" +
                ", tamaño=" + getTamanoBytes() + " bytes" +
                '}';
    }
}
