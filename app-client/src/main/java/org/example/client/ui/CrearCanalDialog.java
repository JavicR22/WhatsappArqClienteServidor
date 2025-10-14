package org.example.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para crear un nuevo canal
 */
public class CrearCanalDialog extends JDialog {

    private JTextField tfNombre;
    private JTextArea taDescripcion;
    private JCheckBox cbPrivado;
    private boolean confirmado = false;

    public CrearCanalDialog(Frame parent) {
        super(parent, "Crear Nuevo Canal", true);
        initComponents();
        setSize(400, 300);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel de formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre del canal
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panelFormulario.add(new JLabel("Nombre:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        tfNombre = new JTextField(20);
        panelFormulario.add(tfNombre, gbc);

        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelFormulario.add(new JLabel("Descripción:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        taDescripcion = new JTextArea(5, 20);
        taDescripcion.setLineWrap(true);
        taDescripcion.setWrapStyleWord(true);
        panelFormulario.add(new JScrollPane(taDescripcion), gbc);

        // Checkbox privado
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cbPrivado = new JCheckBox("Canal Privado (requiere invitación)");
        cbPrivado.setSelected(true);
        panelFormulario.add(cbPrivado, gbc);

        add(panelFormulario, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCrear = new JButton("Crear");
        JButton btnCancelar = new JButton("Cancelar");

        btnCrear.addActionListener(e -> {
            if (validarFormulario()) {
                confirmado = true;
                dispose();
            }
        });

        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });

        panelBotones.add(btnCancelar);
        panelBotones.add(btnCrear);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private boolean validarFormulario() {
        if (tfNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El nombre del canal es obligatorio",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getNombreCanal() {
        return tfNombre.getText().trim();
    }

    public String getDescripcion() {
        return taDescripcion.getText().trim();
    }

    public boolean isPrivado() {
        return cbPrivado.isSelected();
    }
}
