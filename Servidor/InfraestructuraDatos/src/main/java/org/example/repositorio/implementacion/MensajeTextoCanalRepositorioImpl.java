package org.example.repositorio.implementacion;

import org.example.entidades.MensajeTextoCanal;
import org.example.persistencia.adaptador.AdaptadorBaseDatos;
import org.example.persistencia.fabrica.FabricaBaseDatos;
import org.example.repositorio.MensajeTextoCanalRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MensajeTextoCanalRepositorioImpl implements MensajeTextoCanalRepositorio {
    private final AdaptadorBaseDatos adaptadorBaseDatos = FabricaBaseDatos.getAdapter();

    @Override
    public void guardar(MensajeTextoCanal mensaje) {
        // La tabla tiene: id_mensaje, emisor, id_canal, contenido, fecha_envio
        String sql = "INSERT INTO mensaje_texto_canal (emisor, id_canal, contenido, fecha_envio) VALUES (?, ?, ?, ?)";

        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mensaje.getEmisor().getUsername());
            // Asumiendo que el objeto Canal tiene un método getIdCanal()
          //  ps.setInt(2, mensaje.getCanal().getNombreCanal());
            ps.setString(3, mensaje.getContenido());
            ps.setTimestamp(4, Timestamp.valueOf(mensaje.getFechaEnvio()));

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar MensajeTextoCanal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
