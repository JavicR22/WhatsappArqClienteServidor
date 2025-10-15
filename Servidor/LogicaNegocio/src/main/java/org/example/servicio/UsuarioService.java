package org.example.servicio;

import org.example.entidades.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    void registrarUsuario(Usuario usuario);
    Optional<Usuario> iniciarSesion(String username, String password);
    List<Usuario> listarUsuarios();
    void eliminarUsuario(String username);
}