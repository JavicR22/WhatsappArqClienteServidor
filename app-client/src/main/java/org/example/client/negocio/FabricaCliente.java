package org.example.client.negocio;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.config.ConfigManager;

public class FabricaCliente {

    public static ClienteApp crearCliente(ConfigManager config) {
        GestorComunicacion gestor = new GestorComunicacion();
        AuthBusinessLogic auth = new AuthBusinessLogic(gestor);
        MensajeBusinessLogic mensajes = new MensajeBusinessLogic(gestor, auth);
        ServicioNotificaciones notificaciones = new ServicioNotificaciones();

        return new ClienteApp(config, gestor, auth, mensajes, notificaciones);
    }
}
