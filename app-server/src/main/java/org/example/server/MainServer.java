package org.example.server;

import org.example.server.config.ConfigManager;
import org.example.server.db.DBConnectionPool;
import org.example.server.tcp.TCPListener;
import org.example.server.tcp.ClientRegistry;
import org.example.server.tcp.SessionFactory;
import org.example.server.repositorios.impl.UsuarioRepositoryImpl;
import org.example.server.repositorios.impl.MensajeRepositoryImpl;
import org.example.server.repositorios.impl.CanalRepositoryImpl;
import org.example.server.repositorios.impl.SolicitudRepositoryImpl;

public class MainServer {
    public static void main(String[] args) {
        ConfigManager config = new ConfigManager("server-config.properties");

        String url = config.get("db.url");
        String user = config.get("db.user");
        String password = config.get("db.password");
        int maxCon = config.getInt("maxConnections");
        int port = config.getInt("server.port");

        DBConnectionPool pool = new DBConnectionPool(url, user, password, maxCon);

        var usuarioRepo = new UsuarioRepositoryImpl(pool);
        var mensajeRepo = new MensajeRepositoryImpl(pool);
        var canalRepo = new CanalRepositoryImpl(pool);
        var solicitudRepo = new SolicitudRepositoryImpl(pool);

        ClientRegistry registry = new ClientRegistry(config.getInt("maxConnections"));
        SessionFactory sessionFactory = new SessionFactory(usuarioRepo, mensajeRepo, pool, registry);

        TCPListener listener = new TCPListener(port, registry, sessionFactory);
        listener.iniciar();
    }
}
