package org.example.servicio.impl;

import org.example.entidades.Canal;
import org.example.entidades.Usuario;
import org.example.eventos.MensajeriaDispatcher;
import org.example.repositorio.CanalRepositorio;
import org.example.servicio.CanalService;
import org.example.servicio.UsuarioService;

import java.util.List;
import java.util.Optional;

public class CanalServiceImpl implements CanalService {
    // Inyección de dependencia del Repositorio
    private final CanalRepositorio canalRepositorio;
    private final MensajeriaDispatcher dispatcher;
    private final UsuarioService usuarioService;

    public CanalServiceImpl(CanalRepositorio canalRepositorio, MensajeriaDispatcher mensajeriaDispatcher,
                            UsuarioService usuarioService) {
        this.canalRepositorio = canalRepositorio;
        this.dispatcher=mensajeriaDispatcher;
        this.usuarioService=usuarioService;
    }


    @Override
    public Canal crearCanal(Canal canal) throws Exception {
        // ✅ Validación básica
        if (canal == null || canal.getNombreCanal() == null || canal.getNombreCanal().trim().isEmpty()) {
            throw new Exception("El nombre del canal no puede estar vacío o nulo.");
        }

        String nombreCanal = canal.getNombreCanal();

        // ✅ 1. Verificar si ya existe un canal con ese nombre
        if (canalRepositorio.buscarPorNombre(nombreCanal).isPresent()) {
            System.out.println("Canal ya existe no se puede crear");
            return null;
        }

        try {
            // ✅ 2. Guardar canal
            canalRepositorio.guardar(canal);
            System.out.println(canal.getDescripcion()+" "+canal.getUsernameCreador());

            // ✅ 3. Recuperar el canal recién creado (para obtener su ID u otros datos generados)
            Optional<Canal> canalCreadoOpt = obtenerCanalPorNombre(nombreCanal);
            if (canalCreadoOpt.isEmpty()) {
                throw new Exception("No fue posible recuperar el canal después de guardarlo.");
            }
            Canal canalCreado = canalCreadoOpt.get();

            // ✅ 4. Notificar creación
            notificarCreacionCanal(canalCreado);

            return canalCreado;

        } catch (Exception e) {
            // ✅ Manejo centralizado de errores
            throw new Exception("Fallo en la creación del canal: " + e.getMessage(), e);
        }
    }
    private void notificarCreacionCanal(Canal canalCreado) {
        String protocolo = String.format(
                "CANAL_CREADO|%s|%s|%s|%s|%s",
                canalCreado.getIdCanal(),
                canalCreado.getNombreCanal(),
                canalCreado.getDescripcion(),
                canalCreado.getPrivado(),
                canalCreado.getUsernameCreador()
        );

        dispatcher.notificarCreacionCanal(canalCreado.getUsernameCreador(), protocolo);
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
