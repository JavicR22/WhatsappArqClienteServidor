package org.example.client.ui;

import org.example.client.comunicacion.ReproductorAudio;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Panel para reproducir mensajes de audio con controles de play, pause y restart
 */
public class AudioPlayerPanel extends JPanel {
    private ReproductorAudio reproductor;
    private JButton btnPlay;
    private JButton btnPause;
    private JButton btnRestart;
    private JLabel lblDuracion;
    private JProgressBar progressBar;
    private File archivoAudio;
    private long duracionSegundos;
    private Timer timerProgreso;
    private boolean reproduciendo = false;

    public AudioPlayerPanel(File archivoAudio, long duracionSegundos) {
        this.archivoAudio = archivoAudio;
        this.duracionSegundos = duracionSegundos;
        this.reproductor = new ReproductorAudio();

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        setBackground(new Color(240, 248, 255));
        setPreferredSize(new Dimension(250, 60));

        // Panel de información
        JPanel panelInfo = new JPanel(new BorderLayout(5, 2));
        panelInfo.setOpaque(false);

        JLabel lblIcono = new JLabel("🎵");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        lblDuracion = new JLabel(formatearDuracion(duracionSegundos));
        lblDuracion.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblDuracion.setForeground(new Color(100, 100, 100));

        progressBar = new JProgressBar(0, (int) duracionSegundos);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(150, 8));

        panelInfo.add(lblIcono, BorderLayout.WEST);
        panelInfo.add(progressBar, BorderLayout.CENTER);
        panelInfo.add(lblDuracion, BorderLayout.EAST);

        add(panelInfo, BorderLayout.NORTH);

        // Panel de controles
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panelControles.setOpaque(false);

        btnPlay = new JButton("▶");
        btnPlay.setPreferredSize(new Dimension(40, 30));
        btnPlay.setFocusPainted(false);

        btnPause = new JButton("⏸");
        btnPause.setPreferredSize(new Dimension(40, 30));
        btnPause.setEnabled(false);
        btnPause.setFocusPainted(false);

        btnRestart = new JButton("⏮");
        btnRestart.setPreferredSize(new Dimension(40, 30));
        btnRestart.setFocusPainted(false);

        // Listeners
        btnPlay.addActionListener(e -> reproducir());
        btnPause.addActionListener(e -> pausar());
        btnRestart.addActionListener(e -> reiniciar());

        panelControles.add(btnRestart);
        panelControles.add(btnPlay);
        panelControles.add(btnPause);

        add(panelControles, BorderLayout.CENTER);

        // Timer para actualizar progreso
        timerProgreso = new Timer(100, e -> actualizarProgreso());

        // Cargar audio
        cargarAudio();
    }

    private void cargarAudio() {
        try {
            reproductor.cargar(archivoAudio);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar audio: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reproducir() {
        try {
            reproductor.reproducir();
            reproduciendo = true;

            btnPlay.setEnabled(false);
            btnPause.setEnabled(true);

            timerProgreso.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al reproducir audio: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pausar() {
        reproductor.pausar();
        reproduciendo = false;

        btnPlay.setEnabled(true);
        btnPause.setEnabled(false);

        timerProgreso.stop();
    }

    private void reiniciar() {
        reproductor.detener();
        reproduciendo = false;

        btnPlay.setEnabled(true);
        btnPause.setEnabled(false);

        progressBar.setValue(0);
        lblDuracion.setText(formatearDuracion(duracionSegundos));

        timerProgreso.stop();
    }

    private void actualizarProgreso() {
        if (!reproductor.estaReproduciendo()) {
            // Audio terminó
            reiniciar();
        }
    }

    private String formatearDuracion(long segundos) {
        long minutos = segundos / 60;
        segundos = segundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    public void limpiar() {
        if (timerProgreso != null) {
            timerProgreso.stop();
        }
        if (reproductor != null) {
            reproductor.cerrar();
        }
    }
}
