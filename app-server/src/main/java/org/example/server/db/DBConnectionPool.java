package org.example.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

public class DBConnectionPool {

    private final String url;
    private final String user;
    private final String password;
    private final int maxConexiones;

    private final Deque<Connection> pool = new ArrayDeque<>();

    public DBConnectionPool(String url, String user, String password, int maxConexiones) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxConexiones = maxConexiones;
    }

    public synchronized Connection obtenerConexion() throws SQLException {
        if (!pool.isEmpty()) {
            return pool.pop();
        }
        if (pool.size() < maxConexiones) {
            return DriverManager.getConnection(url, user, password);
        }
        throw new SQLException("Límite máximo de conexiones alcanzado.");
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
