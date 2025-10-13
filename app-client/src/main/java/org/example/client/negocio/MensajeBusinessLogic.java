package org.example.client.negocio;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.controladores.MensajeController;
import org.example.client.modelo.*;

import java.util.UUID;

/**
 * CAPA DE LÓGICA DE NEGOCIO
 * Gestiona la lógica de envío y recepción de mensajes
 */
public class MensajeBusinessLogic {

    private final GestorComunicacion gestorComunicacion;
    private final AuthBusinessLogic authBusinessLogic;

    public MensajeBusinessLogic(GestorComunicacion gestorComunicacion,
                                AuthBusinessLogic authBusinessLogic) {
        this.gestorComunicacion = gestorComunicacion;
        this.authBusinessLogic = authBusinessLogic;
    }

    /**
     * Envía un mensaje de texto al servidor
     */
    public boolean enviarMensajeTexto(String contenido) {
        try {
            Usuario usuario = authBusinessLogic.obtenerUsuarioActual();
            if (usuario == null) {
                System.err.println("No hay usuario autenticado");
                return false;
            }

            MensajeTexto mensaje = new MensajeTexto(
                UUID.randomUUID().toString(),
                usuario,
                contenido
            );

            gestorComunicacion.enviarMensaje(mensaje);
            return true;

        } catch (Exception e) {
            System.err.println("Error enviando mensaje: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inicia un hilo para escuchar mensajes entrantes
     */
    public void iniciarEscuchaMensajes(MensajeController.MensajeListener listener) {
        Thread hiloEscucha = new Thread(() -> {
            try {
                while (true) {
                    Mensaje mensaje = gestorComunicacion.recibirMensaje();

                    if (mensaje == null) {
                        listener.onConexionCerrada();
                        break;
                    }

                    listener.onMensajeRecibido(mensaje);
                }
            } catch (Exception e) {
                listener.onError("Error recibiendo mensajes: " + e.getMessage());
            }
        }, "HiloEscuchaMensajes");

        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }
}
