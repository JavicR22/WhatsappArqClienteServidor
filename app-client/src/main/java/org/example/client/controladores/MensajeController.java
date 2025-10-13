package org.example.client.controladores;

import org.example.client.negocio.MensajeBusinessLogic;
import org.example.client.modelo.Mensaje;

/**
 * CAPA DE INTERMEDIARIOS/CONTROLADORES
 * Controla el flujo de mensajes entre la UI y la lógica de negocio
 */
public class MensajeController {
    
    private final MensajeBusinessLogic mensajeBusinessLogic;
    
    public MensajeController(MensajeBusinessLogic mensajeBusinessLogic) {
        this.mensajeBusinessLogic = mensajeBusinessLogic;
    }
    
    /**
     * Envía un mensaje de texto al servidor
     */
    public boolean enviarMensaje(String contenido) {
        if (contenido == null || contenido.trim().isEmpty()) {
            return false;
        }
        
        return mensajeBusinessLogic.enviarMensajeTexto(contenido);
    }
    
    /**
     * Inicia el listener de mensajes entrantes
     */
    public void iniciarEscuchaMensajes(MensajeListener listener) {
        mensajeBusinessLogic.iniciarEscuchaMensajes(listener);
    }
    
    /**
     * Interfaz para notificar mensajes recibidos a la UI
     */
    public interface MensajeListener {
        void onMensajeRecibido(Mensaje mensaje);
        void onError(String error);
        void onConexionCerrada();
    }
}
