package org.example.repositorio.implementacion;

import org.example.entidades.MensajeTextoPrivado;
import org.example.persistencia.adaptador.AdaptadorBaseDatos;
import org.example.persistencia.fabrica.FabricaBaseDatos;
import org.example.repositorio.MensajeTextoPrivadoRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MensajeTextoPrivadoRepositorioImpl implements MensajeTextoPrivadoRepositorio {
    private final AdaptadorBaseDatos adaptadorBaseDatos = FabricaBaseDatos.getAdapter();

    @Override
    public void guardar(MensajeTextoPrivado mensaje) {
        // La tabla tiene: id_mensaje, emisor, receptor, contenido_texto, fecha_envio
        String sql = "INSERT INTO mensaje_texto_privado (emisor, receptor, contenido_texto, fecha_envio) VALUES (?, ?, ?, ?)";

        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mensaje.getEmisor().getUsername());
            ps.setString(2, mensaje.getReceptor().getUsername());
            ps.setString(3, mensaje.getContenidoTexto());
            // Usamos LocalDateTime del objeto para obtener la hora de envío
            ps.setTimestamp(4, Timestamp.valueOf(mensaje.getFechaEnvio()));

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar MensajeTextoPrivado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
