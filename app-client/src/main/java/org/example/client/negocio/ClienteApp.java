package org.example.client.negocio;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.config.ConfigManager;

public class ClienteApp {
    private final ConfigManager config;
    private final GestorComunicacion gestor;
    private final AuthBusinessLogic auth;
    private final MensajeBusinessLogic mensajes;
    private final ServicioNotificaciones notificaciones;

    public ClienteApp(ConfigManager config, GestorComunicacion gestor,
                      AuthBusinessLogic auth, MensajeBusinessLogic mensajes,
                      ServicioNotificaciones notificaciones) {
        this.config = config;
        this.gestor = gestor;
        this.auth = auth;
        this.mensajes = mensajes;
        this.notificaciones = notificaciones;
    }

    public void iniciar() {
        String host = config.get("server.host");
        int port = config.getInt("server.port");
        gestor.conectar(host, port);
    }

    public void detener() {
        gestor.cerrarConexion();
    }

    // Getters
    public AuthBusinessLogic getAuth() { return auth; }
    public MensajeBusinessLogic getMensajes() { return mensajes; }
    public ServicioNotificaciones getNotificaciones() { return notificaciones; }
}
