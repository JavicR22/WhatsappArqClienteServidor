package org.example.server.ui;

import org.example.server.EmbeddedServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class MainServerUI extends JFrame {

    private JTextArea taLogs;
    private JButton btnToggle;
    private Process serverProcess;

    public MainServerUI() {
        setTitle("Servidor - Control");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        taLogs = new JTextArea();
        taLogs.setEditable(false);
        taLogs.setLineWrap(true);
        taLogs.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(taLogs);
        add(scroll, BorderLayout.CENTER);

        btnToggle = new JButton("Iniciar servidor");
        btnToggle.addActionListener(this::onToggleServer);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnToggle);
        add(top, BorderLayout.NORTH);

        log("Interfaz de control servidor lista.");
    }

    private void onToggleServer(ActionEvent e) {
        if (serverProcess == null || !serverProcess.isAlive()) {
            startServerProcess();
        } else {
            stopServerProcess();
        }
    }



    private void startServerProcess() {
        if (EmbeddedServer.isRunning()) {
            log("Servidor ya está corriendo.");
            return;
        }
        EmbeddedServer.start();
        btnToggle.setText("Apagar servidor");
        log("Servidor iniciado (embebido).");
    }

    private void stopServerProcess() {
        if (!EmbeddedServer.isRunning()) {
            log("Servidor no está corriendo.");
        } else {
            EmbeddedServer.stop();
            log("Servidor detenido (solicitado).");
        }
        btnToggle.setText("Iniciar servidor");
    }


    private void log(String text) {
        SwingUtilities.invokeLater(() -> taLogs.append(text + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainServerUI().setVisible(true));
    }
}
