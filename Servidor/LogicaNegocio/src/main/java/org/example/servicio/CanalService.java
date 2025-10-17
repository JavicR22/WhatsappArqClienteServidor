package org.example.servicio;

import org.example.entidades.Canal;

import java.util.List;
import java.util.Optional;

public interface CanalService {
    Canal crearCanal(Canal canal) throws Exception;

    /**
     * Obtiene un canal por su nombre.
     */
    Optional<Canal> obtenerCanalPorNombre(String nombreCanal);

    /**
     * Lista todos los canales disponibles.
     */
    List<Canal> listarCanalesDisponibles();

    // Aquí irían métodos de negocio más complejos, ej:
    // void agregarMiembro(String username, String nombreCanal);
}
