package org.example.client.ui;

import org.example.client.tcp.GestorConexionesCliente;
import org.example.common.entidades.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.UUID;

public class MainClientUI extends JFrame {

    private JTextArea taLogs;
    private JTextField tfCorreo;
    private JPasswordField pfContrasena;
    private JButton btnConectar;
    private JTextField tfMensaje;
    private JButton btnEnviar;

    private GestorConexionesCliente conexion;

    public MainClientUI() {
        setTitle("Cliente - Chat Académico");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Panel superior: credenciales
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topPanel.add(new JLabel("Correo:"));
        tfCorreo = new JTextField(15);
        topPanel.add(tfCorreo);
        topPanel.add(new JLabel("Contraseña:"));
        pfContrasena = new JPasswordField(10);
        topPanel.add(pfContrasena);
        btnConectar = new JButton("Conectar");
        btnConectar.addActionListener(this::onConectar);
        topPanel.add(btnConectar);
        add(topPanel, BorderLayout.NORTH);

        // Centro: logs
        taLogs = new JTextArea();
        taLogs.setEditable(false);
        taLogs.setLineWrap(true);
        taLogs.setWrapStyleWord(true);
        add(new JScrollPane(taLogs), BorderLayout.CENTER);

        // Inferior: enviar mensaje
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        tfMensaje = new JTextField();
        tfMensaje.setEnabled(false);
        btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);
        btnEnviar.addActionListener(this::onEnviar);
        bottomPanel.add(tfMensaje, BorderLayout.CENTER);
        bottomPanel.add(btnEnviar, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        log("Interfaz cliente lista.");
    }

    private void onConectar(ActionEvent e) {
        String correo = tfCorreo.getText().trim();
        String contrasena = new String(pfContrasena.getPassword());

        if (correo.isEmpty() || contrasena.isEmpty()) {
            log("❗ Rellena correo y contraseña.");
            return;
        }

        String host = "localhost";
        int port = 8080;

        conexion = new GestorConexionesCliente();
        log("Conectando a " + host + ":" + port + " ...");

        boolean ok = conexion.conectar(host, port);
        if (!ok) {
            log("❌ No se pudo conectar al servidor.");
            return;
        }

        log("✅ Conectado. Enviando autenticación...");

        Usuario u = new Usuario(null, null, correo, null, null);
        MensajeAutenticacion auth = new MensajeAutenticacion(
                UUID.randomUUID().toString(),
                u,
                correo,
                contrasena
        );

        try {
            conexion.enviarMensaje(auth);
            var respuesta = conexion.leerMensaje();
            if (respuesta instanceof MensajeRespuesta mr) {
                log("Servidor: " + mr.getMensaje());
                if (!mr.isExito()) {
                    log("❌ Autenticación fallida. Cerrando conexión.");
                    conexion.cerrarConexion();
                    return;
                } else {
                    log("🔓 Autenticación exitosa. UsuarioId: " + mr.getUsuarioId());
                    tfMensaje.setEnabled(true);
                    btnEnviar.setEnabled(true);
                    btnConectar.setEnabled(false);
                    tfCorreo.setEnabled(false);
                    pfContrasena.setEnabled(false);

                    new Thread(() -> {
                        try {
                            while (true) {
                                var m = conexion.leerMensaje();
                                if (m == null) break;
                                if (m instanceof MensajeTexto mt) {
                                    log("💬 " + mt.getRemitente().getNombre() + ": " + mt.getContenido());
                                } else {
                                    log("📨 Mensaje recibido: " + m.getClass().getSimpleName());
                                }
                            }
                        } catch (Exception ex) {
                            log("❌ Conexión finalizada: " + ex.getMessage());
                        }
                    }, "ListenerThread").start();
                }
            } else {
                log("⚠️ Respuesta inesperada al autenticar: " + respuesta.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            log("❌ Error autenticando: " + ex.getMessage());
            conexion.cerrarConexion();
        }
    }

    private void onEnviar(ActionEvent e) {
        String texto = tfMensaje.getText().trim();
        if (texto.isEmpty()) return;

        Usuario dummy = new Usuario(null, "Yo", null, null, null);
        MensajeTexto msg = new MensajeTexto(UUID.randomUUID().toString(), dummy, texto);
        try {
            conexion.enviarMensaje(msg);
            log("Tú: " + texto);
            tfMensaje.setText("");
        } catch (Exception ex) {
            log("❌ Error enviando mensaje: " + ex.getMessage());
        }
    }

    private void log(String texto) {
        SwingUtilities.invokeLater(() -> taLogs.append(texto + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainClientUI().setVisible(true));
    }
}
