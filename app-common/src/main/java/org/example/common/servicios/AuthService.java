package org.example.common.servicios;

import org.example.common.entidades.Usuario;
import java.util.Optional;

public interface AuthService {

    boolean registrar(Usuario usuario, String contrasena);

    Optional<Usuario> login(String correo, String contrasena);

    void logout(String usuarioId);

    boolean existeCorreo(String correo);
}
