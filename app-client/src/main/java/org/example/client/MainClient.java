package org.example.client;

import org.example.client.controladores.AuthController;
import org.example.client.controladores.MensajeController;
import org.example.client.negocio.AuthBusinessLogic;
import org.example.client.negocio.MensajeBusinessLogic;
import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.config.ConfigManager;
import org.example.client.modelo.*;

import java.util.Scanner;

/**
 * CAPA DE PRESENTACIÓN (Consola)
 * Cliente de consola que utiliza la arquitectura por capas.
 * Se comunica únicamente con los controladores.
 */
public class MainClient {
    public static void main(String[] args) {
        ConfigManager config = new ConfigManager("client-config.properties");
        String host = config.get("server.host");
        int port = config.getInt("server.port");

        // Inicializar capa de comunicación
        GestorComunicacion gestorComunicacion = new GestorComunicacion();
        if (!gestorComunicacion.conectar(host, port)) {
            System.err.println("❌ No se pudo conectar al servidor.");
            return;
        }

        System.out.println("✅ Conexión exitosa al servidor.");

        // Inicializar capa de lógica de negocio
        AuthBusinessLogic authBusinessLogic = new AuthBusinessLogic(gestorComunicacion);
        MensajeBusinessLogic mensajeBusinessLogic = new MensajeBusinessLogic(
            gestorComunicacion, 
            authBusinessLogic
        );

        // Inicializar controladores
        AuthController authController = new AuthController(authBusinessLogic);
        MensajeController mensajeController = new MensajeController(mensajeBusinessLogic);

        // Autenticación
        String correo = "cliente2@uni.edu";
        String contrasena = "1234";
        
        System.out.println("🔐 Autenticando usuario: " + correo);
        
        boolean autenticado = authController.autenticar(correo, contrasena);
        
        if (!autenticado) {
            System.err.println("❌ Autenticación fallida. Cerrando conexión.");
            gestorComunicacion.cerrarConexion();
            return;
        }

        System.out.println("✅ Autenticación exitosa");

        // Iniciar escucha de mensajes
        mensajeController.iniciarEscuchaMensajes(new MensajeController.MensajeListener() {
            @Override
            public void onMensajeRecibido(Mensaje mensaje) {
                if (mensaje instanceof MensajeTexto txt) {
                    System.out.println("💬 " + txt.getRemitente().getNombre() + ": " + txt.getContenido());
                }
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ " + error);
            }

            @Override
            public void onConexionCerrada() {
                System.out.println("⚠️ Conexión finalizada");
            }
        });

        // Bucle principal: enviar mensajes
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("Escribe un mensaje (o 'salir'): ");
            String texto = sc.nextLine();
            if (texto.equalsIgnoreCase("salir")) break;

            mensajeController.enviarMensaje(texto);
        }

        // Cerrar sesión
        authController.desconectar(authBusinessLogic.obtenerUsuarioActual().getId());
        System.out.println("👋 Cliente cerrado correctamente.");
    }
}
