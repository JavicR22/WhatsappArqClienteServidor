package org.example.configuracioConexion;

import org.example.objectPool.ConnectionPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClientHandler implements Runnable {
    private final ConnectionPool pool;
    private final Connection conexion;

    public ClientHandler(ConnectionPool pool, Connection conexion) {
        this.pool = pool;
        this.conexion = conexion;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(conexion.getSocket().getInputStream()));
             PrintWriter output = new PrintWriter(conexion.getSocket().getOutputStream(), true)) {

            output.println("Conectado al servidor. Escribe 'exit' para salir.");

            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                if ("exit".equalsIgnoreCase(mensaje.trim())) {
                    break;
                }
                System.out.println("[" + conexion.getUsuario() + "]: " + mensaje);
            }

        } catch (IOException e) {
            System.err.println("Error con el cliente " + conexion.getUsuario() + ": " + e.getMessage());
        } finally {
            pool.removerConexion(conexion.getUsuario());
        }
    }

}
