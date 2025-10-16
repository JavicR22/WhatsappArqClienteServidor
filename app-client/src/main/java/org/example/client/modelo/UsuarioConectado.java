package org.example.client.modelo;

import java.io.Serializable;

/**
 * Representa un usuario conectado al servidor con su foto de perfil.
 * Se usa para mostrar la lista de usuarios en línea.
 */
public class UsuarioConectado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String fotoBase64;

    public UsuarioConectado() {
    }

    public UsuarioConectado(String username, String fotoBase64) {
        this.username = username;
        this.fotoBase64 = fotoBase64;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    @Override
    public String toString() {
        return "UsuarioConectado{" +
                "username='" + username + '\'' +
                ", tieneFoto=" + (fotoBase64 != null && !fotoBase64.equals("DEFAULT")) +
                '}';
    }
}
