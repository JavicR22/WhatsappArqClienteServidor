package org.example.servicio.impl;

import org.example.entidades.Canal;
import org.example.repositorio.CanalRepositorio;
import org.example.servicio.CanalService;

import java.util.List;
import java.util.Optional;

public class CanalServiceImpl implements CanalService {
    // Inyección de dependencia del Repositorio
    private final CanalRepositorio canalRepositorio;

    public CanalServiceImpl(CanalRepositorio canalRepositorio) {
        this.canalRepositorio = canalRepositorio;
    }

    @Override
    public Canal crearCanal(String nombreCanal) throws Exception {
        if (nombreCanal == null || nombreCanal.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del canal no puede estar vacío.");
        }

        // 1. Lógica de negocio: Verificar si ya existe un canal con ese nombre
        if (canalRepositorio.buscarPorNombre(nombreCanal).isPresent()) {
            throw new Exception("El canal '" + nombreCanal + "' ya existe. Por favor, elija otro nombre.");
        }

        // 2. Crear y guardar el nuevo canal
        Canal nuevoCanal = new Canal(nombreCanal);

        try {
            canalRepositorio.guardar(nuevoCanal);
            return nuevoCanal;
        } catch (Exception e) {
            // Manejar excepciones específicas del Repositorio
            throw new Exception("Fallo en la creación del canal debido a un error de base de datos.");
        }
    }

    @Override
    public Optional<Canal> obtenerCanalPorNombre(String nombreCanal) {
        return canalRepositorio.buscarPorNombre(nombreCanal);
    }

    @Override
    public List<Canal> listarCanalesDisponibles() {
        return canalRepositorio.listarTodos();
    }
}
