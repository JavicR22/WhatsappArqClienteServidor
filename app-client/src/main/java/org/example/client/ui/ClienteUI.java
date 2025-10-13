package org.example.client.ui;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.mock.ServidorMock;
import org.example.client.modelo.Usuario;

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

    public ClienteUI() {
        setTitle("Chat Académico - Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        layout = new CardLayout();
        panelPrincipal = new JPanel(layout);

        loginUI = new LoginUI();
        chatUI = new ChatUI();

        panelPrincipal.add(loginUI, "login");
        panelPrincipal.add(chatUI, "chat");

        add(panelPrincipal);

        // Acción de login (aquí puedes conectar con tu AuthController)
        loginUI.addLoginListener(e -> {
            // Simulación: autenticación exitosa
            mostrarChat();
        });
    }

    public void mostrarChat() {
        layout.show(panelPrincipal, "chat");
        chatUI.setChats(java.util.List.of("General", "Investigación", "Clase-2025"));

        // Usuario autenticado simulado
        Usuario usuarioActual = new Usuario("5", "Tú", "cliente@uni.edu", "1234", "Estudiante");

        // Crear y activar conexión mock
        GestorComunicacion gestor = new GestorComunicacion();
        gestor.activarModoMock(true);
        gestor.conectar("localhost", 8080);

        // Configurar sesión
        chatUI.configurarSesion(usuarioActual, gestor);

        // Obtener usuarios visibles
        List<Usuario> usuarios = org.example.client.mock.ServidorMock.obtenerUsuariosVisibles();
        chatUI.setUsuarios(usuarios);
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteUI().setVisible(true));
    }
}
