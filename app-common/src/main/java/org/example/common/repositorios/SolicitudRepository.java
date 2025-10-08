package org.example.common.repositorios;

import org.example.common.entidades.Solicitud;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository {

    void guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(String id);

    List<Solicitud> listarPorReceptor(String receptorId);

    void actualizarEstado(String id, String nuevoEstado);
}
