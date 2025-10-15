package org.example.configuracioConexion;

import org.example.FileBase64Encoder;
import org.example.entidades.Usuario;
import org.example.objectPool.ConnectionPool;
import org.example.servicio.impl.UsuarioServiceImpl;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class AuthHandler implements Runnable {

    private final Socket socket;
    private final ConnectionPool pool;
    private final UsuarioServiceImpl usuarioService;

    public AuthHandler(Socket socket, ConnectionPool pool, UsuarioServiceImpl usuarioService) {
        this.socket = socket;
        this.pool = pool;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run() {
        BufferedReader input = null; // ❌ Declarar fuera del try-catch para evitar cierre automático
        PrintWriter output = null;

        try { // ❌ Usar try simple

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // 1️⃣ Solicitar credenciales
            output.println("LOGIN"); // protocolo simple

            // Cliente envía: username|password
            String credenciales = input.readLine();
            if (credenciales == null || !credenciales.contains("|")) {
                output.println("ERR:Formato inválido. Use username|password");
                socket.close();
                return;
            }

            String[] partes = credenciales.split("\\|", 2);
            String username = partes[0].trim();
            String password = partes[1].trim();
            String ipCliente = socket.getInetAddress().getHostAddress();

            // 2️⃣ Validar usuario en servicio
            Usuario usuario = usuarioService.autenticarConIP(username, password, ipCliente);
            if (usuario==null) {
                output.println("ERR:Credenciales o IP incorrectas");
                socket.close();
                return;
            }



            // 3️⃣ Agregar al pool
            Connection conexion = new Connection(socket, username);
            boolean aceptado = pool.agregarConexion(username, conexion);

            if (!aceptado) {
                output.println("ERR:Servidor lleno o usuario ya conectado");
                socket.close();
                return;
            }

            output.println("OK:Bienvenido " + username);
            String rutaFoto = usuario.getRutaFoto(); // Asumiendo que esta entidad viene del servicio
            String fotoBase64 = FileBase64Encoder.encodeFileToBase64(rutaFoto);

            if (fotoBase64 != null) {
                // Definimos un protocolo simple, ej: FOTO_AVATAR: <Base64String>
                output.println("FOTO_AVATAR:" + fotoBase64);
            } else {
                // Si no hay foto, envía un comando para que el cliente use el avatar por defecto
                output.println("FOTO_AVATAR:DEFAULT");
            }

            // 💡 Transferencia de control: El socket se entrega abierto a ClientHandler
            new Thread(new ClientHandler(pool, conexion)).start();

        } catch (IOException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            // Cerrar el socket si el fallo ocurre antes de la transferencia
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException closeE) {
                // Ignorar error de cierre
            }
        }
    }
}