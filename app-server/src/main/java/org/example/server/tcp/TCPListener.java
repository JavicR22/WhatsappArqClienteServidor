package org.example.server.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCPListener: escucha conexiones TCP entrantes en el puerto configurado.
 * Al aceptar una conexión, delega la creación de la sesión a SessionFactory.
 */
public class TCPListener {

    private final int puerto;
    private final ClientRegistry registro;
    private final SessionFactory sessionFactory;
    private final ExecutorService pool;

    public TCPListener(int puerto, ClientRegistry registro, SessionFactory sessionFactory) {
        this.puerto = puerto;
        this.registro = registro;
        this.sessionFactory = sessionFactory;
        this.pool = Executors.newCachedThreadPool();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor escuchando en puerto: " + puerto);

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Nueva conexión entrante desde: " + clienteSocket.getInetAddress().getHostAddress());

                // Crear ClientSession mediante SessionFactory
                ClientSession sesion = sessionFactory.create(clienteSocket);

                // Ejecutar la sesión en el pool de hilos
                pool.execute(sesion);
            }

        } catch (IOException e) {
            System.err.println("Error en TCPListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
