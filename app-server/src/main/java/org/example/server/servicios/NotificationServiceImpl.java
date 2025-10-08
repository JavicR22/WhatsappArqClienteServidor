package org.example.server.servicios;

import org.example.common.entidades.Mensaje;
import org.example.common.entidades.Usuario;
import org.example.common.servicios.NotificationService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationServiceImpl implements NotificationService {

    // Mapa usuarioId -> Observador
    private final Map<String, ObservadorNotificacion> observadores = new ConcurrentHashMap<>();

    @Override
    public void registrarObservador(String usuarioId, ObservadorNotificacion observador) {
        observadores.put(usuarioId, observador);
    }

    @Override
    public void eliminarObservador(String usuarioId) {
        observadores.remove(usuarioId);
    }

    @Override
    public void notificarMensajeNuevo(Mensaje mensaje) {
        // Aquí se decide a qué observadores notificar. Ejemplo: si es privado, notificar receptor.
        // Para demostración: notificar a todos
        for (ObservadorNotificacion obs : observadores.values()) {
            obs.onMensajeNuevo(mensaje);
        }
    }

    @Override
    public void notificarConexion(Usuario usuario) {
        for (ObservadorNotificacion obs : observadores.values()) {
            obs.onUsuarioConectado(usuario);
        }
    }

    @Override
    public void notificarDesconexion(Usuario usuario) {
        for (ObservadorNotificacion obs : observadores.values()) {
            obs.onUsuarioDesconectado(usuario);
        }
    }
}
