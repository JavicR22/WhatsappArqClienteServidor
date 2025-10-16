package org.example.repositorio;

import org.example.entidades.MensajeTextoPrivado;

public interface MensajeTextoPrivadoRepositorio {
    /**
     * Guarda un nuevo mensaje de texto privado en la base de datos.
     */
    void guardar(MensajeTextoPrivado mensaje);
    // Otros métodos de consulta (ej. List<MensajeTextoPrivado> obtenerHistorial(String emisor, String receptor);)
}