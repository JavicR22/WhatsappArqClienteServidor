package org.example.client.ui;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.controladores.CanalController;
import org.example.client.datos.RepositorioLocal;
import org.example.client.modelo.*;
import org.example.client.mock.ServidorMock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

/**
 * Chat principal al estilo WhatsApp Desktop.
 * Muestra lista de chats/canales a la izquierda y conversación al centro.
 */
public class ChatUI extends JPanel {

    // 🧩 Componentes de la interfaz
    private DefaultListModel<String> modeloChats = new DefaultListModel<>();
    private DefaultListModel<String> modeloCanales = new DefaultListModel<>();
    private DefaultListModel<String> modeloUsuarios = new DefaultListModel<>();
    private JList<String> listaChats;
    private JList<String> listaCanales;
    private JList<String> listaUsuarios;
    private JPanel panelCentral;
    private JTextArea taMensajes;
    private JTextField tfMensaje;
    private JButton btnEnviar;
    private JButton btnAudio;
    private JButton btnCrearCanal;
    private JButton btnInvitarUsuarios;
    private JButton btnVerInvitaciones;
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

    public ChatUI() {
        setLayout(new BorderLayout(10, 10));
        construirPanelLateral();
        construirPanelCentral();
    }

    /** -------------------------------
     * PANEL LATERAL (Chats, Canales y Usuarios)
     * ------------------------------- */
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

        // 🟢 TAB de chats privados
        listaChats = new JList<>(modeloChats);
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

        btnCrearCanal.addActionListener(e -> mostrarPanelCrearCanal());
        btnInvitarUsuarios.addActionListener(e -> mostrarPanelInvitarUsuarios());
        btnVerInvitaciones.addActionListener(e -> mostrarPanelInvitaciones());

        panelBotones.add(btnCrearCanal);
        panelBotones.add(btnInvitarUsuarios);
        panelBotones.add(btnVerInvitaciones);
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
        bienvenida.setText("✅ Bienvenido al chat académico.\n💬 Chatea con quien tú quieras.\n📢 Crea canales y colabora con tu comunidad.");

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


    /** -------------------------------
     * MOSTRAR CHAT EN UN CANAL
     * ------------------------------- */
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

        taMensajes = new JTextArea();
        taMensajes.setEditable(false);
        taMensajes.setLineWrap(true);
        taMensajes.append("Bienvenido al canal: " + canal.getNombre() + "\n");
        taMensajes.append("Miembros: " + String.join(", ", canal.getMiembros()) + "\n\n");
        chatPanel.add(new JScrollPane(taMensajes), BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(5, 5));
        tfMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnAudio = new JButton("🎤");
        inferior.add(btnAudio, BorderLayout.WEST);
        inferior.add(tfMensaje, BorderLayout.CENTER);
        inferior.add(btnEnviar, BorderLayout.EAST);
        chatPanel.add(inferior, BorderLayout.SOUTH);

        btnEnviar.addActionListener(e -> enviarMensajeCanal());

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

            taMensajes.append("[" + usuarioActual.getNombre() + "]: " + texto + "\n");

            if (respuesta instanceof MensajeRespuesta mr && mr.isExito()) {
                // Mensaje enviado exitosamente al canal
            } else {
                taMensajes.append("⚠️ Error en el envío\n");
            }

            tfMensaje.setText("");

        } catch (Exception ex) {
            taMensajes.append("❌ Error enviando mensaje: " + ex.getMessage() + "\n");
        }
    }

    /** -------------------------------
     * PANELES DE GESTIÓN DE CANALES (en lugar de diálogos)
     * ------------------------------- */
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
        List<Usuario> usuarios = usuariosVisibles != null ? usuariosVisibles : ServidorMock.obtenerUsuariosVisibles();
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

    /** -------------------------------
     * MÉTODOS DE ACTUALIZACIÓN
     * ------------------------------- */
    public void setChats(List<String> nombres) {
        modeloChats.clear();
        for (String n : nombres) modeloChats.addElement(n);
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

        actualizarPanelUsuarioActual();
    }

    private void actualizarPanelUsuarioActual() {
        if (usuarioActual == null || panelUsuarioActual == null) return;

        panelUsuarioActual.removeAll();

        // Crear panel con foto y nombre
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        infoPanel.setOpaque(false);

        // Cargar y mostrar foto si existe
        if (usuarioActual.getFotoBase64() != null && !usuarioActual.getFotoBase64().isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(usuarioActual.getFotoBase64());
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage originalImage = ImageIO.read(bis);

                if (originalImage != null) {
                    int size = 48;
                    Image scaledImage = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    JLabel lblFoto = new JLabel(new ImageIcon(scaledImage));
                    lblFoto.setPreferredSize(new Dimension(size, size));
                    lblFoto.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
                    infoPanel.add(lblFoto);
                } else {
                    JLabel lblFotoDefault = new JLabel("👤");
                    lblFotoDefault.setFont(new Font("SansSerif", Font.PLAIN, 32));
                    lblFotoDefault.setPreferredSize(new Dimension(48, 48));
                    infoPanel.add(lblFotoDefault);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al decodificar foto: " + e.getMessage());
                JLabel lblFotoDefault = new JLabel("👤");
                lblFotoDefault.setFont(new Font("SansSerif", Font.PLAIN, 32));
                lblFotoDefault.setPreferredSize(new Dimension(48, 48));
                infoPanel.add(lblFotoDefault);
            }
        } else {
            JLabel lblFotoDefault = new JLabel("👤");
            lblFotoDefault.setFont(new Font("SansSerif", Font.PLAIN, 32));
            lblFotoDefault.setPreferredSize(new Dimension(48, 48));
            infoPanel.add(lblFotoDefault);
        }


        // Mostrar nombre del usuario
        JPanel textoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textoPanel.setOpaque(false);

        JLabel lblNombre = new JLabel(usuarioActual.getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 14));
        textoPanel.add(lblNombre);

        JLabel lblCorreo = new JLabel(usuarioActual.getCorreo());
        lblCorreo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblCorreo.setForeground(Color.GRAY);
        textoPanel.add(lblCorreo);

        infoPanel.add(textoPanel);
        panelUsuarioActual.add(infoPanel, BorderLayout.CENTER);

        panelUsuarioActual.revalidate();
        panelUsuarioActual.repaint();
    }

    public void configurarControladorCanales(CanalController controller, RepositorioLocal repo) {
        this.canalController = controller;
        this.repositorioLocal = repo;
        cargarCanales();
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
}
