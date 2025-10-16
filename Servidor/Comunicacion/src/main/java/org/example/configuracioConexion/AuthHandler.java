package org.example.configuracioConexion;

import org.example.objectPool.ConnectionPool;
import org.example.FileBase64Encoder;
import org.example.entidades.Usuario;
import org.example.servicio.MensajeriaService;
import org.example.servicio.UsuarioService;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class AuthHandler implements Runnable {

    private final Socket socket;
    private final ConnectionPool pool;
    private final UsuarioService usuarioService;
    private final MensajeriaService mensajeriaService;

    public AuthHandler(Socket socket, ConnectionPool pool, UsuarioService usuarioService,
                       MensajeriaService mensajeriaService) {
        this.socket = socket;
        this.pool = pool;
        this.usuarioService = usuarioService;
        this.mensajeriaService=mensajeriaService;
    }

    @Override
    public void run() {
        BufferedReader input = null;
        PrintWriter output = null;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // 1️⃣ Solicitar credenciales
            output.println("LOGIN");

            // Cliente envía: username|password
            String credenciales = input.readLine();
            if (credenciales == null || !credenciales.contains("|")) {
                output.println("ERR:Formato inválido. Use username|password");
                socket.close();
                return;
            }

            String[] partes = credenciales.split("\\|", 2);
            String username = partes[0];
            String password = partes[1];

            // 2️⃣ Autenticación
            Optional<Usuario> usuarioOpt = usuarioService.iniciarSesion(username, password);

            if (usuarioOpt.isEmpty()) {
                output.println("ERR:Credenciales inválidas");
                socket.close();
                return;
            }

            // 3️⃣ Crear conexión
            Connection conexion = new Connection(socket, username);

            // 4️⃣ Registrar en el Pool
            boolean aceptado = pool.agregarConexion(username, conexion);
            if (!aceptado) {
                output.println("ERR:Servidor lleno o usuario ya conectado");
                socket.close();
                return;
            }

            // 5️⃣ Notificar éxito
            output.println("OK:Bienvenido " + username);

            // 6️⃣ Enviar foto (si existe)
            Usuario usuario = usuarioOpt.get();
            String rutaFoto = usuario.getRutaFoto();
            String fotoBase64 = FileBase64Encoder.encodeFileToBase64(rutaFoto);

            if (fotoBase64 != null) {
                output.println("FOTO_AVATAR:" + fotoBase64);
            } else {
                output.println("FOTO_AVATAR:DEFAULT");
            }

            // 7️⃣ Entregar control a ClientHandler
            new Thread(new ClientHandler(pool, conexion, mensajeriaService)).start();

        } catch (IOException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
