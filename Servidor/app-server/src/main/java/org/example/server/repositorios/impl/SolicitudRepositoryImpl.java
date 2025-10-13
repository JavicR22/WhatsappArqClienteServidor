package org.example.server.repositorios.impl;

import org.example.common.entidades.Solicitud;
import org.example.common.entidades.Usuario;
import org.example.common.repositorios.SolicitudRepository;
import org.example.server.db.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SolicitudRepositoryImpl implements SolicitudRepository {

    private final DBConnectionPool pool;

    public SolicitudRepositoryImpl(DBConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public void guardar(Solicitud solicitud) {
        String sql = "INSERT INTO solicitudes (id, emisor_id, receptor_id, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, solicitud.getId());
            stmt.setString(2, solicitud.getEmisor().getId());
            stmt.setString(3, solicitud.getReceptor().getId());
            stmt.setString(4, solicitud.getEstado());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Solicitud> buscarPorId(String id) {
        String sql = "SELECT * FROM solicitudes WHERE id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Solicitud s = new Solicitud(
                        rs.getString("id"),
                        new Usuario(rs.getString("emisor_id"), null, null,null, null),
                        new Usuario(rs.getString("receptor_id"), null, null,null, null),
                        rs.getString("estado")
                );
                return Optional.of(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Solicitud> listarPorReceptor(String receptorId) {
        List<Solicitud> solicitudes = new ArrayList<>();
        String sql = "SELECT * FROM solicitudes WHERE receptor_id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, receptorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                solicitudes.add(new Solicitud(
                        rs.getString("id"),
                        new Usuario(rs.getString("emisor_id"), null, null, null, null),
                        new Usuario(rs.getString("receptor_id"), null, null,null, null),
                        rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return solicitudes;
    }

    @Override
    public void actualizarEstado(String id, String nuevoEstado) {
        String sql = "UPDATE solicitudes SET estado = ? WHERE id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
