package org.example.common.servicios;

import org.example.common.entidades.Mensaje;
import org.example.common.entidades.Usuario;
import org.example.common.entidades.Canal;
import java.util.List;

public interface MessageService {

    void enviarMensajePrivado(Usuario remitente, Usuario destinatario, Mensaje mensaje);

    void enviarMensajeCanal(Usuario remitente, Canal canal, Mensaje mensaje);

    void enviarMensajeBroadcast(Usuario remitente, Mensaje mensaje);

    List<Mensaje> obtenerHistorialPrivado(String usuarioAId, String usuarioBId);

    List<Mensaje> obtenerHistorialCanal(String canalId);
}
