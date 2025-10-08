package org.example.server.repositorios.impl;

import org.example.common.entidades.Mensaje;
import org.example.common.entidades.MensajeAudio;
import org.example.common.entidades.MensajeTexto;
import org.example.common.entidades.Usuario;
import org.example.common.repositorios.MensajeRepository;
import org.example.server.db.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MensajeRepositoryImpl implements MensajeRepository {

    private final DBConnectionPool pool;

    public MensajeRepositoryImpl(DBConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public void guardar(Mensaje mensaje) {
        String sql = "INSERT INTO mensajes (id, remitente_id, tipo, contenido, fecha_hora) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mensaje.getId());
            stmt.setString(2, mensaje.getRemitente().getId());
            stmt.setString(3, mensaje instanceof MensajeTexto ? "TEXTO" : "AUDIO");

            if (mensaje instanceof MensajeTexto texto) {
                stmt.setString(4, texto.getContenido());
            } else if (mensaje instanceof MensajeAudio audio) {
                stmt.setString(4, audio.getRutaArchivo());
            }

            stmt.setTimestamp(5, Timestamp.valueOf(mensaje.getFechaHora()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Mensaje> listarPorUsuario(String usuarioId) {
        String sql = "SELECT * FROM mensajes WHERE remitente_id = ? ORDER BY fecha_hora DESC";
        return obtenerMensajes(sql, usuarioId);
    }

    @Override
    public List<Mensaje> listarPorCanal(String canalId) {
        String sql = "SELECT * FROM mensajes WHERE canal_id = ? ORDER BY fecha_hora DESC";
        return obtenerMensajes(sql, canalId);
    }

    private List<Mensaje> obtenerMensajes(String sql, String parametro) {
        List<Mensaje> mensajes = new ArrayList<>();
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, parametro);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Usuario remitente = new Usuario(rs.getString("remitente_id"),null ,  null, null, null);
                Mensaje m;
                if ("TEXTO".equalsIgnoreCase(rs.getString("tipo"))) {
                    m = new MensajeTexto(rs.getString("id"), remitente, rs.getString("contenido"));
                } else {
                    m = new MensajeAudio(rs.getString("id"), remitente, rs.getString("contenido"));
                }
                mensajes.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mensajes;
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM mensajes WHERE id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
