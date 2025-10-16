package org.example.repositorio;

import org.example.entidades.Canal;

import java.util.List;
import java.util.Optional;

public interface CanalRepositorio {
    /**
     * Guarda un nuevo canal en la base de datos y asigna el ID generado.
     */
    void guardar(Canal canal);

    /**
     * Busca un canal por su ID.
     */
    Optional<Canal> buscarPorId(int idCanal);

    /**
     * Busca un canal por su nombre único.
     */
    Optional<Canal> buscarPorNombre(String nombreCanal);

    /**
     * Lista todos los canales existentes.
     */
    List<Canal> listarTodos();
}
