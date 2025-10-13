package org.example.objectPool;

import org.example.configuracioConexion.Connection;
import org.example.entidades.ConfiguracionConexiones;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPool {
    private final ConfiguracionConexiones config;
    private final Map<String, Connection> conexionesActivas = new ConcurrentHashMap<>();

    public ConnectionPool(ConfiguracionConexiones config) {
        this.config = config;
    }

    public synchronized boolean agregarConexion(String usuario, Connection conexion) {
        if (config.esLimitado() && conexionesActivas.size() >= config.getMaxUsuariosConectados()) {
            System.out.println("⚠️ Límite de usuarios alcanzado. No se puede conectar: " + usuario);
            return false;
        }
        conexionesActivas.put(usuario, conexion);
        System.out.println("✅ Usuario conectado: " + usuario);
        return true;
    }

    public synchronized void removerConexion(String usuario) {
        Connection conexion = conexionesActivas.remove(usuario);
        if (conexion != null) {
            conexion.cerrar();
            System.out.println("❎ Usuario desconectado: " + usuario);
        }
    }

    public int getNumeroConexiones() {
        return conexionesActivas.size();
    }

    public boolean estaConectado(String usuario) {
        return conexionesActivas.containsKey(usuario);
    }
}
