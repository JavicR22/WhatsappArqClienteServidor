package org.example.client.ui;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.controladores.AuthController;
import org.example.client.controladores.CanalController;
import org.example.client.datos.PoolConexiones;
import org.example.client.datos.RepositorioLocal;
import org.example.client.mock.ServidorMock;
import org.example.client.modelo.Usuario;
import org.example.client.negocio.AuthBusinessLogic;
import org.example.client.negocio.CanalBusinessLogic;
import org.example.client.negocio.ServicioNotificaciones;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal que coordina el flujo entre Login y Chat.
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

    public ClienteUI() {
        setTitle("Chat Académico - Cliente");
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

            // Autenticar con el servidor
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

    private void inicializarComponentes() {
        try {
            PoolConexiones poolConexiones = new PoolConexiones(
                    "jdbc:h2:./data/clientdb;AUTO_SERVER=TRUE",
                    5
            );

            // Inicializar repositorio local con el pool
            repositorioLocal = new RepositorioLocal(poolConexiones);

            repositorioLocal.inicializarTablas();

            // Inicializar gestor de comunicación
            gestorComunicacion = new GestorComunicacion();
            gestorComunicacion.activarModoMock(true);

            gestorComunicacion.conectar("localhost", 8080);

            // Inicializar servicio de notificaciones
            servicioNotificaciones = new ServicioNotificaciones();

            // Inicializar lógica de negocio de autenticación
            authBusinessLogic = new AuthBusinessLogic(gestorComunicacion);

            authController = new AuthController(authBusinessLogic);

            // Inicializar lógica de negocio de canales
            canalBusinessLogic = new CanalBusinessLogic(
                    gestorComunicacion,
                    repositorioLocal,
                    authBusinessLogic,
                    servicioNotificaciones
            );

            // Inicializar controlador de canales
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

        chatUI.setChats(ServidorMock.obtenerCanalesPredeterminados());

        Usuario usuarioActual = authBusinessLogic.obtenerUsuarioActual();

        // Configurar sesión
        chatUI.configurarSesion(usuarioActual, gestorComunicacion);

        chatUI.configurarControladorCanales(canalController, repositorioLocal);

        // Obtener usuarios visibles
        List<Usuario> usuarios = ServidorMock.obtenerUsuariosVisibles();
        chatUI.setUsuarios(usuarios);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteUI().setVisible(true));
    }
}
