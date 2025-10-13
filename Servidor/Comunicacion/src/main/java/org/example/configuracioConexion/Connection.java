package org.example.configuracioConexion;

import java.net.Socket;

public class Connection {
    private final Socket socket;
    private final String usuario;

    public Connection(Socket socket, String usuario) {
        this.socket = socket;
        this.usuario = usuario;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsuario() {
        return usuario;
    }

    public void cerrar() {
        try {
            socket.close();
        } catch (Exception e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }
}
