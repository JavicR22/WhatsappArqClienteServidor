package org.example.server.repositorios.impl;

import org.example.common.entidades.Usuario;
import org.example.common.repositorios.UsuarioRepository;
import org.example.server.db.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final DBConnectionPool pool;

    public UsuarioRepositoryImpl(DBConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (id, nombre, correo, contrasena, salt) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getId());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getCorreo());
            stmt.setString(4, usuario.getContrasena());
            stmt.setString(5, usuario.getSalt());

            stmt.executeUpdate();
            System.out.println("✅ Usuario guardado correctamente: " + usuario.getCorreo());

        } catch (SQLException e) {
            System.err.println("❌ Error al guardar usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("contrasena"),
                        rs.getString("salt")
                );
                return Optional.of(u);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al buscar usuario por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        String sql = "SELECT * FROM usuario WHERE correo = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("contrasena"),
                        rs.getString("salt")
                );
                return Optional.of(u);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al buscar usuario por correo: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (Connection conn = pool.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("contrasena"),
                        rs.getString("salt")
                ));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al listar usuarios: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = pool.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Usuario eliminado: " + id);

        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar usuario: " + e.getMessage());
        }
    }
}
