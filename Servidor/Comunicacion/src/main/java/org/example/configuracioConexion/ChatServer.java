package org.example.configuracioConexion;

import org.example.FileBase64Encoder;
import org.example.entidades.Usuario;
import org.example.eventos.MensajeriaDispatcher;
import org.example.eventos.MensajeriaObserver;
import org.example.objectPool.ConnectionPool;
import org.example.entidades.ConfiguracionConexiones;
import org.example.servicio.CanalService;
import org.example.servicio.MensajeriaService;
import org.example.servicio.UsuarioService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor principal del chat.
 * Administra las conexiones entrantes, verifica autenticación y coordina los mensajes mediante el dispatcher.
 */
public class ChatServer implements MensajeriaObserver {

    private final int puerto;
    private final ConnectionPool pool;
    private final UsuarioService usuarioService;
    private final MensajeriaDispatcher dispatcher;
    private ServerSocket serverSocket;
    private final MensajeriaService mensajeriaService;
    private final CanalService canalService;
    private volatile boolean enEjecucion = false; // bandera segura para hilos

    public ChatServer(int puerto,
                      ConfiguracionConexiones config,
                      UsuarioService usuarioService,
                      MensajeriaDispatcher dispatcher,
                      MensajeriaService mensajeriaService,
                      CanalService canalService) {
        this.puerto = puerto;
        this.usuarioService = usuarioService;
        this.dispatcher = dispatcher;
        this.pool = new ConnectionPool(config, dispatcher);
        this.dispatcher.addObserver(this);
        this.mensajeriaService=mensajeriaService;
        this.canalService=canalService;
    }

    @Override
    public void onMensajePrivado(String usernameDestino, String mensaje) {
        var conexionDestino = pool.obtenerConexion(usernameDestino);
        if (conexionDestino != null) {
            conexionDestino.enviar(mensaje);
        } else {
            System.out.println("⚠️ No se pudo enviar mensaje privado. Usuario no conectado: " + usernameDestino);
        }
    }

    @Override
    public void onMensajeCanal(String idCanal, String mensaje) {
        for (var usuario : pool.listarUsuariosConectados()) {
            var conexion = pool.obtenerConexion(usuario);
            if (conexion != null && conexion.estaEnCanal(idCanal)) {
                conexion.enviar(mensaje);
            }
        }
    }

    @Override
    public void onUsuarioConectado(String username) {
        broadcastUsuariosConectados();    }

    @Override
    public void onNuevaCanal(String username, String protocolo) {
        var conexionDestino = pool.obtenerConexion(username);
        if (conexionDestino != null) {
            conexionDestino.enviar(protocolo);
        } else {
            System.out.println("⚠️ No se pudo enviar mensaje privado. Usuario no conectado: " + username);
        }
    }

    @Override
    public void onUsuarioDesconectado(String username) {
        broadcastUsuariosConectados();    }

    /**
     * Inicia el servidor y escucha nuevas conexiones.
     */
    public void iniciar() {
        enEjecucion = true;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            this.serverSocket = serverSocket;

            System.out.println("🚀 Servidor iniciado en puerto " + puerto);
            System.out.println("💡 Límite máximo de usuarios: " + pool.getConfig().getMaxUsuariosConectados());

            while (enEjecucion) {
                Socket socket = serverSocket.accept();
                String ip = socket.getInetAddress().getHostAddress();
                System.out.println("🟢 Conexión entrante desde " + ip);

                // Verificar límite de usuarios
                if (pool.getConfig().esLimitado() &&
                        pool.getNumeroConexiones() >= pool.getConfig().getMaxUsuariosConectados()) {
                    System.out.println("⚠️ Servidor lleno. Rechazando conexión desde " + ip);
                    try (PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
                        output.println("ERR:Servidor lleno. Intente más tarde.");
                    }
                    socket.close();
                    continue;
                }

                // Manejar autenticación y registro de usuario
                new Thread(new AuthHandler(socket, pool, usuarioService, mensajeriaService, canalService)).start();
            }

        } catch (IOException e) {
            if (enEjecucion) {
                System.err.println("❌ Error en el servidor: " + e.getMessage());
            } else {
                System.out.println("🧱 Servidor cerrado correctamente.");
            }
        }
    }

    /**
     * Envía un mensaje a todos los usuarios conectados.
     */
    private void broadcast(String mensaje) {
        for (String usuario : pool.listarUsuariosConectados()) {
            var conexion = pool.obtenerConexion(usuario);
            if (conexion != null) {
                conexion.enviar(mensaje);
            }
        }
    }
    private void broadcastUsuariosConectados() {
        var usuarios = pool.listarUsuariosConectados();
        StringBuilder builder = new StringBuilder("USUARIOS_CONECTADOS|");

        for (String user : usuarios) {
            Usuario u = usuarioService.buscarPorUsername(user);
            String ruta = u != null ? u.getRutaFoto() : null;
            String foto = FileBase64Encoder.encodeFileToBase64(ruta);
            builder.append(user)
                    .append(":")
                    .append(foto != null ? foto : "DEFAULT")
                    .append(";");
        }

        String mensaje = builder.toString();
        pool.getConexionesActivas().values().forEach(conn -> conn.enviar(mensaje));
    }


    /**
     * Detiene el servidor y cierra todas las conexiones.
     */
    public void stop() {
        System.out.println("⏹️ Deteniendo servidor...");
        enEjecucion = false;

        // 1️⃣ Cerrar el ServerSocket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("🧱 Socket del servidor cerrado.");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cerrar el ServerSocket: " + e.getMessage());
        }

        // 2️⃣ Cerrar todas las conexiones activas
        try {
            pool.stop();
        } catch (Exception e) {
            System.err.println("⚠️ Error cerrando conexiones activas: " + e.getMessage());
        }

        System.out.println("✅ Servidor detenido correctamente.");
    }

    public ConnectionPool getPool() {
        return pool;
    }
}
