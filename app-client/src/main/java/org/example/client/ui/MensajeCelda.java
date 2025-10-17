package org.example.client.ui;

import org.example.client.comunicacion.ReproductorAudio;
import org.example.client.modelo.Mensaje;
import org.example.client.modelo.MensajeAudio;
import org.example.client.modelo.MensajeTexto;
import org.example.client.modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Celda personalizada para renderizar mensajes de texto y audio en el chat.
 * Similar a las burbujas de WhatsApp.
 */
public class MensajeCelda extends JPanel {

    private Mensaje mensaje;
    private Usuario usuarioActual;
    private ReproductorAudio reproductor;
    private JButton btnReproducir;
    private boolean reproduciendo = false;

    public MensajeCelda(Mensaje mensaje, Usuario usuarioActual) {
        this.mensaje = mensaje;
        this.usuarioActual = usuarioActual;

        setLayout(new BorderLayout(5, 5));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        construirCelda();
    }

    private void construirCelda() {
        // Determinar si el mensaje es del usuario actual
        boolean esMio = mensaje.getRemitente() != null &&
                usuarioActual != null &&
                mensaje.getRemitente().getCorreo().equals(usuarioActual.getCorreo());

        // Panel contenedor de la burbuja
        JPanel burbuja = new JPanel();
        burbuja.setLayout(new BoxLayout(burbuja, BoxLayout.Y_AXIS));
        burbuja.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Colores según quién envió el mensaje
        if (esMio) {
            burbuja.setBackground(new Color(220, 248, 198)); // Verde claro (mensajes propios)
        } else {
            burbuja.setBackground(Color.WHITE); // Blanco (mensajes recibidos)
        }

        // Renderizar según el tipo de mensaje
        if (mensaje instanceof MensajeTexto) {
            construirMensajeTexto(burbuja, (MensajeTexto) mensaje, esMio);
        } else if (mensaje instanceof MensajeAudio) {
            construirMensajeAudio(burbuja, (MensajeAudio) mensaje, esMio);
        }

        // Alinear la burbuja según quién envió el mensaje
        JPanel contenedor = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        contenedor.setOpaque(false);
        contenedor.add(burbuja);

        add(contenedor, BorderLayout.CENTER);
    }

    private void construirMensajeTexto(JPanel burbuja, MensajeTexto mensajeTexto, boolean esMio) {
        // Nombre del remitente (solo si no es mío)
        if (!esMio && mensajeTexto.getRemitente() != null) {
            JLabel lblNombre = new JLabel(mensajeTexto.getRemitente().getNombre());
            lblNombre.setFont(new Font("Arial", Font.BOLD, 11));
            lblNombre.setForeground(new Color(0, 120, 215));
            lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
            burbuja.add(lblNombre);
            burbuja.add(Box.createVerticalStrut(3));
        }

        // Contenido del mensaje
        JTextArea txtContenido = new JTextArea(mensajeTexto.getContenido());
        txtContenido.setEditable(false);
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        txtContenido.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtContenido.setOpaque(false);
        txtContenido.setBorder(null);
        txtContenido.setAlignmentX(Component.LEFT_ALIGNMENT);
        burbuja.add(txtContenido);
    }

    private void construirMensajeAudio(JPanel burbuja, MensajeAudio mensajeAudio, boolean esMio) {
        // Nombre del remitente (solo si no es mío)
        if (!esMio && mensajeAudio.getRemitente() != null) {
            JLabel lblNombre = new JLabel(mensajeAudio.getRemitente().getNombre());
            lblNombre.setFont(new Font("Arial", Font.BOLD, 11));
            lblNombre.setForeground(new Color(0, 120, 215));
            lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
            burbuja.add(lblNombre);
            burbuja.add(Box.createVerticalStrut(3));
        }

        // Panel de audio con controles
        JPanel panelAudio = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelAudio.setOpaque(false);
        panelAudio.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Botón de reproducir/pausar
        btnReproducir = new JButton("▶");
        btnReproducir.setFont(new Font("Arial", Font.PLAIN, 16));
        btnReproducir.setPreferredSize(new Dimension(40, 40));
        btnReproducir.setFocusPainted(false);
        btnReproducir.addActionListener(e -> toggleReproduccion(mensajeAudio));

        // Etiqueta de duración
        JLabel lblDuracion = new JLabel(formatearDuracion(mensajeAudio.getDuracionSegundos()));
        lblDuracion.setFont(new Font("Arial", Font.PLAIN, 12));

        // Icono de audio
        JLabel lblIcono = new JLabel("🎤");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        panelAudio.add(btnReproducir);
        panelAudio.add(lblIcono);
        panelAudio.add(lblDuracion);

        burbuja.add(panelAudio);
    }

    private void toggleReproduccion(MensajeAudio mensajeAudio) {
        try {
            if (reproduciendo) {
                // Pausar
                if (reproductor != null) {
                    reproductor.pausar();
                }
                btnReproducir.setText("▶");
                reproduciendo = false;
            } else {
                // Reproducir
                File archivo = new File(mensajeAudio.getRutaArchivo());
                if (!archivo.exists()) {
                    JOptionPane.showMessageDialog(this,
                            "El archivo de audio no existe: " + mensajeAudio.getRutaArchivo(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (reproductor == null) {
                    reproductor = new ReproductorAudio();
                }

                reproductor.cargar(archivo);
                reproductor.reproducir();
                btnReproducir.setText("⏸");
                reproduciendo = true;

                // Crear un hilo para detectar cuando termine la reproducción
                new Thread(() -> {
                    try {
                        while (reproductor.estaReproduciendo()) {
                            Thread.sleep(100);
                        }
                        SwingUtilities.invokeLater(() -> {
                            btnReproducir.setText("▶");
                            reproduciendo = false;
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al reproducir audio: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            btnReproducir.setText("▶");
            reproduciendo = false;
        }
    }

    private String formatearDuracion(long segundos) {
        long minutos = segundos / 60;
        long segs = segundos % 60;
        return String.format("%d:%02d", minutos, segs);
    }

    public void limpiarRecursos() {
        if (reproductor != null) {
            reproductor.detener();
        }
    }
}
