package org.example.common.repositorios;

import org.example.common.entidades.Canal;
import java.util.List;
import java.util.Optional;

public interface CanalRepository {

    void guardar(Canal canal);

    Optional<Canal> buscarPorId(String id);

    List<Canal> listarTodos();

    void eliminar(String id);

    void agregarMiembro(String canalId, String usuarioId);

    void eliminarMiembro(String canalId, String usuarioId);
}
