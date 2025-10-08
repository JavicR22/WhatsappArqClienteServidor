package org.example.client.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

public class PoolConexionesH2 {

    private final String url;
    private final int maxConexiones;
    private final Deque<Connection> pool = new ArrayDeque<>();

    public PoolConexionesH2(String url, int maxConexiones) {
        this.url = url;
        this.maxConexiones = maxConexiones;
    }

    public synchronized Connection obtenerConexion() throws SQLException {
        if (!pool.isEmpty()) {
            return pool.pop();
        }
        if (pool.size() < maxConexiones) {
            return DriverManager.getConnection(url);
        }
        throw new SQLException("Límite máximo de conexiones alcanzado (H2).");
    }

    public synchronized void liberarConexion(Connection connection) {
        if (connection != null) {
            pool.push(connection);
        }
    }

    public synchronized void cerrarTodas() {
        while (!pool.isEmpty()) {
            try {
                pool.pop().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
