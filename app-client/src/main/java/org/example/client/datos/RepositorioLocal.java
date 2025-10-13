package org.example.client.datos;

import org.example.client.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
