package org.example.server.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class MainServerUI extends Application {

    private TextArea taLogs;
    private Button btnToggle;
    private Process serverProcess;

    @Override
    public void start(Stage stage) {
        taLogs = new TextArea();
        taLogs.setEditable(false);
        taLogs.setWrapText(true);

        btnToggle = new Button("Iniciar servidor");
        btnToggle.setOnAction(e -> {
            if (serverProcess == null || !serverProcess.isAlive()) startServerProcess();
            else stopServerProcess();
        });

        VBox root = new VBox(8, btnToggle, new Separator(), taLogs);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 800, 500);
        stage.setScene(scene);
        stage.setTitle("Servidor - Control");
        stage.show();

        log("Interfaz de control servidor lista.");
    }

    private void startServerProcess() {
        try {
            // Asume que el jar del servidor se encuentra en el mismo directorio
            String jarName = "app-server-1.0-SNAPSHOT-jar-with-dependencies.jar";
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarName);
            pb.redirectErrorStream(true);
            serverProcess = pb.start();

            btnToggle.setText("Apagar servidor");
            log("Servidor iniciado (proceso). PID: " + serverProcess.pid());

            // Leer la salida y volcar a TextArea
            InputStream is = serverProcess.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            Executors.newSingleThreadExecutor().submit(() -> {
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        log(line);
                    }
                } catch (IOException e) {
                    log("Error leyendo salida del servidor: " + e.getMessage());
                }
            });

            // Opcional: vigilar terminación del proceso para actualizar UI
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    int exit = serverProcess.waitFor();
                    log("Proceso servidor finalizó con código: " + exit);
                    Platform.runLater(() -> btnToggle.setText("Iniciar servidor"));
                } catch (InterruptedException e) {
                    log("Watcher interrumpido.");
                }
            });

        } catch (IOException e) {
            log("❌ No se pudo iniciar el servidor: " + e.getMessage());
        }
    }

    private void stopServerProcess() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy(); // intento educado
            try {
                boolean exited = serverProcess.waitFor(5_000, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (!exited) {
                    serverProcess.destroyForcibly();
                }
                log("Servidor detenido.");
                btnToggle.setText("Iniciar servidor");
            } catch (InterruptedException e) {
                log("Error deteniendo servidor: " + e.getMessage());
            }
        } else {
            log("Servidor no está corriendo.");
            btnToggle.setText("Iniciar servidor");
        }
    }

    private void log(String t) {
        Platform.runLater(() -> taLogs.appendText(t + "\n"));
    }

    @Override
    public void stop() throws Exception {
        stopServerProcess();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
