package org.example.client.ui;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.controladores.CanalController;
import org.example.client.datos.RepositorioLocal;
import org.example.client.modelo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Chat principal al estilo WhatsApp Desktop.
 * Muestra lista de chats/canales a la izquierda y conversación al centro.
 */
public class ChatUI extends JPanel {

    // 🧩 Componentes de la interfaz
    private DefaultListModel<ChatPreview> modeloChats = new DefaultListModel<>();
    private DefaultListModel<String> modeloCanales = new DefaultListModel<>();
    private DefaultListModel<String> modeloUsuarios = new DefaultListModel<>();
    private JList<ChatPreview> listaChats;
    private JList<String> listaCanales;
    private JList<String> listaUsuarios;
    private JPanel panelCentral;
    private JList<Mensaje> listaMensajes;
    private DefaultListModel<Mensaje> modeloMensajes;
    private JTextField tfMensaje;
    private JButton btnEnviar;
    private JButton btnAudio;
    private JButton btnCrearCanal;
    private JButton btnInvitarUsuarios;
    private JButton btnVerInvitaciones;
    private JButton btnCerrarSesion;
    private JPanel panelUsuarioActual;

    // ⚙️ Datos
    private List<Usuario> usuariosVisibles;
    private List<Canal> canales;
    private GestorComunicacion gestorComunicacion;
    private Usuario usuarioActual;
    private Usuario usuarioDestino; // con quien se chatea
    private Canal canalActual; // canal seleccionado
    private CanalController canalController;
    private RepositorioLocal repositorioLocal;
    private LogoutListener logoutListener;

    private JPanel panelInferior;
    private CardLayout cardLayoutInferior;
    private JPanel panelModoTexto;
    private JPanel panelModoGrabacion;
    private AudioRecorderPanel audioRecorderPanel;


