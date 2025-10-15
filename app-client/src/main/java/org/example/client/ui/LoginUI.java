package org.example.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Pantalla de inicio de sesión (estructura similar a la imagen de Login.jpeg)
 */
public class LoginUI extends JPanel {

    private JTextField tfUsuario;
    private JPasswordField pfContrasena;
    private JButton btnLogin;
    private JButton btnRegistro;
    private JButton btnOlvido;

    public LoginUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("WELCOME", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 28));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitulo, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel("Username:"), gbc);
        tfUsuario = new JTextField(15);
        gbc.gridx = 1;
        add(tfUsuario, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        pfContrasena = new JPasswordField(15);
        gbc.gridx = 1;
        add(pfContrasena, gbc);

        // Forgot password
        gbc.gridy++;
        gbc.gridx = 1;
        btnOlvido = new JButton("Forgot Password?");
        add(btnOlvido, gbc);

        // Login
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        btnLogin = new JButton("LOGIN");
        add(btnLogin, gbc);

        // Registro
        gbc.gridy++;
        btnRegistro = new JButton("Click here to Register");
        add(btnRegistro, gbc);
    }

    // Getters para los controladores
    public String getUsuario() { return tfUsuario.getText().trim(); }
    public String getContrasena() { return new String(pfContrasena.getPassword()); }

    public void addLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
    }

    public void addRegistroListener(ActionListener listener) {
        btnRegistro.addActionListener(listener);
    }
}
