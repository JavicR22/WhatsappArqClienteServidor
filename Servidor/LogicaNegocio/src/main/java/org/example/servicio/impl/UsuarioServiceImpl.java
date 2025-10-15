package org.example.servicio.impl;

import org.example.entidades.Usuario;
import org.example.repositorio.UsuarioRepositorio;
import org.example.servicio.UsuarioService;

import java.util.List;
import java.util.Optional;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioServiceImpl(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public void registrarUsuario(Usuario usuario) {
        usuarioRepositorio.guardar(usuario);
    }

    @Override
    public Optional<Usuario> iniciarSesion(String username, String password) {
        Optional<Usuario> usuario = usuarioRepositorio.buscarPorUsername(username);
        if (usuario.isEmpty()) {
            System.out.println("❌ Usuario no encontrado: " + username);
            return Optional.empty();
        }
        if (!usuario.get().getPassword().equals(password)) {
            System.out.println("❌ Contraseña incorrecta para: " + username);
            return Optional.empty();
        }
        System.out.println("✅ Usuario autenticado: " + username);
        return usuario;
    }

    public Usuario buscarPorUsername(String username){
        Optional<Usuario> usuarioOpt = usuarioRepositorio.buscarPorUsername(username);
        if (usuarioOpt.isEmpty()) {
            System.out.println("❌ Usuario no encontrado: " + username);
            return null;
        }
        return usuarioOpt.get();
    }
    public Usuario autenticarConIP(String username, String password, String ipCliente) {


        Usuario usuario = buscarPorUsername(username);

        if (!usuario.getPassword().equals(password)) {
            System.out.println("❌ Contraseña incorrecta para: " + username);
            return null;
        }

        if (usuario.getDireccionIP() != null && !usuario.getDireccionIP().equals(ipCliente)) {
            System.out.println("❌ IP no autorizada: " + ipCliente + " (esperada: " + usuario.getDireccionIP() + ")");
            return null;
        }

        System.out.println("✅ Acceso concedido: " + username + " desde IP " + ipCliente);
        return usuario;
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.listarTodos();
    }

    @Override
    public void eliminarUsuario(String username) {
        usuarioRepositorio.eliminarPorUsername(username);
    }
}
