package org.example.objectPool;

import org.example.configuracioConexion.Connection;
import org.example.entidades.ConfiguracionConexiones;
import org.example.eventos.ConnectionObserver;
import org.example.eventos.MensajeriaDispatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pool de conexiones de usuarios.
 *
 * - Thread-safe: usa ConcurrentHashMap para concurrencia y synchronize
 *   en operaciones que necesitan consistencia entre varias comprobaciones.
 * - Notifica al dispatcher cuando un usuario se conecta/desconecta (si dispatcher != null).
 */
public class ConnectionPool {

    private final ConfiguracionConexiones config;
    private final MensajeriaDispatcher dispatcher;
    private final Map<String, Connection> conexionesActivas = new ConcurrentHashMap<>();
    private final List<ConnectionObserver> observadores = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param config     configuración (no nulo)
     * @param dispatcher dispatcher para notificaciones de usuario (puede ser nulo)
     */
    public ConnectionPool(ConfiguracionConexiones config, MensajeriaDispatcher dispatcher) {
        this.config = Objects.requireNonNull(config);
        this.dispatcher = dispatcher;
    }



    /**
     * Intenta agregar una nueva conexión para el usuario.
     *
     * Si el usuario ya está conectado, no reemplaza la conexión existente
     * y devuelve false (es responsabilidad del llamador cerrar la conexion que no se usó).
     *
     * @param usuario   nombre de usuario (no nulo)
     * @param conexion  conexión asociada (no nula)
     * @return true si la conexión se agregó; false si no (límite alcanzado o ya existe usuario)
     */
    public synchronized boolean agregarConexion(String usuario, Connection conexion) {
        Objects.requireNonNull(usuario, "usuario no puede ser null");
        Objects.requireNonNull(conexion, "conexion no puede ser null");

        // Verificar límite de conexiones
        if (config.esLimitado() && conexionesActivas.size() >= config.getMaxUsuariosConectados()) {
            System.out.println("⚠️ Límite de usuarios alcanzado. No se puede conectar: " + usuario);
            return false;
        }

        // Intentar insertar sin reemplazar una conexión existente
        Connection prev = conexionesActivas.putIfAbsent(usuario, conexion);
        if (prev != null) {
            System.out.println("⚠️ Usuario ya conectado: " + usuario + ". Rechazando nueva conexión.");
            return false;
        }

        System.out.println("✅ Usuario conectado: " + usuario);

        // Notificar (si hay dispatcher)
        if (dispatcher != null) {
            try {
                dispatcher.notificarUsuarioConectado(usuario);
            } catch (Exception e) {
                // Nunca dejar que una excepción del dispatcher rompa la pool
                System.err.println("Error notificando conexión para " + usuario + ": " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * Remueve la conexión del usuario (si existe).
     *
     * @param usuario nombre de usuario (no nulo)
     * @return true si había una conexión y fue removida; false si no existía
     */
    public synchronized boolean removerConexion(String usuario) {
        Objects.requireNonNull(usuario, "usuario no puede ser null");

        Connection conexion = conexionesActivas.remove(usuario);
        if (conexion == null) {
            // No existía
            return false;
        }

        try {
            conexion.cerrar();
        } catch (Exception e) {
            // Registrar pero continuar
            System.err.println("Error cerrando conexión de " + usuario + ": " + e.getMessage());
        }

        System.out.println("❎ Usuario desconectado: " + usuario);

        // Notificar (si hay dispatcher)
        if (dispatcher != null) {
            try {
                dispatcher.notificarUsuarioDesconectado(usuario);
            } catch (Exception e) {
                System.err.println("Error notificando desconexión para " + usuario + ": " + e.getMessage());
            }
        }

        return true;
    }

    /**
     * Devuelve la configuración usada por el pool.
     */
    public ConfiguracionConexiones getConfig() {
        return config;
    }

    /**
     * Número aproximado de conexiones activas (operación rápida).
     */
    public int getNumeroConexiones() {
        return conexionesActivas.size();
    }

    /**
     * Indica si el usuario está conectado.
     *
     * @param usuario nombre del usuario
     */
    public boolean estaConectado(String usuario) {
        Objects.requireNonNull(usuario, "usuario no puede ser null");
        return conexionesActivas.containsKey(usuario);
    }

    /**
     * Lista inmutable (snapshot) de usuarios conectados en este momento.
     * Se devuelve una copia para evitar vistas cambiantes.
     */
    public Collection<String> listarUsuariosConectados() {
        return Collections.unmodifiableCollection(new HashMap<>(conexionesActivas).keySet());
    }

    /**
     * Obtiene la conexión asociada al usuario (o null si no existe).
     * Devuelve la referencia directa a la Connection almacenada.
     *
     * Nota: la conexión puede cerrarse en cualquier momento por otro hilo.
     */
    public Connection obtenerConexion(String usuario) {
        Objects.requireNonNull(usuario, "usuario no puede ser null");
        return conexionesActivas.get(usuario);
    }

    /**
     * Devuelve un snapshot inmutable del mapa usuario->conexión.
     * Esto evita que el llamador vea una vista mutable compartida.
     */
    public Map<String, Connection> getConexionesActivas() {
        return Collections.unmodifiableMap(new HashMap<>(conexionesActivas));
    }

    /**
     * Cierra todas las conexiones y limpia el pool.
     * Tras esto, el pool queda vacío.
     */
    public synchronized void stop() {
        // Cerrar todas las conexiones (intentar cerrar cada una, ignorando errores)
        for (Map.Entry<String, Connection> e : conexionesActivas.entrySet()) {
            try {
                e.getValue().cerrar();
            } catch (Exception ex) {
                System.err.println("Error cerrando conexión de " + e.getKey() + ": " + ex.getMessage());
            }
        }
        conexionesActivas.clear();
        System.out.println("⛔ ConnectionPool detenido, todas las conexiones cerradas.");
    }
}
