package org.example.client.modelo;

/**
 * MODELO DE DOMINIO
 * Representa un mensaje de texto privado entre dos usuarios.
 */
public class MensajeTextoPrivado extends MensajeTexto {
    private static final long serialVersionUID = 1L;

    private String receptorCorreo;

    public MensajeTextoPrivado() {
        super();
        setTipoInterno("MensajeTextoPrivado");
    }

    public MensajeTextoPrivado(String id, Usuario remitente, Usuario receptor, String contenido) {
        super(id, remitente, contenido);
        setTipoInterno("MensajeTextoPrivado");
        this.receptorCorreo = receptor != null ? receptor.getCorreo() : null;
        setDestinatario(this.receptorCorreo);
    }

    public String getReceptorCorreo() {
        return receptorCorreo;
    }

    public void setReceptorCorreo(String receptorCorreo) {
        this.receptorCorreo = receptorCorreo;
    }

    @Override
    public String getTipo() {
        return "MensajeTextoPrivado";
    }

    @Override
    public String toString() {
        return "MensajeTextoPrivado{" +
                "id='" + getId() + '\'' +
                ", remitente=" + getRemitente().getCorreo() +
                ", receptor=" + receptorCorreo +
                ", contenido='" + getContenido() + '\'' +
                ", fechaHora=" + getFechaHora() +
                '}';
    }
}
