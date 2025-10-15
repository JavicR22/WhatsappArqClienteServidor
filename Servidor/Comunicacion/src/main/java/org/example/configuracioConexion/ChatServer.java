package org.example.configuracioConexion;

import org.example.entidades.ConfiguracionConexiones;
import org.example.objectPool.ConnectionPool;
import org.example.servicio.impl.UsuarioServiceImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private final int puerto;
    private final ConnectionPool pool;
    private UsuarioServiceImpl usuarioService;
    private ServerSocket serverSocket;
    private volatile boolean enEjecucion = false; // flag seguro para hilos


    public ChatServer(int puerto, ConfiguracionConexiones config, UsuarioServiceImpl usuarioService) {
        this.puerto = puerto;
        this.pool = new ConnectionPool(config);
        this.usuarioService = usuarioService;
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("🚀 Servidor iniciado en puerto " + puerto);
            System.out.println("💡 Límite máximo de usuarios: " + pool.getConfig().getMaxUsuariosConectados());

            while (true) {
                Socket socket = serverSocket.accept();
                String ip = socket.getInetAddress().getHostAddress();
                System.out.println("🟢 Conexión entrante desde " + ip);

                // 🔒 Verificar si hay cupo antes de procesar login
                if (pool.getConfig().esLimitado() &&
                        pool.getNumeroConexiones() >= pool.getConfig().getMaxUsuariosConectados()) {

                    System.out.println("⚠️ Servidor lleno. Rechazando conexión desde " + ip);
                    try (PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
                        output.println("ERR:Servidor lleno. Intente más tarde.");
                    }
                    socket.close();
                    continue;
                }

                // 🧠 Si hay cupo, manejar autenticación
                new Thread(new AuthHandler(socket, pool, usuarioService)).start();
            }

        } catch (Exception e) {
            System.err.println("❌ Error en el servidor: " + e.getMessage());
        }
    }
    public ConnectionPool getPool(){
        return pool;
    }
    public void stop() {
        System.out.println("⏹️ Deteniendo servidor...");
        enEjecucion = false;

        // 1️⃣ Cerrar el ServerSocket para detener accept()
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("🧱 Socket del servidor cerrado.");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cerrar el ServerSocket: " + e.getMessage());
        }

        // 2️⃣ Cerrar todas las conexiones activas del pool
        try {
            cerrarTodasLasConexiones();
        } catch (Exception e) {
            System.err.println("⚠️ Error cerrando conexiones activas: " + e.getMessage());
        }

        System.out.println("✅ Servidor detenido correctamente.");
    }
    private void cerrarTodasLasConexiones() {
        for (String usuario : pool.listarUsuariosConectados()) {
            pool.removerConexion(usuario);
        }
    }

}
