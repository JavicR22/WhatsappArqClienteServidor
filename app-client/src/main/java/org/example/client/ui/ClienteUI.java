package org.example.client.ui;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.controladores.AuthController;
import org.example.client.controladores.CanalController;
import org.example.client.datos.PoolConexiones;
import org.example.client.datos.RepositorioLocal;
import org.example.client.mock.ServidorMock;
import org.example.client.modelo.Canal;
import org.example.client.modelo.Usuario;
import org.example.client.modelo.UsuarioConectado;
import org.example.client.negocio.AuthBusinessLogic;
import org.example.client.negocio.CanalBusinessLogic;
import org.example.client.negocio.ServicioNotificaciones;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * ClienteUI actualizado:
 * - Permite conectarse al servidor real o usar modo mock.
 * - La ventana de conexión permanece visible hasta que se conecte o el usuario cierre la ventana.
 */
public class ClienteUI extends JFrame {

    private CardLayout layout;
    private JPanel panelPrincipal;
    private LoginUI loginUI;
    private ChatUI chatUI;

    private GestorComunicacion gestorComunicacion;
    private RepositorioLocal repositorioLocal;
    private ServicioNotificaciones servicioNotificaciones;
    private AuthBusinessLogic authBusinessLogic;
    private AuthController authController;
    private CanalBusinessLogic canalBusinessLogic;
    private CanalController canalController;

    private final String ipServidor;
    private final int puertoServidor;
    private final boolean usarModoMock;

