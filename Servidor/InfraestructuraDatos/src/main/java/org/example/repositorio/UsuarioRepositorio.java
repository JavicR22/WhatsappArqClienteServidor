package org.example.repositorio;

import org.example.entidades.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {
    void guardar(Usuario usuario);
    Optional<Usuario> buscarPorUsername(String username);
    List<Usuario> listarTodos();
    void eliminarPorUsername(String username);
}