    public interface LogoutListener {
        void onLogout();
    }

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
    }

    public ChatUI() {
        setLayout(new BorderLayout(10, 10));
        construirPanelLateral();
        construirPanelCentral();
    }

    /** ------------------------------- */
    /** PANEL LATERAL (Chats, Canales y Usuarios) */
    /** ------------------------------- */
    private void construirPanelLateral() {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setPreferredSize(new Dimension(250, 0));

        panelUsuarioActual = new JPanel(new BorderLayout(5, 5));
        panelUsuarioActual.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panelUsuarioActual.setBackground(new Color(240, 240, 240));
        lateral.add(panelUsuarioActual, BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();

        listaChats = new JList<>(modeloChats);
        listaChats.setCellRenderer(new ChatPreviewCellRenderer());
        listaChats.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && listaChats.getSelectedIndex() != -1) {
                    abrirChatDesdePreview(listaChats.getSelectedIndex());
                }
            }
        });
        pestañas.addTab("Chats", new JScrollPane(listaChats));

        // 🟢 TAB de canales
        listaCanales = new JList<>(modeloCanales);
        listaCanales.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && listaCanales.getSelectedIndex() != -1) {
                    abrirChatCanal(listaCanales.getSelectedIndex());
                }
            }
        });
        pestañas.addTab("Canales", new JScrollPane(listaCanales));

        // 🟢 TAB de usuarios (para "todos los usuarios son visibles")
        listaUsuarios = new JList<>(modeloUsuarios);
        listaUsuarios.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                // Buscar el usuario correspondiente
                if (usuariosVisibles != null && index < usuariosVisibles.size()) {
                    Usuario u = usuariosVisibles.get(index);

                    // Decodificar foto Base64 (si existe)
                    if (u.getFotoBase64() != null && !u.getFotoBase64().isEmpty() && !u.getFotoBase64().equals("DEFAULT")) {
                        try {
                            byte[] bytes = java.util.Base64.getDecoder().decode(u.getFotoBase64());
                            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes);
                            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(bis);
                            if (img != null) {
                                Image scaled = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                                label.setIcon(new ImageIcon(scaled));
                            } else {
                                label.setIcon(new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)));
                            }
                        } catch (Exception ex) {
                            label.setIcon(new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)));
                        }
                    } else {
                        // Foto por defecto (emoji 👤)
                        label.setIcon(new ImageIcon(crearIconoTexto("👤", 32, 32)));
                    }

                    label.setText(u.getNombre() != null ? u.getNombre() : u.getCorreo());
                }

                if (isSelected) {
                    label.setBackground(new Color(0xD0E8FF));
                }

                return label;
            }
        });

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

        JPanel panelBotones = new JPanel(new GridLayout(0, 1, 5, 5));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        btnCrearCanal = new JButton("➕ Crear Canal");
        btnInvitarUsuarios = new JButton("👥 Invitar Usuarios");
        btnVerInvitaciones = new JButton("📬 Ver Invitaciones");
        btnCerrarSesion = new JButton("🚪 Cerrar Sesión");

        btnCrearCanal.addActionListener(e -> mostrarPanelCrearCanal());
        btnInvitarUsuarios.addActionListener(e -> mostrarPanelInvitarUsuarios());
        btnVerInvitaciones.addActionListener(e -> mostrarPanelInvitaciones());
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panelBotones.add(btnCrearCanal);
        panelBotones.add(btnInvitarUsuarios);
        panelBotones.add(btnVerInvitaciones);
        panelBotones.add(btnCerrarSesion);
        lateral.add(panelBotones, BorderLayout.SOUTH);

        add(lateral, BorderLayout.WEST);
    }

    private class ChatPreviewCellRenderer extends JPanel implements ListCellRenderer<ChatPreview> {
        private JLabel lblNombre;
        private JLabel lblUltimoMensaje;
        private JLabel lblFecha;
        private JLabel lblFoto;

        public ChatPreviewCellRenderer() {
            setLayout(new BorderLayout(8, 4));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            lblFoto = new JLabel();
            lblFoto.setPreferredSize(new Dimension(40, 40));

            JPanel panelTexto = new JPanel(new BorderLayout(2, 2));
            panelTexto.setOpaque(false);

            lblNombre = new JLabel();
            lblNombre.setFont(new Font("Arial", Font.BOLD, 13));

            lblUltimoMensaje = new JLabel();
            lblUltimoMensaje.setFont(new Font("Arial", Font.PLAIN, 11));
            lblUltimoMensaje.setForeground(Color.GRAY);

            lblFecha = new JLabel();
            lblFecha.setFont(new Font("Arial", Font.PLAIN, 10));
            lblFecha.setForeground(Color.GRAY);
            lblFecha.setHorizontalAlignment(SwingConstants.RIGHT);

            panelTexto.add(lblNombre, BorderLayout.NORTH);
            panelTexto.add(lblUltimoMensaje, BorderLayout.CENTER);

            add(lblFoto, BorderLayout.WEST);
            add(panelTexto, BorderLayout.CENTER);
            add(lblFecha, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ChatPreview> list, ChatPreview preview,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (preview != null) {
                lblNombre.setText(preview.getNombreContacto());
                lblUltimoMensaje.setText(preview.getUltimoMensaje() != null ? preview.getUltimoMensaje() : "Sin mensajes");

                // Formatear fecha
                if (preview.getFechaUltimoMensaje() != null) {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                    lblFecha.setText(preview.getFechaUltimoMensaje().format(formatter));
                } else {
                    lblFecha.setText("");
                }

                // Foto del contacto
                if (preview.getFotoBase64() != null && !preview.getFotoBase64().isEmpty()) {
                    try {
                        byte[] bytes = Base64.getDecoder().decode(preview.getFotoBase64());
                        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                        BufferedImage img = ImageIO.read(bis);
                        if (img != null) {
                            Image scaled = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                            lblFoto.setIcon(new ImageIcon(scaled));
                        } else {
                            lblFoto.setIcon(new ImageIcon(crearIconoTexto("👤", 40, 40)));
                        }
                    } catch (Exception ex) {
                        lblFoto.setIcon(new ImageIcon(crearIconoTexto("👤", 40, 40)));
                    }
                } else {
                    lblFoto.setIcon(new ImageIcon(crearIconoTexto("👤", 40, 40)));
                }
            }

            if (isSelected) {
                setBackground(new Color(0xD0E8FF));
            } else {
                setBackground(Color.WHITE);
            }

            return this;
        }
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas cerrar sesión?",
                "Confirmar cierre de sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                // Enviar mensaje "Exit" al servidor
                if (gestorComunicacion != null && gestorComunicacion.estaConectado()) {
                    gestorComunicacion.enviarMensajeExit();
                    System.out.println("[v0] Mensaje Exit enviado al servidor");
                }

                // Cerrar sesión en la base de datos local
                if (repositorioLocal != null && usuarioActual != null) {
                    repositorioLocal.cerrarTodasLasSesiones(usuarioActual.getCorreo());
                    System.out.println("[v0] Sesión cerrada en base de datos local");
                }

                // Notificar al listener (ClienteUI)
                if (logoutListener != null) {
                    logoutListener.onLogout();
                }

            } catch (Exception ex) {
                System.err.println("Error al cerrar sesión: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Error al cerrar sesión: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirChatDesdePreview(int indice) {
        if (indice < 0 || indice >= modeloChats.getSize()) return;

        ChatPreview preview = modeloChats.getElementAt(indice);

        // Buscar el usuario correspondiente
        Usuario destino = null;
        if (usuariosVisibles != null) {
            for (Usuario u : usuariosVisibles) {
                if (u.getCorreo().equals(preview.getCorreoContacto())) {
                    destino = u;
                    break;
                }
            }
        }

        if (destino == null) {
            // Crear usuario temporal si no está en la lista
            destino = new Usuario(
                    preview.getCorreoContacto(),
                    preview.getNombreContacto(),
                    preview.getCorreoContacto(),
                    "",
                    ""
            );
            destino.setFotoBase64(preview.getFotoBase64());
        }

        mostrarChatCon(destino);
    }

    public void cargarChatPreviews() {
        if (repositorioLocal == null || usuarioActual == null) return;

        modeloChats.clear();
        List<ChatPreview> previews = repositorioLocal.obtenerChatPreviews(usuarioActual.getCorreo());

        if (previews.isEmpty()) {
            // Mostrar mensaje de que no hay chats
            System.out.println("[v0] No hay chats previos");
        } else {
            for (ChatPreview preview : previews) {
                // Buscar foto del contacto si está disponible
                if (usuariosVisibles != null) {
                    for (Usuario u : usuariosVisibles) {
                        if (u.getCorreo().equals(preview.getCorreoContacto())) {
                            preview.setFotoBase64(u.getFotoBase64());
                            preview.setNombreContacto(u.getNombre());
                            break;
                        }
                    }
                }
                modeloChats.addElement(preview);
            }
        }
    }

    /** ------------------------------- */
    /** PANEL CENTRAL (Área de chat) */
    /** ------------------------------- */
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
        bienvenida.setText("✅ Bienvenido al chat académico.\n💬 Chatea con quien tú quieras.\n📢 Crea canales y colabora con tu comunidad.");

        panelCentral.add(new JScrollPane(bienvenida), BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    /** ------------------------------- */
    /** MOSTRAR CHAT CON UN USUARIO */
    /** ------------------------------- */
    private void mostrarChatCon(Usuario destino) {
        panelCentral.removeAll();
        this.usuarioDestino = destino;
        this.canalActual = null;

        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> mostrarMensajeBienvenida());

        JLabel lblTitulo = new JLabel("💬 Chateando con " + destino.getNombre() +
                " (" + destino.getCorreo() + ")");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));

        headerPanel.add(btnVolver, BorderLayout.WEST);
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        chatPanel.add(headerPanel, BorderLayout.NORTH);

        modeloMensajes = new DefaultListModel<>();
        listaMensajes = new JList<>(modeloMensajes);
        listaMensajes.setCellRenderer(new ListCellRenderer<Mensaje>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Mensaje> list,
                                                          Mensaje value, int index, boolean isSelected, boolean cellHasFocus) {
                return new MensajeCelda(value, usuarioActual);
            }
        });
        listaMensajes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cargarHistorialMensajes(destino);

        JScrollPane scrollMensajes = new JScrollPane(listaMensajes);
        scrollMensajes.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatPanel.add(scrollMensajes, BorderLayout.CENTER);

        construirPanelInferior();
        chatPanel.add(panelInferior, BorderLayout.SOUTH);

        panelCentral.add(chatPanel, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    private void construirPanelInferior() {
        cardLayoutInferior = new CardLayout();
        panelInferior = new JPanel(cardLayoutInferior);

        // Panel modo texto
        panelModoTexto = new JPanel(new BorderLayout(5, 5));
        tfMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnAudio = new JButton("🎤");

        btnAudio.addActionListener(e -> activarModoGrabacion());
        btnEnviar.addActionListener(e -> enviarMensajePrivado());
        tfMensaje.addActionListener(e -> enviarMensajePrivado());

        panelModoTexto.add(btnAudio, BorderLayout.WEST);
        panelModoTexto.add(tfMensaje, BorderLayout.CENTER);
        panelModoTexto.add(btnEnviar, BorderLayout.EAST);

        // Panel modo grabación
        panelModoGrabacion = new JPanel(new BorderLayout());
        audioRecorderPanel = new AudioRecorderPanel(new AudioRecorderPanel.AudioRecordingListener() {
            @Override
            public void onAudioGrabado(File archivoAudio, long duracionSegundos) {
                try {
                    // Leer el archivo de audio como bytes
                    byte[] audioData = java.nio.file.Files.readAllBytes(archivoAudio.toPath());

                    // Crear mensaje de audio
                    MensajeAudioPrivado mensajeAudio = new MensajeAudioPrivado(
                            java.util.UUID.randomUUID().toString(),
                            usuarioActual,
                            usuarioDestino,
                            archivoAudio.getAbsolutePath(),
                            duracionSegundos,
                            audioData
                    );

                    // Enviar al servidor
                    enviarMensajeAudio(mensajeAudio);

                    // Guardar en base de datos local
                    if (repositorioLocal != null) {
                        repositorioLocal.guardarMensajeAudio(
                                mensajeAudio.getId(),
                                usuarioActual.getCorreo(),
                                usuarioDestino.getCorreo(),
                                null,
                                archivoAudio.getAbsolutePath(),
                                duracionSegundos
                        );
                    }

                    // Agregar a la lista de mensajes
                    modeloMensajes.addElement(mensajeAudio);

                    // Auto-scroll to bottom
                    SwingUtilities.invokeLater(() -> {
                        listaMensajes.ensureIndexIsVisible(modeloMensajes.getSize() - 1);
                    });

                    // Volver a modo texto
                    activarModoTexto();

                    JOptionPane.showMessageDialog(ChatUI.this,
                            "Audio enviado exitosamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    System.err.println("Error enviando audio: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ChatUI.this,
                            "Error enviando audio: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    activarModoTexto();
                }
            }

            @Override
            public void onCancelado() {
                activarModoTexto();
            }
        });
        panelModoGrabacion.add(audioRecorderPanel, BorderLayout.CENTER);

        // Agregar ambos paneles al CardLayout
        panelInferior.add(panelModoTexto, "TEXTO");
        panelInferior.add(panelModoGrabacion, "GRABACION");

        // Mostrar modo texto por defecto
        cardLayoutInferior.show(panelInferior, "TEXTO");
    }

    private void activarModoGrabacion() {
        if (usuarioDestino == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay un destinatario seleccionado",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        cardLayoutInferior.show(panelInferior, "GRABACION");
        audioRecorderPanel.reiniciar();
    }

    private void activarModoTexto() {
        cardLayoutInferior.show(panelInferior, "TEXTO");
        tfMensaje.requestFocus();
    }


    private void enviarMensajeAudio(MensajeAudioPrivado mensajeAudio) {
        try {
            // Por ahora, el protocolo de audio se implementará en el servidor
            // Aquí enviamos la metadata del audio
            String protocolo = "AUDIO_PRIVADO|" +
                    mensajeAudio.getReceptorCorreo() + "|" +
                    mensajeAudio.getDuracionSegundos() + "|" +
                    java.util.Base64.getEncoder().encodeToString(mensajeAudio.getAudioData());

            gestorComunicacion.enviarMensaje(mensajeAudio);
            System.out.println("[v0] Mensaje de audio enviado a " + mensajeAudio.getReceptorCorreo());

        } catch (Exception ex) {
            System.err.println("Error enviando mensaje de audio: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }


    private Image crearIconoTexto(String texto, int ancho, int alto) {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, ancho, alto);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        FontMetrics fm = g2.getFontMetrics();
        int x = (ancho - fm.stringWidth(texto)) / 2;
        int y = (alto - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(texto, x, y);
        g2.dispose();
        return img;
    }

    private void cargarHistorialMensajes(Usuario destino) {
        if (repositorioLocal == null || usuarioActual == null) {
            return;
        }

        try {
            List<MensajeTexto> mensajes = repositorioLocal.obtenerMensajesConUsuario(
                    usuarioActual.getCorreo(),
                    destino.getCorreo()
            );

            modeloMensajes.clear();

            if (mensajes.isEmpty()) {
                // Optionally show empty state
            } else {
                for (MensajeTexto msg : mensajes) {
                    modeloMensajes.addElement(msg);
                }
            }

            // Auto-scroll to bottom
            SwingUtilities.invokeLater(() -> {
                if (modeloMensajes.getSize() > 0) {
                    listaMensajes.ensureIndexIsVisible(modeloMensajes.getSize() - 1);
                }
            });
        } catch (Exception e) {
            System.err.println("Error cargando historial: " + e.getMessage());
        }
    }

    /** ------------------------------- */
    /** MOSTRAR CHAT EN UN CANAL */
    /** ------------------------------- */
    private void mostrarChatCanal(Canal canal) {
        panelCentral.removeAll();
        this.canalActual = canal;
        this.usuarioDestino = null;

        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> mostrarMensajeBienvenida());

        JLabel lblTitulo = new JLabel("📢 Canal: " + canal.getNombre() +
                (canal.isPrivado() ? " (Privado)" : " (Público)") +
                " - " + canal.getMiembros().size() + " miembros");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));

        headerPanel.add(btnVolver, BorderLayout.WEST);
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        chatPanel.add(headerPanel, BorderLayout.NORTH);

        modeloMensajes = new DefaultListModel<>();
        listaMensajes = new JList<>(modeloMensajes);
        listaMensajes.setCellRenderer(new ListCellRenderer<Mensaje>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Mensaje> list,
                                                          Mensaje value, int index, boolean isSelected, boolean cellHasFocus) {
                return new MensajeCelda(value, usuarioActual);
            }
        });
        listaMensajes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        MensajeTexto bienvenida = new MensajeTexto(
                java.util.UUID.randomUUID().toString(),
                new Usuario("SYSTEM", "Sistema", "sistema@chat.com", "", "SYSTEM"),
                "Bienvenido al canal: " + canal.getNombre() + "\nMiembros: " + String.join(", ", canal.getMiembros())
        );
        modeloMensajes.addElement(bienvenida);

        JScrollPane scrollMensajes = new JScrollPane(listaMensajes);
        scrollMensajes.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatPanel.add(scrollMensajes, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(5, 5));
        tfMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnAudio = new JButton("🎤");
        btnAudio.setEnabled(false); // Deshabilitar audio en canales por ahora
        inferior.add(btnAudio, BorderLayout.WEST);
        inferior.add(tfMensaje, BorderLayout.CENTER);
        inferior.add(btnEnviar, BorderLayout.EAST);
        chatPanel.add(inferior, BorderLayout.SOUTH);

        btnEnviar.addActionListener(e -> enviarMensajeCanal());

        panelCentral.add(chatPanel, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    /** ------------------------------- */
    /** ENVÍO DE MENSAJES */
    /** ------------------------------- */
    private void enviarMensajePrivado() {
        String texto = tfMensaje.getText().trim();
        if (texto.isEmpty() || usuarioDestino == null) return;

        try {
            MensajeTextoPrivado mensaje = new MensajeTextoPrivado(
                    java.util.UUID.randomUUID().toString(),
                    usuarioActual,
                    usuarioDestino,
                    texto
            );

            gestorComunicacion.enviarMensaje(mensaje);

            if (repositorioLocal != null) {
                repositorioLocal.guardarMensaje(
                        mensaje.getId(),
                        usuarioActual.getCorreo(),
                        usuarioDestino.getCorreo(),
                        null,
                        texto,
                        "PRIVADO"
                );
                cargarChatPreviews();
            }

            modeloMensajes.addElement(mensaje);

            // Auto-scroll to bottom
            SwingUtilities.invokeLater(() -> {
                listaMensajes.ensureIndexIsVisible(modeloMensajes.getSize() - 1);
            });

            tfMensaje.setText("");

        } catch (Exception ex) {
            System.err.println("Error enviando mensaje: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error enviando mensaje: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarMensajeCanal() {
        String texto = tfMensaje.getText().trim();
        if (texto.isEmpty() || canalActual == null) return;

        try {
            MensajeTexto mensaje = new MensajeTexto(
                    java.util.UUID.randomUUID().toString(),
                    usuarioActual,
                    texto
            );

            gestorComunicacion.enviarMensaje(mensaje);
            Mensaje respuesta = gestorComunicacion.recibirMensaje();

            modeloMensajes.addElement(mensaje);

            // Auto-scroll to bottom
            SwingUtilities.invokeLater(() -> {
                listaMensajes.ensureIndexIsVisible(modeloMensajes.getSize() - 1);
            });

            if (respuesta instanceof MensajeRespuesta mr && !mr.isExito()) {
                JOptionPane.showMessageDialog(this,
                        "Error en el envío: " + mr.getMensaje(),
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
            }

            tfMensaje.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error enviando mensaje: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** ------------------------------- */
    /** PANELES DE GESTIÓN DE CANALES (en lugar de diálogos) */
    /** ------------------------------- */
    private void mostrarPanelCrearCanal() {
        if (canalController == null) {
            JOptionPane.showMessageDialog(this,
                    "El controlador de canales no está inicializado",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        panelCentral.removeAll();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> mostrarMensajeBienvenida());
        JLabel lblTitulo = new JLabel("➕ Crear Nuevo Canal");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(btnVolver, BorderLayout.WEST);
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNombre = new JTextField(20);
        JTextArea txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JCheckBox chkPrivado = new JCheckBox("Canal Privado");

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nombre del Canal:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(txtDescripcion), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(chkPrivado, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnCrear = new JButton("Crear Canal");

        btnCancelar.addActionListener(e -> mostrarMensajeBienvenida());
        btnCrear.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            boolean privado = chkPrivado.isSelected();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El nombre del canal es obligatorio",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean exito = canalController.crearCanal(nombre, descripcion, privado);

            if (exito) {
                JOptionPane.showMessageDialog(this,
                        "Canal creado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarCanales();
                mostrarMensajeBienvenida();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al crear el canal",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        botonesPanel.add(btnCancelar);
        botonesPanel.add(btnCrear);
        panel.add(botonesPanel, BorderLayout.SOUTH);

        panelCentral.add(panel, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    public void setUsuariosConectados(List<UsuarioConectado> lista) {
        modeloUsuarios.clear();
        if (lista == null || lista.isEmpty()) {
            modeloUsuarios.addElement("Nadie en línea");
            return;
        }

        for (UsuarioConectado u : lista) {
            String texto = u.getUsername();
            modeloUsuarios.addElement(texto);
        }
    }

    private void mostrarPanelInvitarUsuarios() {
        if (canalController == null || canales == null || canales.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Primero debes crear un canal",
                    "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        panelCentral.removeAll();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> mostrarMensajeBienvenida());
        JLabel lblTitulo = new JLabel("👥 Invitar Usuarios a Canal");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(btnVolver, BorderLayout.WEST);
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));

        // Selección de canal
        JPanel canalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        canalPanel.add(new JLabel("Selecciona el canal:"));
        JComboBox<String> cmbCanales = new JComboBox<>();
        for (Canal c : canales) {
            cmbCanales.addItem(c.getNombre());
        }
        canalPanel.add(cmbCanales);
        formPanel.add(canalPanel, BorderLayout.NORTH);

        // Lista de usuarios
        JPanel usuariosPanel = new JPanel(new BorderLayout());
        usuariosPanel.add(new JLabel("Selecciona usuarios para invitar:"), BorderLayout.NORTH);

        DefaultListModel<String> modeloUsuariosInvitar = new DefaultListModel<>();
        List<Usuario> usuarios = usuariosVisibles != null ? usuariosVisibles : List.of();

        for (Usuario u : usuarios) {
            if (!u.getCorreo().equals(usuarioActual.getCorreo())) {
                modeloUsuariosInvitar.addElement(u.getNombre() + " (" + u.getCorreo() + ")");
            }
        }

        JList<String> listaUsuariosInvitar = new JList<>(modeloUsuariosInvitar);
        listaUsuariosInvitar.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        usuariosPanel.add(new JScrollPane(listaUsuariosInvitar), BorderLayout.CENTER);

        formPanel.add(usuariosPanel, BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.CENTER);

        // Botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnInvitar = new JButton("Enviar Invitaciones");

        btnCancelar.addActionListener(e -> mostrarMensajeBienvenida());
        btnInvitar.addActionListener(e -> {
            int canalIdx = cmbCanales.getSelectedIndex();
            if (canalIdx < 0) return;

            Canal canal = canales.get(canalIdx);
            List<String> seleccionados = listaUsuariosInvitar.getSelectedValuesList();

            if (seleccionados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Debes seleccionar al menos un usuario",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Extraer correos
            java.util.List<String> correos = new java.util.ArrayList<>();
            for (String sel : seleccionados) {
                int inicio = sel.indexOf('(') + 1;
                int fin = sel.indexOf(')');
                if (inicio > 0 && fin > inicio) {
                    correos.add(sel.substring(inicio, fin));
                }
            }

            boolean exito = canalController.invitarUsuarios(canal.getId(), canal.getNombre(), correos);

            if (exito) {
                JOptionPane.showMessageDialog(this,
                        "Invitaciones enviadas exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                mostrarMensajeBienvenida();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al enviar invitaciones",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        botonesPanel.add(btnCancelar);
        botonesPanel.add(btnInvitar);
        panel.add(botonesPanel, BorderLayout.SOUTH);

        panelCentral.add(panel, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    private void mostrarPanelInvitaciones() {
        if (canalController == null || repositorioLocal == null) {
            JOptionPane.showMessageDialog(this,
                    "El sistema de invitaciones no está disponible",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        panelCentral.removeAll();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> {
            cargarCanales();
            mostrarMensajeBienvenida();
        });
        JLabel lblTitulo = new JLabel("📬 Invitaciones Pendientes");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(btnVolver, BorderLayout.WEST);
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Panel de solicitudes
        SolicitudesPanel panelSolicitudes = new SolicitudesPanel(canalController, repositorioLocal);
        panel.add(panelSolicitudes, BorderLayout.CENTER);

        panelCentral.add(panel, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    /** ------------------------------- */
    /** MÉTODOS DE ACTUALIZACIÓN */
    /** ------------------------------- */
    public void setChats(List<String> nombres) {
        System.out.println("[v0] setChats() deprecated, usar cargarChatPreviews()");
    }

    public void cargarCanales() {
        if (canalController == null) return;

        modeloCanales.clear();
        canales = canalController.obtenerCanales();

        if (canales.isEmpty()) {
            modeloCanales.addElement("No tienes canales aún");
        } else {
            for (Canal c : canales) {
                modeloCanales.addElement(c.toString());
            }
        }
    }

    public void configurarSesion(Usuario usuarioActual, GestorComunicacion gestor) {
        this.usuarioActual = usuarioActual;
        this.gestorComunicacion = gestor;

        gestor.setMensajePrivadoListener((emisor, contenido) -> {
            SwingUtilities.invokeLater(() -> {
                // Save incoming message to database
                if (repositorioLocal != null) {
                    String idMensaje = java.util.UUID.randomUUID().toString();
                    repositorioLocal.guardarMensaje(
                            idMensaje,
                            emisor,
                            usuarioActual.getCorreo(),
                            null,
                            contenido,
                            "PRIVADO"
                    );
                    cargarChatPreviews();
                }

                // Display message if chat is open with this user
                if (usuarioDestino != null && usuarioDestino.getCorreo().equals(emisor)) {
                    MensajeTextoPrivado mensajeEntrante = new MensajeTextoPrivado(
                            java.util.UUID.randomUUID().toString(),
                            usuarioDestino,
                            usuarioActual,
                            contenido
                    );
                    modeloMensajes.addElement(mensajeEntrante);

                    // Auto-scroll to bottom
                    SwingUtilities.invokeLater(() -> {
                        listaMensajes.ensureIndexIsVisible(modeloMensajes.getSize() - 1);
                    });
                } else {
                    // Show notification or update chat list
                    System.out.println("[v0] Nuevo mensaje de " + emisor + " (chat no abierto)");
                }
            });
        });

        actualizarPanelUsuarioActual();

        // Esto se ejecuta en el EDT para asegurar que la UI esté lista
        SwingUtilities.invokeLater(() -> {
            if (repositorioLocal != null) {
                cargarChatPreviews();
                System.out.println("[v0] Chat previews cargados al iniciar sesión");
            } else {
                System.err.println("[v0] No se pueden cargar chat previews: repositorioLocal es null");
            }
        });
    }

    public void configurarControladorCanales(CanalController controller, RepositorioLocal repo) {
        this.repositorioLocal = repo;
        this.canalController = controller;
        cargarCanales();

        if (usuarioActual != null && repositorioLocal != null) {
            SwingUtilities.invokeLater(() -> {
                cargarChatPreviews();
                System.out.println("[v0] Chat previews cargados desde configurarControladorCanales");
            });
        }
    }

    public void setUsuarios(List<Usuario> usuarios) {
        modeloUsuarios.clear();
        usuariosVisibles = usuarios;

        for (Usuario u : usuarios) {
            modeloUsuarios.addElement(u.getNombre() + " (" + u.getCorreo() + ") - " + u.getRol());
        }
    }

    private void abrirChatPrivado(int indice) {
        if (usuariosVisibles == null || indice >= usuariosVisibles.size()) return;

        Usuario seleccionado = usuariosVisibles.get(indice);
        if (seleccionado.getCorreo().equals(usuarioActual.getCorreo())) {
            JOptionPane.showMessageDialog(this,
                    "No puedes abrir chat contigo mismo",
                    "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        mostrarChatCon(seleccionado);
    }

    private void abrirChatCanal(int indice) {
        if (canales == null || canales.isEmpty() || indice >= canales.size()) return;

        Canal canal = canales.get(indice);
        mostrarChatCanal(canal);
    }

    private void actualizarPanelUsuarioActual() {
        if (usuarioActual == null) return;

        panelUsuarioActual.removeAll();

        // Create user info panel
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setOpaque(false);

        // User photo
        JLabel lblFoto = new JLabel();
        if (usuarioActual.getFotoBase64() != null && !usuarioActual.getFotoBase64().isEmpty()
                && !usuarioActual.getFotoBase64().equals("DEFAULT")) {
            try {
                byte[] bytes = Base64.getDecoder().decode(usuarioActual.getFotoBase64());
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                BufferedImage img = ImageIO.read(bis);
                if (img != null) {
                    Image scaled = img.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                    lblFoto.setIcon(new ImageIcon(scaled));
                } else {
                    lblFoto.setIcon(new ImageIcon(crearIconoTexto("👤", 48, 48)));
                }
            } catch (Exception ex) {
                lblFoto.setIcon(new ImageIcon(crearIconoTexto("👤", 48, 48)));
            }
        } else {
            lblFoto.setIcon(new ImageIcon(crearIconoTexto("👤", 48, 48)));
        }

        // User name and email
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        JLabel lblNombre = new JLabel(usuarioActual.getNombre());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblCorreo = new JLabel(usuarioActual.getCorreo());
        lblCorreo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblCorreo.setForeground(Color.GRAY);
        textPanel.add(lblNombre);
        textPanel.add(lblCorreo);

        infoPanel.add(lblFoto, BorderLayout.WEST);
        infoPanel.add(textPanel, BorderLayout.CENTER);

        panelUsuarioActual.add(infoPanel, BorderLayout.CENTER);
        panelUsuarioActual.revalidate();
        panelUsuarioActual.repaint();
    }
}
