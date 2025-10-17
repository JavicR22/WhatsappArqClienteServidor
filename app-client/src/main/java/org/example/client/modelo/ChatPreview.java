package org.example.client.modelo;

import java.time.LocalDateTime;

/**
 * Modelo para mostrar vista previa de chats en la lista de conversaciones
 */
public class ChatPreview {
    private String id;
    private String nombreContacto;
    private String correoContacto;
    private String fotoBase64;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private int mensajesNoLeidos;
    private boolean esCanal;
    private String idCanal;

    public ChatPreview(String id, String nombreContacto, String correoContacto) {
        this.id = id;
        this.nombreContacto = nombreContacto;
        this.correoContacto = correoContacto;
        this.mensajesNoLeidos = 0;
        this.esCanal = false;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }

    public String getCorreoContacto() { return correoContacto; }
    public void setCorreoContacto(String correoContacto) { this.correoContacto = correoContacto; }

    public String getFotoBase64() { return fotoBase64; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }

    public String getUltimoMensaje() { return ultimoMensaje; }
    public void setUltimoMensaje(String ultimoMensaje) { this.ultimoMensaje = ultimoMensaje; }

    public LocalDateTime getFechaUltimoMensaje() { return fechaUltimoMensaje; }
    public void setFechaUltimoMensaje(LocalDateTime fechaUltimoMensaje) { this.fechaUltimoMensaje = fechaUltimoMensaje; }

    public int getMensajesNoLeidos() { return mensajesNoLeidos; }
    public void setMensajesNoLeidos(int mensajesNoLeidos) { this.mensajesNoLeidos = mensajesNoLeidos; }

    public boolean isEsCanal() { return esCanal; }
    public void setEsCanal(boolean esCanal) { this.esCanal = esCanal; }

    public String getIdCanal() { return idCanal; }
    public void setIdCanal(String idCanal) { this.idCanal = idCanal; }

    @Override
    public String toString() {
        return nombreContacto + (mensajesNoLeidos > 0 ? " (" + mensajesNoLeidos + ")" : "");
    }
}
