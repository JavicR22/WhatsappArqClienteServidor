package org.example.presentacion.view;

import javax.swing.*;
import java.awt.*;

public class UserFormPanel extends JPanel {

    private final JTextField txtUsername = new JTextField(12);
    private final JTextField txtEmail = new JTextField(12);
    private final JPasswordField txtPassword = new JPasswordField(12);
    private final JTextField txtIP = new JTextField(12);
    private final JLabel lblFoto = new JLabel(); // lugar para foto circular
    private final JButton btnCargarFoto = new JButton("Cargar Foto");

    public UserFormPanel(String title) {
        setBorder(BorderFactory.createTitledBorder(title));
        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(new JLabel("Username:"));
        add(txtUsername);
        add(new JLabel("Email:"));
        add(txtEmail);
        add(new JLabel("Password:"));
        add(txtPassword);
        add(new JLabel("IP:"));
        add(txtIP);

        // foto circular placeholder
        lblFoto.setPreferredSize(new Dimension(48, 48));
        lblFoto.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(lblFoto);
        add(btnCargarFoto);

        // Botón cargar imagen (implementar file chooser cuando lo necesites)
        btnCargarFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int r = chooser.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                java.io.File f = chooser.getSelectedFile();
                ImageIcon icon = ImageUtils.createCircularImageIcon(f.getAbsolutePath(), 48);
                // 3. 🔑 ¡Paso Crucial! Asignar la ruta como descripción del icono
                String rutaFoto = f.getAbsolutePath();
                icon.setDescription(rutaFoto);

                // 4. Asignar el icono al JLabel
                lblFoto.setIcon(icon);
            }
        });
    }

    // getters para leer los valores si necesitas guardarlos
    public String getUsername() { return txtUsername.getText().trim(); }
    public String getEmail() { return txtEmail.getText().trim(); }
    public String getPassword() { return new String(txtPassword.getPassword()); }
    public String getIp() { return txtIP.getText().trim(); }
    public String getRutaFoto() {
        Icon icon = lblFoto.getIcon();

        if (icon != null && icon instanceof ImageIcon) {
            // Devuelve la descripción que usted estableció manualmente
            String ruta = ((ImageIcon) icon).getDescription();

            // El getDescription() puede devolver null, así que es mejor retornar una cadena vacía en ese caso
            return ruta != null ? ruta : "a";
        }
        return "";
    }
}
