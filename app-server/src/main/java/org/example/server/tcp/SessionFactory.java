package org.example.server.tcp;

import org.example.server.servicios.*;
import org.example.common.repositorios.UsuarioRepository;
import org.example.common.repositorios.MensajeRepository;
import org.example.server.db.DBConnectionPool;

public class SessionFactory {

    private final AuthServiceImpl authService;
    private final NotificationServiceImpl notificationService;
    private final MessageServiceImpl messageService;
    private final ClientRegistry registry;

    public SessionFactory(UsuarioRepository usuarioRepo,
                          MensajeRepository mensajeRepo,
                          DBConnectionPool pool,
                          ClientRegistry registry) {
        this.registry = registry;
        this.authService = new AuthServiceImpl(usuarioRepo);
        this.notificationService = new NotificationServiceImpl();
        this.messageService = new MessageServiceImpl(mensajeRepo, notificationService);
    }

    public ClientSession create(java.net.Socket socket) {
        return new ClientSession(socket, registry, authService, notificationService, messageService);
    }
}
