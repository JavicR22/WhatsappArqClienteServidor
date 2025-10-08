package org.example.server.repositorios.impl;

import org.example.common.entidades.Canal;
import org.example.common.entidades.Usuario;
import org.example.common.repositorios.CanalRepository;
import org.example.server.db.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CanalRepositoryImpl implements CanalRepository {

    private final DBConnectionPool pool;

    public CanalRepositoryImpl(DBConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public void guardar(Canal canal) {
        String sql = "INSERT INTO canales (id, nombre) VALUES (?, ?)";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, canal.getId());
            stmt.setString(2, canal.getNombre());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insertar miembros (tabla intermedia canal_miembros)
        if (canal.getMiembros() != null) {
            for (Usuario usuario : canal.getMiembros()) {
                agregarMiembro(canal.getId(), usuario.getId());
            }
        }
    }

    @Override
    public Optional<Canal> buscarPorId(String id) {
        String sql = "SELECT * FROM canales WHERE id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Canal canal = new Canal(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        listarMiembros(id)
                );
                return Optional.of(canal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Canal> listarTodos() {
        List<Canal> lista = new ArrayList<>();
        String sql = "SELECT * FROM canales";
        try (Connection conn = pool.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                lista.add(new Canal(id, rs.getString("nombre"), listarMiembros(id)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM canales WHERE id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void agregarMiembro(String canalId, String usuarioId) {
        String sql = "INSERT INTO canal_miembros (canal_id, usuario_id) VALUES (?, ?)";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, canalId);
            stmt.setString(2, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void eliminarMiembro(String canalId, String usuarioId) {
        String sql = "DELETE FROM canal_miembros WHERE canal_id = ? AND usuario_id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, canalId);
            stmt.setString(2, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Usuario> listarMiembros(String canalId) {
        List<Usuario> miembros = new ArrayList<>();
        String sql = "SELECT u.id, u.nombre, u.correo, u.contrasena_hash " +
                "FROM usuarios u JOIN canal_miembros cm ON u.id = cm.usuario_id WHERE cm.canal_id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, canalId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                miembros.add(new Usuario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("contrasena_hash"),
                        null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return miembros;
    }
}
