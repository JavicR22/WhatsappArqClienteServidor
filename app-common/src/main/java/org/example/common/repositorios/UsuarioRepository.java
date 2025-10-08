package org.example.common.repositorios;

import org.example.common.entidades.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    void guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(String id);

    Optional<Usuario> buscarPorCorreo(String correo);

    List<Usuario> listarTodos();

    void eliminar(String id);
}
