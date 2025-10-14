package org.example.client.ui;

import org.example.client.controladores.CanalController;
import org.example.client.datos.RepositorioLocal;
import org.example.client.modelo.Canal;
import org.example.client.modelo.Solicitud;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Panel para mostrar y gestionar solicitudes de invitación a canales
 */
public class SolicitudesPanel extends JPanel {

    private CanalController canalController;
    private RepositorioLocal repositorioLocal;
    private DefaultListModel<String> modeloSolicitudes;
    private JList<String> listaSolicitudes;
    private List<Solicitud> solicitudes;

    public SolicitudesPanel(CanalController canalController, RepositorioLocal repositorioLocal) {
        this.canalController = canalController;
        this.repositorioLocal = repositorioLocal;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Invitaciones Pendientes");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitulo, BorderLayout.NORTH);

        // Lista de solicitudes
        modeloSolicitudes = new DefaultListModel<>();
        listaSolicitudes = new JList<>(modeloSolicitudes);
        listaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listaSolicitudes);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnAceptar = new JButton("✓ Aceptar");
        JButton btnRechazar = new JButton("✗ Rechazar");
        JButton btnActualizar = new JButton("🔄 Actualizar");

        btnAceptar.addActionListener(e -> aceptarSolicitud());
        btnRechazar.addActionListener(e -> rechazarSolicitud());
        btnActualizar.addActionListener(e -> cargarSolicitudes());

        panelBotones.add(btnAceptar);
        panelBotones.add(btnRechazar);
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        // Cargar solicitudes iniciales
        cargarSolicitudes();
    }

    public void cargarSolicitudes() {
        modeloSolicitudes.clear();
        solicitudes = canalController.obtenerSolicitudesPendientes();

        if (solicitudes.isEmpty()) {
            modeloSolicitudes.addElement("No tienes invitaciones pendientes");
        } else {
            for (Solicitud s : solicitudes) {
                Optional<Canal> canalOpt = repositorioLocal.buscarCanalPorId(s.getIdCanal());
                String nombreCanal = canalOpt.map(Canal::getNombre).orElse("Canal desconocido");
                modeloSolicitudes.addElement("Invitación al canal: " + nombreCanal);
            }
        }
    }

    private void aceptarSolicitud() {
        int indice = listaSolicitudes.getSelectedIndex();
        if (indice < 0 || solicitudes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una invitación",
                    "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Solicitud solicitud = solicitudes.get(indice);
        boolean exito = canalController.aceptarInvitacion(solicitud.getId(), solicitud.getIdCanal());

        if (exito) {
            JOptionPane.showMessageDialog(this,
                    "Invitación aceptada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarSolicitudes();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al aceptar la invitación",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechazarSolicitud() {
        int indice = listaSolicitudes.getSelectedIndex();
        if (indice < 0 || solicitudes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una invitación",
                    "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Solicitud solicitud = solicitudes.get(indice);
        boolean exito = canalController.rechazarInvitacion(solicitud.getId(), solicitud.getIdCanal());

        if (exito) {
            JOptionPane.showMessageDialog(this,
                    "Invitación rechazada",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarSolicitudes();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al rechazar la invitación",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
