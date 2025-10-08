package org.example.client;

import org.example.client.config.ConfigManager;
import org.example.client.tcp.GestorConexionesCliente;
import org.example.common.entidades.MensajeAutenticacion;
import org.example.common.entidades.MensajeRespuesta;
import org.example.common.entidades.MensajeTexto;
import org.example.common.entidades.Usuario;

import java.util.Scanner;
import java.util.UUID;

public class MainClient {
    public static void main(String[] args) {
        ConfigManager config = new ConfigManager("client-config.properties");
        String host = config.get("server.host");
        int port = config.getInt("server.port");

        GestorConexionesCliente conexion = new GestorConexionesCliente();
        if (!conexion.conectar(host, port)) {
            System.err.println("❌ No se pudo conectar al servidor.");
            return;
        }

        System.out.println("✅ Conexión exitosa al servidor.");

        // Crear usuario y mensaje de autenticación
        Usuario dummy = new Usuario(null, "prueba2", "cliente2@uni.edu", null, null);
        MensajeAutenticacion auth = new MensajeAutenticacion(
                UUID.randomUUID().toString(),
                dummy,
                "cliente2@uni.edu",
                "1234"
        );

        System.out.println("JSON autenticación -> " +
                org.example.common.protocolo.AnalizadorProtocolo.serializarAString(auth));

        try {
            // 1️⃣ Enviar autenticación
            conexion.enviarMensaje(auth);

            // 2️⃣ Esperar respuesta del servidor antes de continuar
            var respuesta = conexion.leerMensaje();
            if (respuesta instanceof MensajeRespuesta resp) {
                System.out.println("📨 Respuesta del servidor: " + resp.getMensaje());
                if (!resp.isExito()) {
                    System.err.println("❌ Autenticación fallida. Cerrando conexión.");
                    conexion.cerrarConexion();
                    return;
                }
            } else {
                System.err.println("⚠️ Respuesta inesperada del servidor: " +
                        respuesta.getClass().getSimpleName());
                conexion.cerrarConexion();
                return;
            }

            // 3️⃣ Hilo lector: mantiene la conexión viva y recibe mensajes broadcast
            new Thread(() -> {
                try {
                    while (true) {
                        var mensaje = conexion.leerMensaje();
                        System.out.println("📩 Nuevo mensaje recibido: " +
                                mensaje.getClass().getSimpleName());
                        if (mensaje instanceof MensajeTexto txt) {
                            System.out.println("💬 " + txt.getRemitente().getNombre() + ": " + txt.getContenido());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Conexión finalizada: " + e.getMessage());
                }
            }, "ListenerThread").start();

            // 4️⃣ Bucle principal: leer entrada del usuario y enviar mensajes
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("Escribe un mensaje (o 'salir'): ");
                String texto = sc.nextLine();
                if (texto.equalsIgnoreCase("salir")) break;

                MensajeTexto msg = new MensajeTexto(
                        UUID.randomUUID().toString(),
                        dummy,
                        texto
                );
                conexion.enviarMensaje(msg);
            }

            // 5️⃣ Cerrar conexión manualmente
            conexion.cerrarConexion();
            System.out.println("👋 Cliente cerrado correctamente.");

        } catch (Exception e) {
            System.err.println("❌ Error en cliente: " + e.getMessage());
            e.printStackTrace();
            conexion.cerrarConexion();
        }
    }
}
