package org.example.client.datos;

import org.example.client.modelo.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CAPA DE ACCESO DE DATOS
 * Maneja la persistencia local en la base de datos H2.
 * Proporciona operaciones CRUD para entidades del dominio.
 */
public class RepositorioLocal {

    private final PoolConexiones poolConexiones;

    public RepositorioLocal(PoolConexiones poolConexiones) {
        this.poolConexiones = poolConexiones;
    }

    /**
     * Crea las tablas necesarias si no existen
     */
    public void inicializarTablas() {
        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            Statement stmt = conn.createStatement();

            // Tabla de usuarios
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS usuarios (" +
                            "id VARCHAR(255) PRIMARY KEY, " +
                            "nombre VARCHAR(255) NOT NULL, " +
                            "correo VARCHAR(255) UNIQUE NOT NULL" +
                            ")"
            );

            // Tabla de canales
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS canales (" +
                            "id VARCHAR(255) PRIMARY KEY, " +
                            "nombre VARCHAR(255) NOT NULL, " +
                            "descripcion TEXT, " +
                            "privado BOOLEAN NOT NULL, " +
                            "creador_email VARCHAR(255) NOT NULL, " +
                            "creado_en BIGINT NOT NULL" +
                            ")"
            );

            // Tabla de miembros de canales
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS canal_miembros (" +
                            "id_canal VARCHAR(255) NOT NULL, " +
                            "correo_usuario VARCHAR(255) NOT NULL, " +
                            "PRIMARY KEY (id_canal, correo_usuario), " +
                            "FOREIGN KEY (id_canal) REFERENCES canales(id) ON DELETE CASCADE" +
                            ")"
            );

            // Tabla de solicitudes
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS solicitudes (" +
                            "id VARCHAR(255) PRIMARY KEY, " +
                            "id_usuario VARCHAR(255) NOT NULL, " +
                            "id_canal VARCHAR(255) NOT NULL, " +
                            "fecha_solicitud TIMESTAMP NOT NULL, " +
                            "estado VARCHAR(50) NOT NULL" +
                            ")"
            );

            // Tabla de mensajes
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS mensajes (" +
                            "id VARCHAR(255) PRIMARY KEY, " +
                            "remitente_correo VARCHAR(255) NOT NULL, " +
                            "destinatario_correo VARCHAR(255), " +
                            "id_canal VARCHAR(255), " +
                            "contenido TEXT NOT NULL, " +
                            "tipo VARCHAR(50) NOT NULL, " +
                            "fecha_hora TIMESTAMP NOT NULL" +
                            ")"
            );

            System.out.println("✅ Tablas de base de datos inicializadas correctamente");

        } catch (SQLException e) {
            System.err.println("❌ Error inicializando tablas: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Guarda un usuario en la base de datos local
     */
    public boolean guardarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id, nombre, correo) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario.getId());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getCorreo());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error guardando usuario: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Busca un usuario por su correo electrónico
     */
    public Optional<Usuario> buscarUsuarioPorCorreo(String correo) {
        String sql = "SELECT id, nombre, correo FROM usuarios WHERE correo = ?";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, correo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        null,
                        null
                );
                return Optional.of(usuario);
            }

            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("Error buscando usuario: " + e.getMessage());
            return Optional.empty();
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Elimina un usuario de la base de datos local
     */
    public boolean eliminarUsuario(String usuarioId) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuarioId);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error eliminando usuario: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Guarda un canal en la base de datos local
     */
    public boolean guardarCanal(Canal canal) {
        String sql = "INSERT INTO canales (id, nombre, descripcion, privado, creador_email, creado_en) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, canal.getId());
            stmt.setString(2, canal.getNombre());
            stmt.setString(3, canal.getDescripcion());
            stmt.setBoolean(4, canal.isPrivado());
            stmt.setString(5, canal.getCreadorEmail());
            stmt.setLong(6, canal.getCreadoEn());

            int filasAfectadas = stmt.executeUpdate();

            // Guardar miembros del canal
            if (filasAfectadas > 0) {
                for (String miembro : canal.getMiembros()) {
                    agregarMiembroCanal(canal.getId(), miembro);
                }
            }

            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error guardando canal: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Obtiene todos los canales del usuario actual
     */
    public List<Canal> obtenerCanalesDelUsuario(String correoUsuario) {
        String sql = "SELECT DISTINCT c.id, c.nombre, c.descripcion, c.privado, c.creador_email, c.creado_en " +
                "FROM canales c " +
                "INNER JOIN canal_miembros cm ON c.id = cm.id_canal " +
                "WHERE cm.correo_usuario = ?";

        List<Canal> canales = new ArrayList<>();
        Connection conn = null;

        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, correoUsuario);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String idCanal = rs.getString("id");
                List<String> miembros = obtenerMiembrosCanal(idCanal);
                Canal canal = new Canal(
                        idCanal,
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBoolean("privado"),
                        rs.getString("creador_email"),
                        miembros,
                        rs.getLong("creado_en")
                );
                canales.add(canal);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo canales: " + e.getMessage());
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }

        return canales;
    }

    /**
     * Agrega un miembro a un canal
     */
    public boolean agregarMiembroCanal(String idCanal, String correoUsuario) {
        String sql = "INSERT INTO canal_miembros (id_canal, correo_usuario) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idCanal);
            stmt.setString(2, correoUsuario);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error agregando miembro al canal: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Obtiene los miembros de un canal
     */
    public List<String> obtenerMiembrosCanal(String idCanal) {
        String sql = "SELECT correo_usuario FROM canal_miembros WHERE id_canal = ?";
        List<String> miembros = new ArrayList<>();

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idCanal);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                miembros.add(rs.getString("correo_usuario"));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo miembros del canal: " + e.getMessage());
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }

        return miembros;
    }

    /**
     * Guarda una solicitud de invitación
     */
    public boolean guardarSolicitud(Solicitud solicitud) {
        String sql = "INSERT INTO solicitudes (id, id_usuario, id_canal, fecha_solicitud, estado) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, solicitud.getId());
            stmt.setString(2, solicitud.getIdUsuario());
            stmt.setString(3, solicitud.getIdCanal());
            stmt.setObject(4, solicitud.getFechaSolicitud());
            stmt.setString(5, solicitud.getEstado());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error guardando solicitud: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Obtiene las solicitudes pendientes de un usuario
     */
    public List<Solicitud> obtenerSolicitudesPendientes(String correoUsuario) {
        String sql = "SELECT id, id_usuario, id_canal, fecha_solicitud, estado " +
                "FROM solicitudes WHERE id_usuario = ? AND estado = 'PENDIENTE'";

        List<Solicitud> solicitudes = new ArrayList<>();
        Connection conn = null;

        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, correoUsuario);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Solicitud solicitud = new Solicitud(
                        rs.getString("id"),
                        rs.getString("id_usuario"),
                        rs.getString("id_canal")
                );
                solicitud.setEstado(rs.getString("estado"));
                solicitudes.add(solicitud);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo solicitudes: " + e.getMessage());
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }

        return solicitudes;
    }

    /**
     * Actualiza el estado de una solicitud
     */
    public boolean actualizarEstadoSolicitud(String idSolicitud, String nuevoEstado) {
        String sql = "UPDATE solicitudes SET estado = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nuevoEstado);
            stmt.setString(2, idSolicitud);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando solicitud: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Busca un canal por su ID
     */
    public Optional<Canal> buscarCanalPorId(String idCanal) {
        String sql = "SELECT id, nombre, descripcion, privado, creador_email, creado_en FROM canales WHERE id = ?";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idCanal);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                List<String> miembros = obtenerMiembrosCanal(idCanal);
                Canal canal = new Canal(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBoolean("privado"),
                        rs.getString("creador_email"),
                        miembros,
                        rs.getLong("creado_en")
                );
                return Optional.of(canal);
            }

            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("Error buscando canal: " + e.getMessage());
            return Optional.empty();
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Guarda un mensaje en la base de datos local
     */
    public boolean guardarMensaje(String id, String remitenteCorreo, String destinatarioCorreo,
                                  String idCanal, String contenido, String tipo) {
        String sql = "INSERT INTO mensajes (id, remitente_correo, destinatario_correo, id_canal, contenido, tipo, fecha_hora) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP())";

        Connection conn = null;
        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setString(2, remitenteCorreo);
            stmt.setString(3, destinatarioCorreo);
            stmt.setString(4, idCanal);
            stmt.setString(5, contenido);
            stmt.setString(6, tipo);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error guardando mensaje: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }
    }

    /**
     * Obtiene los mensajes de una conversación con un usuario
     */
    public List<MensajeTexto> obtenerMensajesConUsuario(String correoUsuario1, String correoUsuario2) {
        String sql = "SELECT id, remitente_correo, contenido, fecha_hora FROM mensajes " +
                "WHERE (remitente_correo = ? AND destinatario_correo = ?) " +
                "OR (remitente_correo = ? AND destinatario_correo = ?) " +
                "ORDER BY fecha_hora ASC";

        List<MensajeTexto> mensajes = new ArrayList<>();
        Connection conn = null;

        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, correoUsuario1);
            stmt.setString(2, correoUsuario2);
            stmt.setString(3, correoUsuario2);
            stmt.setString(4, correoUsuario1);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String remitenteCorreo = rs.getString("remitente_correo");
                Usuario remitente = new Usuario(null, remitenteCorreo, remitenteCorreo, null, null);

                MensajeTexto mensaje = new MensajeTexto(
                        rs.getString("id"),
                        remitente,
                        rs.getString("contenido")
                );
                mensajes.add(mensaje);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo mensajes: " + e.getMessage());
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }

        return mensajes;
    }

    /**
     * Obtiene los mensajes de un canal
     */
    public List<MensajeTexto> obtenerMensajesDeCanal(String idCanal) {
        String sql = "SELECT id, remitente_correo, contenido, fecha_hora FROM mensajes " +
                "WHERE id_canal = ? " +
                "ORDER BY fecha_hora ASC";

        List<MensajeTexto> mensajes = new ArrayList<>();
        Connection conn = null;

        try {
            conn = poolConexiones.obtenerConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idCanal);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String remitenteCorreo = rs.getString("remitente_correo");
                Usuario remitente = new Usuario(null, remitenteCorreo, remitenteCorreo, null, null);

                MensajeTexto mensaje = new MensajeTexto(
                        rs.getString("id"),
                        remitente,
                        rs.getString("contenido")
                );
                mensajes.add(mensaje);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo mensajes del canal: " + e.getMessage());
        } finally {
            if (conn != null) {
                poolConexiones.liberarConexion(conn);
            }
        }

        return mensajes;
    }
}
