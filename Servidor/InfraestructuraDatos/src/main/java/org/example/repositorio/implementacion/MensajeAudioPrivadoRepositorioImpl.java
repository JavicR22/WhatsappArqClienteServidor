package org.example.repositorio.implementacion;

import org.example.entidades.MensajeAudioPrivado;
import org.example.persistencia.adaptador.AdaptadorBaseDatos;
import org.example.persistencia.fabrica.FabricaBaseDatos;
import org.example.repositorio.MensajeAudioPrivadoRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MensajeAudioPrivadoRepositorioImpl implements MensajeAudioPrivadoRepositorio {
    private final AdaptadorBaseDatos adaptadorBaseDatos = FabricaBaseDatos.getAdapter();

    @Override
    public void guardar(MensajeAudioPrivado mensaje) {
        // La tabla tiene: id_mensaje, emisor, receptor, ruta_audio, contenido_texto, fecha_envio
        String sql = "INSERT INTO mensaje_audio_privado (emisor, receptor, ruta_audio, contenido_texto, fecha_envio) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mensaje.getEmisor().getUsername());
            ps.setString(2, mensaje.getReceptor().getUsername());
            ps.setString(3, mensaje.getRutaAudio());
            ps.setString(4, mensaje.getContenidoTexto());
            ps.setTimestamp(5, Timestamp.valueOf(mensaje.getFechaEnvio()));

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar MensajeAudioPrivado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}