package org.example.common.repositorios;

import org.example.common.entidades.Mensaje;
import java.util.List;

public interface MensajeRepository {

    void guardar(Mensaje mensaje);

    List<Mensaje> listarPorUsuario(String usuarioId);

    List<Mensaje> listarPorCanal(String canalId);

    void eliminar(String id);
}
