package org.example.client.ui;

import org.example.client.comunicacion.GrabadorAudio;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Panel para grabar audio con controles de grabación, pausa, continuar, cancelar y enviar
 */
public class AudioRecorderPanel extends JPanel {
    private GrabadorAudio grabador;
    private JButton btnGrabar;
    private JButton btnPausar;
    private JButton btnContinuar;
    private JButton btnCancelar;
    private JButton btnEnviar;
    private JLabel lblEstado;
    private JLabel lblDuracion;
    private Timer timerDuracion;
    private AudioRecordingListener listener;

    public interface AudioRecordingListener {
        void onAudioGrabado(File archivoAudio, long duracionSegundos);
        void onCancelado();
    }

    public AudioRecorderPanel(AudioRecordingListener listener) {
        this.listener = listener;
        this.grabador = new GrabadorAudio();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Grabación de Audio"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Panel de estado
        JPanel panelEstado = new JPanel(new GridLayout(2, 1, 5, 5));
        lblEstado = new JLabel("Presiona 🎤 para iniciar grabación");
        lblEstado.setFont(new Font("Arial", Font.BOLD, 12));
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);

        lblDuracion = new JLabel("00:00");
        lblDuracion.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblDuracion.setHorizontalAlignment(SwingConstants.CENTER);
        lblDuracion.setForeground(new Color(220, 53, 69));

        panelEstado.add(lblEstado);
        panelEstado.add(lblDuracion);
        add(panelEstado, BorderLayout.NORTH);

        // Panel de controles
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnGrabar = new JButton("🎤 Grabar");
        btnGrabar.setBackground(new Color(220, 53, 69));
        btnGrabar.setForeground(Color.WHITE);
        btnGrabar.setFocusPainted(false);

        btnPausar = new JButton("⏸ Pausar");
        btnPausar.setEnabled(false);

        btnContinuar = new JButton("▶ Continuar");
        btnContinuar.setEnabled(false);

        btnCancelar = new JButton("✖ Cancelar");
        btnCancelar.setEnabled(false);

        btnEnviar = new JButton("📤 Enviar");
        btnEnviar.setEnabled(false);
        btnEnviar.setBackground(new Color(40, 167, 69));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFocusPainted(false);

        // Listeners
        btnGrabar.addActionListener(e -> iniciarGrabacion());
        btnPausar.addActionListener(e -> pausarGrabacion());
        btnContinuar.addActionListener(e -> continuarGrabacion());
        btnCancelar.addActionListener(e -> cancelarGrabacion());
        btnEnviar.addActionListener(e -> enviarAudio());

        panelControles.add(btnGrabar);
        panelControles.add(btnPausar);
        panelControles.add(btnContinuar);
        panelControles.add(btnCancelar);
        panelControles.add(btnEnviar);

        add(panelControles, BorderLayout.CENTER);

        // Timer para actualizar duración
        timerDuracion = new Timer(1000, e -> actualizarDuracion());
    }

    private void iniciarGrabacion() {
        try {
            grabador.iniciarGrabacion();

            btnGrabar.setEnabled(false);
            btnPausar.setEnabled(true);
            btnContinuar.setEnabled(false);
            btnCancelar.setEnabled(true);
            btnEnviar.setEnabled(false);

            lblEstado.setText("🔴 Grabando...");
            lblEstado.setForeground(new Color(220, 53, 69));

            timerDuracion.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al iniciar grabación: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pausarGrabacion() {
        grabador.pausarGrabacion();

        btnPausar.setEnabled(false);
        btnContinuar.setEnabled(true);
        btnEnviar.setEnabled(true);

        lblEstado.setText("⏸ Pausado - Puedes enviar o continuar");
        lblEstado.setForeground(new Color(255, 193, 7));

        timerDuracion.stop();
    }

    private void continuarGrabacion() {
        grabador.continuarGrabacion();

        btnPausar.setEnabled(true);
        btnContinuar.setEnabled(false);
        btnEnviar.setEnabled(false);

        lblEstado.setText("🔴 Grabando...");
        lblEstado.setForeground(new Color(220, 53, 69));

        timerDuracion.start();
    }

    private void cancelarGrabacion() {
        grabador.cancelarGrabacion();
        resetearControles();

        if (listener != null) {
            listener.onCancelado();
        }
    }

    private void enviarAudio() {
        try {
            // Crear directorio temporal para audios
            File dirAudios = new File("audios_temp");
            if (!dirAudios.exists()) {
                dirAudios.mkdirs();
            }

            String nombreArchivo = "audio_" + System.currentTimeMillis() + ".wav";
            String rutaArchivo = dirAudios.getAbsolutePath() + File.separator + nombreArchivo;

            File archivoAudio = grabador.finalizarGrabacion(rutaArchivo);
            long duracionSegundos = grabador.getDuracionSegundos();

            if (archivoAudio != null && listener != null) {
                listener.onAudioGrabado(archivoAudio, duracionSegundos);
            }

            resetearControles();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al enviar audio: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDuracion() {
        long segundos = grabador.getDuracionSegundos();
        long minutos = segundos / 60;
        segundos = segundos % 60;
        lblDuracion.setText(String.format("%02d:%02d", minutos, segundos));
    }

    private void resetearControles() {
        btnGrabar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnContinuar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnEnviar.setEnabled(false);

        lblEstado.setText("Presiona 🎤 para iniciar grabación");
        lblEstado.setForeground(Color.BLACK);
        lblDuracion.setText("00:00");

        timerDuracion.stop();
    }

    public void reiniciar() {
        // Cancelar cualquier grabación en curso
        if (grabador != null) {
            grabador.cancelarGrabacion();
        }
        // Resetear todos los controles al estado inicial
        resetearControles();
    }
}
