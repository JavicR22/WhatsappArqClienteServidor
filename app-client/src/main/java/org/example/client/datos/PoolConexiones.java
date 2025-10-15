package org.example.client.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * CAPA DE ACCESO DE DATOS
 * Pool de conexiones para la base de datos H2.
 * Optimiza el uso de recursos reutilizando conexiones.
 */
public class PoolConexiones {

    private final String url;
    private final int maxConexiones;
    private final Deque<Connection> pool = new ArrayDeque<>();

    public PoolConexiones(String url, int maxConexiones) {
        this.url = url;
        this.maxConexiones = maxConexiones;
    }

    /**
     * Obtiene una conexión del pool o crea una nueva si es necesario
     */
    public synchronized Connection obtenerConexion() throws SQLException {
        if (!pool.isEmpty()) {
            return pool.pop();
        }
        if (pool.size() < maxConexiones) {
            return DriverManager.getConnection(url);
        }
        throw new SQLException("Límite máximo de conexiones alcanzado (H2).");
    }

    /**
     * Devuelve una conexión al pool para su reutilización
     */
    public synchronized void liberarConexion(Connection connection) {
        if (connection != null) {
            pool.push(connection);
        }
    }

    /**
     * Cierra todas las conexiones del pool
     */
    public synchronized void cerrarTodas() {
        while (!pool.isEmpty()) {
            try {
                pool.pop().close();
            } catch (SQLException e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}
