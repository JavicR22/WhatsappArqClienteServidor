package org.example.controladores;

import org.example.configuracioConexion.ChatServer;
import org.example.entidades.ConfiguracionConexiones;
import org.example.entidades.Usuario;
import org.example.eventos.MensajeriaDispatcher;
import org.example.servicio.CanalService;
import org.example.servicio.MensajeriaService;
import org.example.servicio.impl.UsuarioServiceImpl;

import java.util.Collection;
import java.util.Collections;

public class ServidorControlador {

    private ChatServer chatServer;
    private final UsuarioServiceImpl usuarioService;
    private final MensajeriaService mensajeriaService;
    private MensajeriaDispatcher dispatcher;
    private final CanalService canalService;

    public ServidorControlador(UsuarioServiceImpl usuarioService, MensajeriaDispatcher dispatcher,
                               MensajeriaService mensajeService, CanalService canalService) {
        this.usuarioService = usuarioService;
        this.dispatcher = dispatcher; // 🔹 Inicializamos el dispatcher aquí
        this.mensajeriaService=mensajeService;
        this.canalService=canalService;
    }

    /**
     * Inicia el servidor de chat en el puerto indicado, con la configuración dada.
     */
    public void iniciarServidor(int puerto, int maxUsuarios, boolean esLimitado) {
        ConfiguracionConexiones config = new ConfiguracionConexiones(maxUsuarios, esLimitado);
        chatServer = new ChatServer(puerto, config, usuarioService, dispatcher, mensajeriaService, canalService);
        new Thread(chatServer::iniciar).start();
    }

    /**
     * Registra un nuevo usuario en el sistema.
     */
    public void registrarUsuario(String username, String email, String password, String ip, String rutaFoto) {
        Usuario u = new Usuario(username, email, password, rutaFoto, ip);
        usuarioService.registrarUsuario(u);
    }

    /**
     * Retorna la lista de usuarios actualmente conectados.
     */
    public Collection<String> obtenerUsuariosConectados() {
        if (chatServer != null) {
            return chatServer.getPool().listarUsuariosConectados();
        }
        return Collections.emptyList();
    }

    /**
     * Cierra la conexión de un usuario específico.
     */
    public void cerrarConexion(String username) {
        if (chatServer != null) {
            chatServer.getPool().removerConexion(username);
        }
    }

    /**
     * Detiene completamente el servidor.
     */
    public void detenerServidor() {
        if (chatServer != null) {
            chatServer.stop();
            System.out.println("🛑 Servidor detenido desde el controlador.");
        }
    }

    /**
     * Retorna el dispatcher (por si otros componentes lo necesitan).
     */
    public MensajeriaDispatcher getDispatcher() {
        return dispatcher;
    }
}