    public ClienteUI(String ipServidor, int puertoServidor, GestorComunicacion gestorYaConectado, boolean usarModoMock) {
        this.ipServidor = ipServidor;
        this.puertoServidor = puertoServidor;
        this.usarModoMock = usarModoMock;
        // Reutilizamos el gestor que ya validó la conexión
        this.gestorComunicacion = gestorYaConectado;

        setTitle("Chat Académico - Cliente" + (usarModoMock ? " (MODO MOCK)" : " (SERVIDOR REAL)"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        inicializarComponentes();

        layout = new CardLayout();
        panelPrincipal = new JPanel(layout);

        loginUI = new LoginUI();
        chatUI = new ChatUI();

        panelPrincipal.add(loginUI, "login");
        panelPrincipal.add(chatUI, "chat");

        add(panelPrincipal);

        loginUI.addLoginListener(e -> {
            String correo = loginUI.getUsuario();
            String contrasena = loginUI.getContrasena();

            if (correo.isEmpty() || contrasena.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Por favor ingrese correo y contraseña",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean autenticado = authController.autenticar(correo, contrasena);

            if (autenticado) {
                mostrarChat();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Credenciales incorrectas",
                        "Error de autenticación",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Inicializa componentes del cliente. Se asume que gestorComunicacion ya está conectado.
     */
    private void inicializarComponentes() {
        try {
            PoolConexiones poolConexiones = new PoolConexiones(
                    "jdbc:h2:./data/clientdb;AUTO_SERVER=TRUE",
                    5
            );

            repositorioLocal = new RepositorioLocal(poolConexiones);
            repositorioLocal.inicializarTablas();

            // Si gestorComunicacion no fue pasado, crear uno (defensivo) y conectar
            if (gestorComunicacion == null) {
                gestorComunicacion = new GestorComunicacion();
                gestorComunicacion.activarModoMock(usarModoMock);
                boolean conectado = gestorComunicacion.conectar(ipServidor, puertoServidor);
                if (!conectado) {
                    throw new RuntimeException("No se pudo conectar al servidor desde inicializarComponentes.");
                }
            }

            servicioNotificaciones = new ServicioNotificaciones();
            authBusinessLogic = new AuthBusinessLogic(gestorComunicacion);
            authController = new AuthController(authBusinessLogic);

            canalBusinessLogic = new CanalBusinessLogic(
                    gestorComunicacion,
                    repositorioLocal,
                    authBusinessLogic,
                    servicioNotificaciones
            );

            canalController = new CanalController(canalBusinessLogic);

            System.out.println("✅ Componentes inicializados correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error inicializando componentes: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar la aplicación: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrarChat() {
        layout.show(panelPrincipal, "chat");

        if (usarModoMock) {
            List<Canal> canalesPredeterminados = ServidorMock.obtenerCanalesPredeterminados();

            List<String> nombresCanales = new java.util.ArrayList<>();
            for (Canal canal : canalesPredeterminados) {
                if (canal != null && canal.getNombre() != null) {
                    nombresCanales.add(canal.getNombre());
                }
            }

            chatUI.setChats(nombresCanales);

            List<Usuario> usuarios = ServidorMock.obtenerUsuariosVisibles();
            chatUI.setUsuarios(usuarios);
        }

        Usuario usuarioActual = authBusinessLogic.obtenerUsuarioActual();
        chatUI.configurarSesion(usuarioActual, gestorComunicacion);
        List<UsuarioConectado> conectados = authBusinessLogic.obtenerUsuariosConectados();
        List<Usuario> listaUsuarios = new ArrayList<>();
        for (UsuarioConectado uc : conectados) {
            Usuario u = new Usuario(uc.getUsername(), uc.getUsername(), uc.getUsername(), "", "");
            u.setFotoBase64(uc.getFotoBase64());
            listaUsuarios.add(u);
        }
        chatUI.setUsuarios(listaUsuarios);

        chatUI.setUsuariosConectados(conectados);

        chatUI.configurarControladorCanales(canalController, repositorioLocal);
    }

    /**
     * Diálogo modal para pedir IP y puerto. Intenta la conexión al servidor antes de cerrar.
     * Si la conexión falla, permanece visible (permite reintento).
     */
    public static class VentanaConexion extends JDialog {
        private final JTextField campoIp;
        private final JTextField campoPuerto;
        private final JCheckBox checkModoMock;
        private final JButton botonConectar;
        private final JButton botonCancelar;
        private GestorComunicacion gestorConectado = null;
        private boolean usarModoMock = false;

        public VentanaConexion(Frame parent, String ipDefault, int puertoDefault) {
            super(parent, "Conexión al Servidor", true);
            setLayout(new GridBagLayout());
            setSize(400, 220);
            setResizable(false);
            setLocationRelativeTo(parent);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(8, 8, 8, 8);
            c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;

            add(new JLabel("Dirección IP:"), c);
            c.gridx = 1;
            campoIp = new JTextField(15);
            campoIp.setText(ipDefault == null ? "127.0.0.1" : ipDefault);
            add(campoIp, c);

            c.gridx = 0; c.gridy = 1;
            add(new JLabel("Puerto:"), c);
            c.gridx = 1;
            campoPuerto = new JTextField(15);
            campoPuerto.setText(String.valueOf(puertoDefault <= 0 ? 8080 : puertoDefault));
            add(campoPuerto, c);

            c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
            checkModoMock = new JCheckBox("Usar servidor simulado (Mock)");
            checkModoMock.setSelected(false);
            add(checkModoMock, c);

            botonConectar = new JButton("Conectar");
            botonCancelar = new JButton("Cancelar");

            JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            botones.add(botonConectar);
            botones.add(botonCancelar);

            c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
            add(botones, c);

            // Intento de conexión cuando el usuario presiona Conectar
            botonConectar.addActionListener(e -> {
                usarModoMock = checkModoMock.isSelected();

                String ip = campoIp.getText().trim();
                String puertoTxt = campoPuerto.getText().trim();
                if (ip.isEmpty() || puertoTxt.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Debe ingresar una IP y un puerto válidos",
                            "Datos incompletos",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int puerto;
                try {
                    puerto = Integer.parseInt(puertoTxt);
                    if (puerto < 0 || puerto > 65535) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Puerto inválido. Debe ser un número entre 0 y 65535.",
                            "Puerto inválido",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                GestorComunicacion gestorTemp = new GestorComunicacion();
                gestorTemp.activarModoMock(usarModoMock);
                boolean conectado = gestorTemp.conectar(ip, puerto);

                if (conectado) {
                    // Éxito: guardamos el gestor y cerramos el diálogo
                    this.gestorConectado = gestorTemp;
                    String modo = usarModoMock ? "MOCK" : "REAL";
                    JOptionPane.showMessageDialog(this,
                            "Conectado exitosamente al servidor (" + modo + ")",
                            "Conexión exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                } else {
                    // Falló la conexión: mostramos error y dejamos el diálogo abierto para reintento
                    JOptionPane.showMessageDialog(this,
                            "No se pudo conectar al servidor en " + ip + ":" + puerto + "\nVerifique IP/puerto y reintente.",
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            botonCancelar.addActionListener(e -> {
                this.gestorConectado = null;
                setVisible(false);
            });

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    gestorConectado = null;
                    setVisible(false);
                }
            });
        }

        public GestorComunicacion getGestorConectado() {
            return gestorConectado;
        }

        public String getIp() {
            return campoIp.getText().trim();
        }

        public int getPuerto() {
            try {
                return Integer.parseInt(campoPuerto.getText().trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        public boolean isUsarModoMock() {
            return usarModoMock;
        }
    }

    /**
     * Punto de entrada: muestra el diálogo de conexión y solo si se logra conectar
     * crea la instancia única de ClienteUI.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaConexion dialogo = new VentanaConexion(null, "127.0.0.1", 8080);
            dialogo.setVisible(true);

            GestorComunicacion gestorConectado = dialogo.getGestorConectado();
            if (gestorConectado == null) {
                System.out.println("❌ Conexión cancelada o no establecida. Saliendo.");
                System.exit(0);
                return;
            }

            String ip = dialogo.getIp();
            int puerto = dialogo.getPuerto();
            boolean usarModoMock = dialogo.isUsarModoMock();

            ClienteUI cliente = new ClienteUI(ip, puerto, gestorConectado, usarModoMock);
            cliente.setVisible(true);
        });
    }
}
