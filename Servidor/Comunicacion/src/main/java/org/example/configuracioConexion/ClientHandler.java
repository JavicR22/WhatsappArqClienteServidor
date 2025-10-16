package org.example.configuracioConexion;

import org.example.entidades.MensajeTextoCanal;
import org.example.entidades.MensajeTextoPrivado;
import org.example.entidades.Usuario;
import org.example.entidades.Canal;
import org.example.objectPool.ConnectionPool;
import org.example.servicio.MensajeriaService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Clase encargada de manejar la comunicación de un cliente autenticado.
 * Escucha mensajes entrantes y los delega al MensajeriaService.
 */
public class ClientHandler implements Runnable {

    private final ConnectionPool pool;
    private final Connection conexion;
    private final MensajeriaService mensajeriaService;

    public ClientHandler(ConnectionPool pool, Connection conexion, MensajeriaService mensajeriaService) {
        this.pool = pool;
        this.conexion = conexion;
        this.mensajeriaService = mensajeriaService;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(
                new InputStreamReader(conexion.getSocket().getInputStream()));
             PrintWriter output = new PrintWriter(conexion.getSocket().getOutputStream(), true)) {

            output.println("✅ Conectado al servidor. Usa 'exit' para salir.");

            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                if ("exit".equalsIgnoreCase(mensaje.trim())) {
                    break;
                }

                procesarMensaje(mensaje);
            }

        } catch (IOException e) {
            System.err.println("⚠️ Error I/O con el cliente " + conexion.getUsuario() + ": " + e.getMessage());
        } finally {
            // 🔴 Cuando el cliente se desconecta
            String usuario = conexion.getUsuario();
            pool.removerConexion(usuario);

            // Notificar a los demás (opcional, si tienes dispatcher)
            System.out.println("👋 Usuario desconectado: " + usuario);

            conexion.cerrar();
        }
    }

    /**
     * Procesa cada mensaje recibido del cliente según su prefijo.
     */
    private void procesarMensaje(String mensaje) {
        try {
            if (mensaje.startsWith("PRIVADO|")) {
                procesarMensajePrivado(mensaje);
            } else if (mensaje.startsWith("CANAL|")) {
                procesarMensajeCanal(mensaje);
            }
        } catch (Exception e) {
            System.err.println("❌ Error procesando mensaje de " + conexion.getUsuario() + ": " + e.getMessage());
        }
    }

    /**
     * Maneja un mensaje privado. Formato esperado:
     * PRIVADO|receptor|contenido
     */
    private void procesarMensajePrivado(String mensaje) {
        String[] partes = mensaje.split("\\|", 3);
        if (partes.length < 3) return;

        String receptor = partes[1];
        String contenido = partes[2];

        try {
            System.out.println(receptor);
            MensajeTextoPrivado msg = new MensajeTextoPrivado();
            msg.setEmisor(new Usuario(conexion.getUsuario()));
            msg.setReceptor(new Usuario(receptor));
            msg.setContenidoTexto(contenido);

            // Envía el mensaje al servicio
            mensajeriaService.enviarMensajeTextoPrivado(msg);
            System.out.println("📩 PRIVADO → " + conexion.getUsuario() + " → " + receptor + ": " + contenido);
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje privado: " + e.getMessage());
        }
    }

    /**
     * Maneja un mensaje de canal. Formato esperado:
     * CANAL|idCanal|contenido
     */
    private void procesarMensajeCanal(String mensaje) {
        String[] partes = mensaje.split("\\|", 3);
        if (partes.length < 3) return;

        String idCanal = partes[1];
        String contenido = partes[2];

        try {
            MensajeTextoCanal msg = new MensajeTextoCanal();
            msg.setEmisor(new Usuario(conexion.getUsuario()));
            msg.setCanal(new Canal(Integer.parseInt(idCanal)));
            msg.setContenido(contenido);

            mensajeriaService.enviarMensajeTextoCanal(msg);
            System.out.println("📢 CANAL " + idCanal + " → " + conexion.getUsuario() + ": " + contenido);
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje de canal: " + e.getMessage());
        }
    }


}
