package org.example.configuracioConexion;

import org.example.entidades.ConfiguracionConexiones;
import org.example.objectPool.ConnectionPool;

import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private final int puerto;
    private final ConnectionPool pool;

    public ChatServer(int puerto, ConfiguracionConexiones config) {
        this.puerto = puerto;
        this.pool = new ConnectionPool(config);
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("🚀 Servidor iniciado en puerto " + puerto);

            while (true) {
                Socket socket = serverSocket.accept();

                // Aquí puedes implementar un protocolo de login simple
                String usuario = "Usuario" + (pool.getNumeroConexiones() + 1);

                Connection conexion = new Connection(socket, usuario);
                boolean aceptado = pool.agregarConexion(usuario, conexion);

                if (aceptado) {
                    new Thread(new ClientHandler(pool, conexion)).start();
                } else {
                    socket.close(); // Rechaza la conexión
                }
            }

        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}
