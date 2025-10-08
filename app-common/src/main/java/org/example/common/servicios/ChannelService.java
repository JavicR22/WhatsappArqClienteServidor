package org.example.common.servicios;

import org.example.common.entidades.Canal;
import org.example.common.entidades.Usuario;
import org.example.common.entidades.Solicitud;
import java.util.List;

public interface ChannelService {

    Canal crearCanal(String nombre, Usuario creador);

    void eliminarCanal(String canalId);

    void agregarUsuarioACanal(String canalId, Usuario usuario);

    void removerUsuarioDeCanal(String canalId, Usuario usuario);

    List<Canal> listarCanalesPorUsuario(String usuarioId);

    Solicitud enviarSolicitudUnion(String canalId, Usuario solicitante);

    void responderSolicitud(String solicitudId, boolean aceptar);
}
