package org.example.server.servicios;

import org.example.common.entidades.Canal;
import org.example.common.entidades.Mensaje;
import org.example.common.entidades.Usuario;
import org.example.common.servicios.MessageService;
import org.example.common.repositorios.MensajeRepository;
import org.example.common.servicios.NotificationService;

import java.util.List;

public class MessageServiceImpl implements MessageService {

    private final MensajeRepository mensajeRepository;
    private final NotificationService notificationService;

    public MessageServiceImpl(MensajeRepository mensajeRepository, NotificationService notificationService) {
        this.mensajeRepository = mensajeRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void enviarMensajePrivado(Usuario remitente, Usuario destinatario, Mensaje mensaje) {
        mensajeRepository.guardar(mensaje);
        // Notificar al destinatario (si está conectado)
        notificationService.notificarMensajeNuevo(mensaje);
    }

    @Override
    public void enviarMensajeCanal(Usuario remitente, Canal canal, Mensaje mensaje) {
        mensajeRepository.guardar(mensaje);
        notificationService.notificarMensajeNuevo(mensaje);
    }

    @Override
    public void enviarMensajeBroadcast(Usuario remitente, Mensaje mensaje) {
        mensajeRepository.guardar(mensaje);
        notificationService.notificarMensajeNuevo(mensaje);
    }

    @Override
    public List<Mensaje> obtenerHistorialPrivado(String usuarioAId, String usuarioBId) {
        // Implementación: consultar MensajeRepository con condiciones
        return List.of();
    }

    @Override
    public List<Mensaje> obtenerHistorialCanal(String canalId) {
        return List.of();
    }
}
