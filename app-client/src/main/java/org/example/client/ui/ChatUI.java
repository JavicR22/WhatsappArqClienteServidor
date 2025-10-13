package org.example.client.ui;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.modelo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Chat principal al estilo WhatsApp Desktop.
 * Muestra lista de chats/canales a la izquierda y conversación al centro.
 */
public class ChatUI extends JPanel {

    // 🧩 Componentes de la interfaz
    private DefaultListModel<String> modeloChats = new DefaultListModel<>();
    private DefaultListModel<String> modeloUsuarios = new DefaultListModel<>();
    private JList<String> listaChats;
    private JList<String> listaUsuarios;
    private JPanel panelCentral;
    private JTextArea taMensajes;
    private JTextField tfMensaje;
    private JButton btnEnviar;
    private JButton btnAudio;
    private JButton btnCrearCanal;
    private JButton btnSolicitarUnion;

    // ⚙️ Datos
    private List<Usuario> usuariosVisibles;
    private GestorComunicacion gestorComunicacion;
    private Usuario usuarioActual;
    private Usuario usuarioDestino; // con quien se chatea

    public ChatUI() {
        setLayout(new BorderLayout(10, 10));
        construirPanelLateral();
        construirPanelCentral();
    }

    /** -------------------------------
     * PANEL LATERAL (Chats y Usuarios)
     * ------------------------------- */
    private void construirPanelLateral() {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setPreferredSize(new Dimension(250, 0));

        JTabbedPane pestañas = new JTabbedPane();

        // 🟢 TAB de chats/canales
        listaChats = new JList<>(modeloChats);
        pestañas.addTab("Chats", new JScrollPane(listaChats));
        pestañas.addTab("Channels", new JScrollPane(new JList<>(modeloChats)));

        // 🟢 TAB de usuarios (para “todos los usuarios son visibles”)
        listaUsuarios = new JList<>(modeloUsuarios);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        pestañas.addTab("Usuarios", scrollUsuarios);

        // Evento de doble clic para abrir chat
        listaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && listaUsuarios.getSelectedIndex() != -1) {
                    abrirChatPrivado(listaUsuarios.getSelectedIndex());
                }
            }
        });

        lateral.add(pestañas, BorderLayout.CENTER);

        // Botones inferiores
        JPanel panelBotones = new JPanel(new GridLayout(0, 1, 5, 5));
        btnCrearCanal = new JButton("Crear Canal");
        btnSolicitarUnion = new JButton("Solicitar Unión");
        panelBotones.add(btnCrearCanal);
        panelBotones.add(btnSolicitarUnion);
        lateral.add(panelBotones, BorderLayout.SOUTH);

        add(lateral, BorderLayout.WEST);
    }

    /** -------------------------------
     * PANEL CENTRAL (Área de chat)
     * ------------------------------- */
    private void construirPanelCentral() {
        panelCentral = new JPanel(new BorderLayout(5, 5));
        mostrarMensajeBienvenida();
        add(panelCentral, BorderLayout.CENTER);
    }

    private void mostrarMensajeBienvenida() {
        panelCentral.removeAll();

        JTextArea bienvenida = new JTextArea();
        bienvenida.setEditable(false);
        bienvenida.setLineWrap(true);
        bienvenida.setWrapStyleWord(true);
        bienvenida.setFont(new Font("SansSerif", Font.PLAIN, 14));
        bienvenida.setText("✅ Bienvenido al chat académico.\n💬 Chatea con quien tú quieras.");

        panelCentral.add(new JScrollPane(bienvenida), BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    /** -------------------------------
     * MOSTRAR CHAT CON UN USUARIO
     * ------------------------------- */
    private void mostrarChatCon(Usuario destino) {
        panelCentral.removeAll();
        this.usuarioDestino = destino;

        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));

        JLabel lblTitulo = new JLabel("💬 Chateando con " + destino.getNombre() +
                " (" + destino.getCorreo() + ")");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        chatPanel.add(lblTitulo, BorderLayout.NORTH);

        taMensajes = new JTextArea();
        taMensajes.setEditable(false);
        taMensajes.setLineWrap(true);
        chatPanel.add(new JScrollPane(taMensajes), BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(5, 5));
        tfMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnAudio = new JButton("🎤");
        inferior.add(btnAudio, BorderLayout.WEST);
        inferior.add(tfMensaje, BorderLayout.CENTER);
        inferior.add(btnEnviar, BorderLayout.EAST);
        chatPanel.add(inferior, BorderLayout.SOUTH);

        btnEnviar.addActionListener(e -> enviarMensajePrivado());

        panelCentral.add(chatPanel, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    /** -------------------------------
     * ENVÍO DE MENSAJES
     * ------------------------------- */
    private void enviarMensajePrivado() {
        String texto = tfMensaje.getText().trim();
        if (texto.isEmpty() || usuarioDestino == null) return;

        try {
            MensajeTexto mensaje = new MensajeTexto(
                    java.util.UUID.randomUUID().toString(),
                    usuarioActual,
                    texto
            );

            gestorComunicacion.enviarMensaje(mensaje);
            Mensaje respuesta = gestorComunicacion.recibirMensaje();

            taMensajes.append("Tú: " + texto + "\n");

            if (respuesta instanceof MensajeRespuesta mr && mr.isExito()) {
                taMensajes.append("📩 " + mr.getMensaje() + "\n");
            } else {
                taMensajes.append("⚠️ Error en el envío\n");
            }

            tfMensaje.setText("");

        } catch (Exception ex) {
            taMensajes.append("❌ Error enviando mensaje: " + ex.getMessage() + "\n");
        }
    }

    /** -------------------------------
     * MÉTODOS DE ACTUALIZACIÓN
     * ------------------------------- */
    public void setChats(List<String> nombres) {
        modeloChats.clear();
        for (String n : nombres) modeloChats.addElement(n);
    }

    public void configurarSesion(Usuario usuarioActual, GestorComunicacion gestor) {
        this.usuarioActual = usuarioActual;
        this.gestorComunicacion = gestor;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        modeloUsuarios.clear();
        usuariosVisibles = usuarios;

        for (Usuario u : usuarios) {
            modeloUsuarios.addElement(u.getNombre() + " (" + u.getCorreo() + ") - " + u.getRol());
        }
    }

    private void abrirChatPrivado(int indice) {
        Usuario seleccionado = usuariosVisibles.get(indice);
        if (seleccionado.getCorreo().equals(usuarioActual.getCorreo())) {
            taMensajes.append("No puedes abrir chat contigo mismo 😅\n");
            return;
        }
        mostrarChatCon(seleccionado);
    }
}
