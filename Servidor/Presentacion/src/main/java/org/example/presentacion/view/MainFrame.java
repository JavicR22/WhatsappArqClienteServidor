package org.example.presentacion.view;

import org.example.controladores.ServidorControlador;
import org.example.controladores.UsuarioControlador;
import org.example.entidades.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {
    private final ServidorControlador controller;
    private final UsuarioControlador usuarioControlador;
    private final JSpinner spinnerMaxUsers;
    private final JButton btnIniciar;
    private final JPanel centerPanel;
    private final DefaultListModel<UserListItem> connectedModel;
    private final JList<UserListItem> connectedList;

    private final JButton btnRegistrar;
    private final JButton btnVerConectados;

    private UserFormPanel registroPanel;

    public MainFrame(ServidorControlador controller, UsuarioControlador usuarioControlador) {
        this.usuarioControlador=usuarioControlador;
        this.controller = controller;
        setTitle("Panel Servidor Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === TOP ===
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Puerto:"));
        JTextField puertoField = new JTextField("5000", 6);
        top.add(puertoField);

        top.add(new JLabel("Max Usuarios:"));
        spinnerMaxUsers = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        top.add(spinnerMaxUsers);

        JCheckBox chkLimitado = new JCheckBox("Es limitado", true);
        top.add(chkLimitado);

        btnIniciar = new JButton("Iniciar Servidor");
        top.add(btnIniciar);
        add(top, BorderLayout.NORTH);

        // === LEFT PANEL ===
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(220, 0));

        JPanel navButtons = new JPanel(new GridLayout(0, 1, 5, 5));
        btnRegistrar = new JButton("Registrar Usuario");
        btnVerConectados = new JButton("Ver Conectados");

        navButtons.add(btnRegistrar);
        navButtons.add(btnVerConectados);
        leftPanel.add(navButtons, BorderLayout.NORTH);

        // Lista de conectados (oculta al inicio)
        connectedModel = new DefaultListModel<>();
        connectedList = new JList<>(connectedModel);
        connectedList.setCellRenderer(new UserListRenderer());
        JScrollPane scrollUsuarios = new JScrollPane(connectedList);
        leftPanel.add(scrollUsuarios, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        // === CENTER PANEL ===
        centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        // === EVENTOS ===
        btnIniciar.addActionListener((ActionEvent e) -> {
            int puerto = Integer.parseInt(puertoField.getText().trim());
            int maxUsuarios = (int) spinnerMaxUsers.getValue();
            boolean esLimitado = chkLimitado.isSelected();

            controller.iniciarServidor(puerto, maxUsuarios, esLimitado);
            btnIniciar.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Servidor iniciado correctamente");
        });

        btnRegistrar.addActionListener(e -> mostrarFormularioRegistro());
        btnVerConectados.addActionListener(e -> mostrarUsuariosConectados());

        // evento doble clic o click derecho en usuario
        connectedList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    UserListItem item = connectedList.getSelectedValue();
                    if (item != null) {
                        int op = JOptionPane.showConfirmDialog(MainFrame.this,
                                "¿Desea cerrar la conexión de " + item.getUsername() + "?",
                                "Cerrar conexión", JOptionPane.YES_NO_OPTION);
                        if (op == JOptionPane.YES_OPTION) {
                            controller.cerrarConexion(item.getUsername());
                        }
                    }
                }
            }
        });

        // Timer refresca usuarios conectados
        Timer timer = new Timer(1000, ev -> {
            if (connectedList.isVisible()) {
                actualizarUsuariosConectados();
            }
        });
        timer.start();
    }

    /** Muestra un solo formulario de registro */
    private void mostrarFormularioRegistro() {
        centerPanel.removeAll();
        registroPanel = new UserFormPanel("Registrar nuevo usuario");
        JButton btnGuardar = new JButton("Guardar Usuario");
        btnGuardar.addActionListener(e -> {
            System.err.println(registroPanel.getRutaFoto());
            controller.registrarUsuario(
                    registroPanel.getUsername(),
                    registroPanel.getEmail(),
                    registroPanel.getPassword(),
                    registroPanel.getIp(),
                    registroPanel.getRutaFoto()
            );
            JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.");
        });
        JPanel container = new JPanel(new BorderLayout());
        container.add(registroPanel, BorderLayout.CENTER);
        container.add(btnGuardar, BorderLayout.SOUTH);
        centerPanel.add(container, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    /** Muestra la lista de usuarios conectados */
    private void mostrarUsuariosConectados() {
        centerPanel.removeAll();
        JScrollPane scroll = new JScrollPane(connectedList);
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        actualizarUsuariosConectados();
    }

    /** Refresca lista lateral de usuarios conectados */
    private void actualizarUsuariosConectados() {
        java.util.Collection<String> usuarios = controller.obtenerUsuariosConectados();

        SwingUtilities.invokeLater(() -> {
            connectedModel.clear();

            usuarios.forEach(username -> {
                ImageIcon avatar = null;

                // 2. Usar el controlador para buscar la entidad Usuario completa
                Usuario usuario = usuarioControlador.buscarUsuarioPorUsername(username);

                if (usuario != null) {
                    // Asumo que la entidad Usuario tiene un método getRutaFoto()
                    String rutaFoto = usuario.getRutaFoto();

                    // 3. Verificar la ruta y cargar la imagen
                    if (rutaFoto != null && !rutaFoto.isEmpty()) {
                        // Usamos ImageUtils para cargar la imagen con el estilo circular/dimensionado
                        // Importante: Esto requiere que ImageUtils y las clases de imagen estén disponibles
                        try {
                            // 🔑 Crear el ImageIcon a partir de la ruta
                            // Si ImageUtils está en el mismo paquete, usarlo directamente
                            avatar = ImageUtils.createCircularImageIcon(rutaFoto, 48);
                        } catch (Exception ex) {
                            System.err.println("Advertencia: No se pudo cargar la foto para " + username + " desde: " + rutaFoto);
                            // avatar queda como null si falla la carga
                        }
                    }
                }

                // 4. Agregar el UserListItem con el avatar (ImageIcon o null)
                connectedModel.addElement(new UserListItem(username, avatar));
            });
        });
    }
}
