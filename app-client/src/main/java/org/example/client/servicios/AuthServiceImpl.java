package org.example.client.servicios;

import org.example.common.entidades.Usuario;
import org.example.common.servicios.AuthService;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private Usuario usuarioActual;

    @Override
    public boolean registrar(Usuario usuario, String contrasena) {
        // En esta fase solo simula el registro local, luego se enviará al servidor
        this.usuarioActual = usuario;
        System.out.println("Usuario registrado localmente: " + usuario.getNombre());
        return true;
    }

    @Override
    public Optional<Usuario> login(String correo, String contrasena) {
        // En la siguiente fase se implementará la autenticación real con el servidor
        if (usuarioActual != null && usuarioActual.getCorreo().equals(correo)) {
            return Optional.of(usuarioActual);
        }
        return Optional.empty();
    }

    @Override
    public void logout(String usuarioId) {
        System.out.println("Usuario desconectado: " + usuarioId);
        usuarioActual = null;
    }

    @Override
    public boolean existeCorreo(String correo) {
        return usuarioActual != null && usuarioActual.getCorreo().equals(correo);
    }
}
