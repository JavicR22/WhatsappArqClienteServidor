package org.example.configuracioConexion;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {
    private final Socket socket;
    private final String usuario;
    private final PrintWriter output;

    public Connection(Socket socket, String usuario) throws IOException {
        this.socket = socket;
        this.usuario = usuario;
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsuario() {
        return usuario;
    }

    public void enviar(String mensaje) {
        try {
            output.println(mensaje);
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando mensaje a " + usuario + ": " + e.getMessage());
        }
    }

    public void cerrar() {
        try {
            socket.close();
        } catch (Exception e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }

    // (Opcional) si más adelante manejas canales o grupos
    public boolean estaEnCanal(String idCanal) {
        // Por ahora simplemente devuelve true para evitar errores
        return true;
    }
}
