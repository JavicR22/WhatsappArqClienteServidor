package org.example.repositorio.implementacion;

import org.example.entidades.MensajeAudioCanal;
import org.example.persistencia.adaptador.AdaptadorBaseDatos;
import org.example.persistencia.fabrica.FabricaBaseDatos;
import org.example.repositorio.MensajeAudioCanalRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MensajeAudioCanalRepositorioImpl implements MensajeAudioCanalRepositorio {
    private final AdaptadorBaseDatos adaptadorBaseDatos = FabricaBaseDatos.getAdapter();

    @Override
    public void guardar(MensajeAudioCanal mensaje) {
        // La tabla tiene: id_mensaje, emisor, id_canal, ruta_audio, contenido, fecha_envio
        String sql = "INSERT INTO mensaje_audio_canal (emisor, id_canal, ruta_audio, contenido, fecha_envio) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = adaptadorBaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mensaje.getEmisor().getUsername());
            // Asumiendo que el objeto Canal tiene un método getIdCanal()
            ps.setInt(2, mensaje.getCanal().getIdCanal());
            ps.setString(3, mensaje.getRutaAudio());
            // Asumo que 'contenido' es para la transcripción o texto asociado al audio
            ps.setString(4, mensaje.getContenido());
            ps.setTimestamp(5, Timestamp.valueOf(mensaje.getFechaEnvio()));

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar MensajeAudioCanal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
