package org.example.controladores;

import org.example.entidades.Usuario;
import org.example.servicio.impl.UsuarioServiceImpl;

import java.util.List;

public class UsuarioControlador {
    private final UsuarioServiceImpl usuarioService;

    public UsuarioControlador(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }
    public List<Usuario> listarUsuario(){
        return usuarioService.listarUsuarios();
    }
    public Usuario buscarUsuarioPorUsername(String username){
        return usuarioService.buscarPorUsername(username);
    }
}
