package org.example.controladores;

import org.example.configuracioConexion.ChatServer;
import org.example.entidades.ConfiguracionConexiones;
import org.example.entidades.Usuario;
import org.example.servicio.impl.UsuarioServiceImpl;

import java.util.Collection;

public class ServidorControlador {
    private ChatServer chatServer;
    private final UsuarioServiceImpl usuarioService;

    public ServidorControlador(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void iniciarServidor(int puerto, int maxUsuarios, boolean esLimitado) {
        ConfiguracionConexiones config = new ConfiguracionConexiones(maxUsuarios, esLimitado);
        chatServer = new ChatServer(puerto, config, usuarioService);
        new Thread(chatServer::iniciar).start();
    }

    public void registrarUsuario(String username, String email, String password, String ip, String rutaFoto) {
        Usuario u = new Usuario(username, email, password, rutaFoto, ip);
        usuarioService.registrarUsuario(u);
    }

    public Collection<String> obtenerUsuariosConectados() {
        return chatServer != null ? chatServer.getPool().listarUsuariosConectados() : java.util.Collections.emptyList();
    }

    public void cerrarConexion(String username) {
        if (chatServer != null) {
            chatServer.getPool().removerConexion(username);
        }
    }
}
