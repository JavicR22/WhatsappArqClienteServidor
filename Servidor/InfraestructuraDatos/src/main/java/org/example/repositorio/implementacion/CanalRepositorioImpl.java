package org.example.repositorio.implementacion;

import org.example.entidades.Canal;
import org.example.persistencia.adaptador.AdaptadorBaseDatos;
import org.example.persistencia.fabrica.FabricaBaseDatos;
import org.example.repositorio.CanalRepositorio;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CanalRepositorioImpl implements CanalRepositorio {
    private final AdaptadorBaseDatos adaptadorBaseDatos = FabricaBaseDatos.getAdapter();

    // Método auxiliar para mapear el ResultSet a la entidad Canal

    private Canal mapearCanal(ResultSet rs) throws SQLException {
        return new Canal(
                rs.getInt("id_canal"),
                rs.getString("nombre_canal"),
                rs.getTimestamp("fecha_creacion").toLocalDateTime(),
                rs.getString("propietario"),
                rs.getString("descripcion"),
                rs.getBoolean("privado"));
    }

    @Override
    public void guardar(Canal canal) {
        // Solo insertamos el nombre, la DB se encarga del ID y fecha_creacion
        String sql = "INSERT INTO canal (nombre_canal, fecha_creacion, propietario, privado, descripcion) VALUES (?,?,?,?,?)";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             // Solicitamos las claves generadas (el id_canal)
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, canal.getNombreCanal());
            ps.setTimestamp(2,Timestamp.valueOf(canal.getFechaCreacion()));
            ps.setString(3,canal.getUsernameCreador());
            ps.setBoolean(4,canal.getPrivado());
            ps.setString(5,canal.getDescripcion());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        // Asignamos el ID generado de vuelta al objeto Canal
                        canal.setIdCanal(rs.getInt(1));
                        // Nota: La fecha de creación también se podría recuperar si se necesita.
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar Canal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Canal> buscarPorId(int idCanal) {
        String sql = "SELECT id_canal, nombre_canal, fecha_creacion FROM canal WHERE id_canal = ?";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCanal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCanal(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Canal> buscarPorNombre(String nombreCanal) {
        String sql = "SELECT id_canal, nombre_canal, fecha_creacion, propietario,privado, descripcion FROM canal WHERE nombre_canal = ?";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreCanal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCanal(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Canal> listarTodos() {
        List<Canal> lista = new ArrayList<>();
        String sql = "SELECT id_canal, nombre_canal, fecha_creacion FROM canal";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearCanal(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}