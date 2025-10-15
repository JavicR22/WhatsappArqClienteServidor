package org.example.repositorio.implementacion;

import org.example.entidades.Usuario;
import org.example.persistencia.adaptador.AdaptadorBaseDatos;
import org.example.persistencia.fabrica.FabricaBaseDatos;
import org.example.repositorio.UsuarioRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositorioImpl implements UsuarioRepositorio {
    private final AdaptadorBaseDatos adaptadorBaseDatos = FabricaBaseDatos.getAdapter();
    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (username, email, password, ruta_foto, direccion_ip) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getRutaFoto());
            ps.setString(5, usuario.getDireccionIP());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("ruta_foto"),
                        rs.getString("direccion_ip")
                );
                return Optional.of(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("ruta_foto"),
                        rs.getString("direccion_ip")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public void eliminarPorUsername(String username) {
        String sql = "DELETE FROM usuario WHERE username = ?";
        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
