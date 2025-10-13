package org.example.server;

import org.example.server.config.ConfigManager;
import org.example.server.db.DBConnectionPool;
import org.example.server.repositorios.impl.*;
import org.example.server.tcp.ClientRegistry;
import org.example.server.tcp.SessionFactory;
import org.example.server.tcp.TCPListener;

public class EmbeddedServer {

    private static TCPListener listener;
    private static Thread serverThread;

    public static synchronized void start() {
        if (serverThread != null && serverThread.isAlive()) {
            System.out.println("Servidor ya en ejecución (embebido).");
            return;
        }

        serverThread = new Thread(() -> {
            try {
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

                listener = new TCPListener(port, registry, sessionFactory);
                listener.iniciar(); // BLOQUEA hasta que stop() sea llamado
            } catch (Exception e) {
                System.err.println("Error en EmbeddedServer: " + e.getMessage());
                e.printStackTrace();
            }
        }, "EmbeddedServerThread");

        serverThread.setDaemon(true);
        serverThread.start();
        System.out.println("Embedded server thread arrancada.");
    }

    public static synchronized void stop() {
        try {
            if (listener != null) {
                listener.stop();
            }
            if (serverThread != null) {
                serverThread.join(2000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            listener = null;
            serverThread = null;
            System.out.println("Embedded server detenido (intentado).");
        }
    }

    public static synchronized boolean isRunning() {
        return serverThread != null && serverThread.isAlive();
    }
}
