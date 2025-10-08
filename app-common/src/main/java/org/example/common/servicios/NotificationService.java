package org.example.common.servicios;

import org.example.common.entidades.Mensaje;
import org.example.common.entidades.Usuario;

public interface NotificationService {

    void registrarObservador(String usuarioId, ObservadorNotificacion observador);

    void eliminarObservador(String usuarioId);

    void notificarMensajeNuevo(Mensaje mensaje);

    void notificarConexion(Usuario usuario);

    void notificarDesconexion(Usuario usuario);

    interface ObservadorNotificacion {
        void onMensajeNuevo(Mensaje mensaje);
        void onUsuarioConectado(Usuario usuario);
        void onUsuarioDesconectado(Usuario usuario);
    }
}
