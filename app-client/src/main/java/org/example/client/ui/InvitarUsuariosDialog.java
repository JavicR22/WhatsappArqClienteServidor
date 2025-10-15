package org.example.client.ui;

import org.example.client.modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para invitar usuarios a un canal
 */
public class InvitarUsuariosDialog extends JDialog {

    private JList<String> listaUsuarios;
    private DefaultListModel<String> modeloUsuarios;
    private List<Usuario> usuarios;
    private boolean confirmado = false;

    public InvitarUsuariosDialog(Frame parent, List<Usuario> usuariosDisponibles, String correoActual) {
        super(parent, "Invitar Usuarios al Canal", true);
        this.usuarios = new ArrayList<>();

        // Filtrar el usuario actual
        for (Usuario u : usuariosDisponibles) {
            if (!u.getCorreo().equals(correoActual)) {
                this.usuarios.add(u);
            }
        }

        initComponents();
        setSize(400, 400);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Instrucciones
        JLabel lblInstrucciones = new JLabel("Selecciona los usuarios que deseas invitar:");
        lblInstrucciones.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(lblInstrucciones, BorderLayout.NORTH);

        // Lista de usuarios
        modeloUsuarios = new DefaultListModel<>();
        for (Usuario u : usuarios) {
            modeloUsuarios.addElement(u.getNombre() + " (" + u.getCorreo() + ") - " + u.getRol());
        }

        listaUsuarios = new JList<>(modeloUsuarios);
        listaUsuarios.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listaUsuarios);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnInvitar = new JButton("Invitar");
        JButton btnCancelar = new JButton("Cancelar");

        btnInvitar.addActionListener(e -> {
            if (listaUsuarios.getSelectedIndices().length == 0) {
                JOptionPane.showMessageDialog(this,
                        "Debes seleccionar al menos un usuario",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            confirmado = true;
            dispose();
        });

        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });

        panelBotones.add(btnCancelar);
        panelBotones.add(btnInvitar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public List<String> getCorreosSeleccionados() {
        List<String> correos = new ArrayList<>();
        int[] indices = listaUsuarios.getSelectedIndices();

        for (int i : indices) {
            correos.add(usuarios.get(i).getCorreo());
        }

        return correos;
    }
}
