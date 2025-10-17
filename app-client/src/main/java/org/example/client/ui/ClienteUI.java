package org.example.client.ui;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.controladores.AuthController;
import org.example.client.controladores.CanalController;
import org.example.client.datos.PoolConexiones;
import org.example.client.datos.RepositorioLocal;
import org.example.client.modelo.Canal;
import org.example.client.modelo.Sesion;
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
import java.util.Optional;

/**
 * ClienteUI actualizado:
 * - Permite conectarse al servidor real o usar modo mock.
 * - La ventana de conexión permanece visible hasta que se conecte o el usuario cierre la ventana.
 * - Soporta persistencia de sesión y reconexión automática
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
    private Sesion sesionActual;

    public ClienteUI(String ipServidor, int puertoServidor, GestorComunicacion gestorYaConectado) {
        this.ipServidor = ipServidor;
        this.puertoServidor = puertoServidor;

        // Reutilizamos el gestor que ya validó la conexión
        this.gestorComunicacion = gestorYaConectado;

        setTitle("Chat Académico - Cliente (SERVIDOR REAL)");

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

        chatUI.setLogoutListener(() -> {
            cerrarSesionYVolverAlLogin();
        });

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
                crearYGuardarSesion(correo);
                mostrarChat();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Credenciales incorrectas",
                        "Error de autenticación",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        verificarSesionActiva();
    }

    private void verificarSesionActiva() {
        // Este método se puede llamar al iniciar para verificar si hay una sesión guardada
        // Por ahora, simplemente mostramos el login
        System.out.println("[v0] Verificando sesión activa...");
    }

    private void crearYGuardarSesion(String correoUsuario) {
        try {
            String idSesion = java.util.UUID.randomUUID().toString();
            String token = java.util.UUID.randomUUID().toString();
            sesionActual = new Sesion(idSesion, correoUsuario, token);

            if (repositorioLocal != null) {
                boolean guardado = repositorioLocal.guardarSesion(sesionActual);
                if (guardado) {
                    System.out.println("[v0] Sesión guardada correctamente: " + idSesion);
                } else {
                    System.err.println("[v0] Error al guardar sesión");
                }
            }
        } catch (Exception e) {
            System.err.println("[v0] Error creando sesión: " + e.getMessage());
        }
    }

    private void cerrarSesionYVolverAlLogin() {
        try {
            // Cerrar sesión en la base de datos
            if (sesionActual != null && repositorioLocal != null) {
                repositorioLocal.cerrarSesion(sesionActual.getId());
                System.out.println("[v0] Sesión cerrada en base de datos");
            }

            // Cerrar conexión con el servidor
            if (gestorComunicacion != null && gestorComunicacion.estaConectado()) {
                gestorComunicacion.cerrarConexion();
                System.out.println("[v0] Conexión con servidor cerrada");
            }

            // Limpiar sesión actual
            sesionActual = null;

            // Volver a la pantalla de login
            layout.show(panelPrincipal, "login");

            // Mostrar diálogo de reconexión
            SwingUtilities.invokeLater(() -> {
                int opcion = JOptionPane.showConfirmDialog(
                        this,
                        "Sesión cerrada. ¿Desea conectarse nuevamente?",
                        "Sesión cerrada",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );

                if (opcion == JOptionPane.YES_OPTION) {
                    // Mostrar ventana de conexión
                    VentanaConexion dialogo = new VentanaConexion(this, ipServidor, puertoServidor);
                    dialogo.setVisible(true);

                    GestorComunicacion nuevoGestor = dialogo.getGestorConectado();
                    if (nuevoGestor != null) {
                        gestorComunicacion = nuevoGestor;
                        authBusinessLogic = new AuthBusinessLogic(gestorComunicacion);
                        authController = new AuthController(authBusinessLogic);
                        JOptionPane.showMessageDialog(this,
                                "Reconectado exitosamente. Por favor inicie sesión.",
                                "Reconexión exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo reconectar. La aplicación se cerrará.",
                                "Error de reconexión",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            });

        } catch (Exception e) {
            System.err.println("[v0] Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
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
                gestorComunicacion.activarModoMock(false);
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

            gestorComunicacion.setCanalCreadoListener((idCanal, nombre, descripcion, privado, creadorEmail, creadoEn) -> {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[v0] ========================================");
                    System.out.println("[v0] CANAL_CREADO recibido del servidor");
                    System.out.println("[v0] ID: " + idCanal);
                    System.out.println("[v0] Nombre: " + nombre);
                    System.out.println("[v0] Descripción: " + descripcion);
                    System.out.println("[v0] Privado: " + privado);
                    System.out.println("[v0] Creador: " + creadorEmail);
                    System.out.println("[v0] Timestamp: " + creadoEn);
                    System.out.println("[v0] ========================================");

                    try {
                        List<String> miembros = new ArrayList<>();
                        miembros.add(creadorEmail);
                        Canal nuevoCanal = new Canal(idCanal, nombre, descripcion, privado, creadorEmail, miembros, creadoEn);

                        System.out.println("[v0] Canal creado en memoria con " + nuevoCanal.getMiembros().size() + " miembros");
                        System.out.println("[v0] Miembros: " + String.join(", ", nuevoCanal.getMiembros()));

                        if (repositorioLocal == null) {
                            System.err.println("[v0] ❌ ERROR: repositorioLocal es NULL");
                            JOptionPane.showMessageDialog(this,
                                    "Error: Sistema de persistencia no disponible",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        System.out.println("[v0] Intentando guardar canal en base de datos...");
                        boolean guardado = repositorioLocal.guardarCanal(nuevoCanal);

                        if (guardado) {
                            System.out.println("[v0] ✅ Canal guardado exitosamente en base de datos");

                            List<Canal> canalesGuardados = repositorioLocal.obtenerCanalesDelUsuario(creadorEmail);
                            System.out.println("[v0] Canales en BD para " + creadorEmail + ": " + canalesGuardados.size());
                            for (Canal c : canalesGuardados) {
                                System.out.println("[v0]   - " + c.getNombre() + " (ID: " + c.getId() + ")");
                            }

                            if (chatUI != null) {
                                System.out.println("[v0] Actualizando lista de canales en UI...");
                                chatUI.cargarCanales();
                                System.out.println("[v0] ✅ Lista de canales actualizada en UI");
                            } else {
                                System.err.println("[v0] ❌ chatUI es NULL, no se puede actualizar UI");
                            }

                            JOptionPane.showMessageDialog(this,
                                    "Canal '" + nombre + "' creado y guardado exitosamente",
                                    "Canal creado",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            System.err.println("[v0] ❌ Error: guardarCanal() retornó false");
                            JOptionPane.showMessageDialog(this,
                                    "Canal creado en servidor pero no se pudo guardar localmente",
                                    "Advertencia",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        System.err.println("[v0] ❌ Excepción procesando CANAL_CREADO: " + e.getMessage());
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                "Error procesando canal: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });

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

        Usuario usuarioActual = authBusinessLogic.obtenerUsuarioActual();

        chatUI.configurarControladorCanales(canalController, repositorioLocal);

        // Ahora configurar la sesión (esto llamará a cargarChatPreviews con repositorioLocal ya configurado)
        chatUI.configurarSesion(usuarioActual, gestorComunicacion);

        System.out.println("[v0] Cargando canales existentes para: " + usuarioActual.getCorreo());
        chatUI.cargarCanales();

        List<UsuarioConectado> conectados = authBusinessLogic.obtenerUsuariosConectados();
        List<Usuario> listaUsuarios = new ArrayList<>();
        for (UsuarioConectado uc : conectados) {
            Usuario u = new Usuario(uc.getUsername(), uc.getUsername(), uc.getUsername(), "", "");
            u.setFotoBase64(uc.getFotoBase64());
            listaUsuarios.add(u);
        }
        chatUI.setUsuarios(listaUsuarios);

        chatUI.setUsuariosConectados(conectados);

        gestorComunicacion.setMensajeListener(mensaje -> {
            if (mensaje.startsWith("USUARIOS_CONECTADOS|")) {
                // Actualizar la lista de usuarios en el EDT de Swing
                SwingUtilities.invokeLater(() -> {
                    List<UsuarioConectado> usuariosActualizados = authBusinessLogic.obtenerUsuariosConectados();
                    List<Usuario> listaActualizada = new ArrayList<>();
                    for (UsuarioConectado uc : usuariosActualizados) {
                        Usuario u = new Usuario(uc.getUsername(), uc.getUsername(), uc.getUsername(), "", "");
                        u.setFotoBase64(uc.getFotoBase64());
                        listaActualizada.add(u);
                    }
                    chatUI.setUsuarios(listaActualizada);
                    chatUI.setUsuariosConectados(usuariosActualizados);

                    chatUI.cargarChatPreviews();

                    System.out.println("[v0] Lista de usuarios actualizada en la UI: " + usuariosActualizados.size() + " usuarios");
                });
            }
        });
    }

    /**
     * Diálogo modal para pedir IP y puerto. Intenta la conexión al servidor antes de cerrar.
     * Si la conexión falla, permanece visible (permite reintento).
     */
    public static class VentanaConexion extends JDialog {
        private final JTextField campoIp;
        private final JTextField campoPuerto;
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

            botonConectar = new JButton("Conectar");
            botonCancelar = new JButton("Cancelar");

            JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            botones.add(botonConectar);
            botones.add(botonCancelar);

            c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
            add(botones, c);

            // Intento de conexión cuando el usuario presiona Conectar
            botonConectar.addActionListener(e -> {

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
                boolean conectado = gestorTemp.conectar(ip, puerto);

                if (conectado) {
                    // Éxito: guardamos el gestor y cerramos el diálogo
                    this.gestorConectado = gestorTemp;
                    String modo = usarModoMock ? "MOCK" : "REAL";
                    JOptionPane.showMessageDialog(this,
                            "Conectado exitosamente al servidor real.",
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

            ClienteUI cliente = new ClienteUI(ip, puerto, gestorConectado);
            cliente.setVisible(true);
        });
    }
}
