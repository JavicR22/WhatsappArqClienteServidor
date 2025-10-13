package org.example.server.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPListener {

    private final int puerto;
    private final ClientRegistry registro;
    private final SessionFactory sessionFactory;
    private ExecutorService pool;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public TCPListener(int puerto, ClientRegistry registro, SessionFactory sessionFactory) {
        this.puerto = puerto;
        this.registro = registro;
        this.sessionFactory = sessionFactory;
    }

    public void iniciar() {
        running = true;
        pool = Executors.newCachedThreadPool();

        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor escuchando en puerto: " + puerto);

            while (running) {
                try {
                    Socket clienteSocket = serverSocket.accept();
                    System.out.println("Nueva conexión entrante desde: " + clienteSocket.getInetAddress().getHostAddress());

                    ClientSession sesion = sessionFactory.create(clienteSocket);
                    pool.execute(sesion);
                } catch (SocketException se) {
                    // Si se cerró el serverSocket intencionalmente para detener el listener, salimos silenciosamente
                    if (!running) break;
                    throw se;
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("Error en TCPListener: " + e.getMessage());
                e.printStackTrace();
            } else {
                System.out.println("TCPListener detenido.");
            }
        } finally {
            shutdownPool();
            closeServerSocketQuietly();
        }
    }

    public void stop() {
        running = false;
        closeServerSocketQuietly();
        shutdownPool();
        System.out.println("Deteniendo TCPListener en puerto: " + puerto);
    }

    private void closeServerSocketQuietly() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignored) {}
    }

    private void shutdownPool() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }
}
