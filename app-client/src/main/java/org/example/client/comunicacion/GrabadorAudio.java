package org.example.client.comunicacion;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Clase para grabar audio desde el micrófono
 * Permite grabar, pausar, continuar y cancelar la grabación
 */
public class GrabadorAudio {
    private TargetDataLine lineaEntrada;
    private AudioFormat formato;
    private ByteArrayOutputStream streamSalida;
    private Thread hiloGrabacion;
    private volatile boolean grabando = false;
    private volatile boolean pausado = false;
    private volatile boolean cancelado = false;
    private long duracionMilisegundos = 0;
    private long tiempoInicio = 0;
    private long tiempoPausaAcumulado = 0;
    private long tiempoUltimaPausa = 0;

    public GrabadorAudio() {
        // Formato de audio: 16 kHz, 16 bits, mono
        formato = new AudioFormat(16000, 16, 1, true, false);
    }

    /**
     * Inicia la grabación de audio
     */
    public void iniciarGrabacion() throws LineUnavailableException {
        if (grabando) return;

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, formato);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Formato de audio no soportado");
        }

        lineaEntrada = (TargetDataLine) AudioSystem.getLine(info);
        lineaEntrada.open(formato);
        lineaEntrada.start();

        streamSalida = new ByteArrayOutputStream();
        grabando = true;
        pausado = false;
        cancelado = false;
        tiempoInicio = System.currentTimeMillis();
        tiempoPausaAcumulado = 0;

        // Hilo para capturar audio
        hiloGrabacion = new Thread(() -> {
            byte[] buffer = new byte[4096];

            while (grabando && !cancelado) {
                if (!pausado) {
                    int bytesLeidos = lineaEntrada.read(buffer, 0, buffer.length);
                    if (bytesLeidos > 0) {
                        streamSalida.write(buffer, 0, bytesLeidos);
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });

        hiloGrabacion.start();
        System.out.println("[v0] Grabación iniciada");
    }

    /**
     * Pausa la grabación
     */
    public void pausarGrabacion() {
        if (!grabando || pausado) return;

        pausado = true;
        tiempoUltimaPausa = System.currentTimeMillis();
        System.out.println("[v0] Grabación pausada");
    }

    /**
     * Continúa la grabación después de una pausa
     */
    public void continuarGrabacion() {
        if (!grabando || !pausado) return;

        pausado = false;
        tiempoPausaAcumulado += (System.currentTimeMillis() - tiempoUltimaPausa);
        System.out.println("[v0] Grabación continuada");
    }

    /**
     * Cancela la grabación y descarta el audio
     */
    public void cancelarGrabacion() {
        if (!grabando) return;

        cancelado = true;
        detenerGrabacion();
        streamSalida = null;
        System.out.println("[v0] Grabación cancelada");
    }

    /**
     * Finaliza la grabación y guarda el audio en un archivo
     */
    public File finalizarGrabacion(String rutaArchivo) throws IOException {
        if (!grabando || !pausado) {
            throw new IllegalStateException("Debe pausar la grabación antes de enviar");
        }

        detenerGrabacion();

        if (cancelado || streamSalida == null) {
            return null;
        }

        // Calcular duración real (sin pausas)
        duracionMilisegundos = System.currentTimeMillis() - tiempoInicio - tiempoPausaAcumulado;

        // Guardar audio en archivo WAV
        byte[] audioData = streamSalida.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(bais, formato, audioData.length / formato.getFrameSize());

        File archivoAudio = new File(rutaArchivo);
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, archivoAudio);

        System.out.println("[v0] Grabación finalizada: " + archivoAudio.getAbsolutePath() +
                " (" + (duracionMilisegundos / 1000) + " segundos)");

        return archivoAudio;
    }

    /**
     * Detiene la grabación internamente
     */
    private void detenerGrabacion() {
        grabando = false;

        if (hiloGrabacion != null) {
            try {
                hiloGrabacion.join(1000);
            } catch (InterruptedException e) {
                hiloGrabacion.interrupt();
            }
        }

        if (lineaEntrada != null) {
            lineaEntrada.stop();
            lineaEntrada.close();
        }
    }

    // Getters
    public boolean estaGrabando() {
        return grabando && !pausado;
    }

    public boolean estaPausado() {
        return pausado;
    }

    public long getDuracionSegundos() {
        if (grabando) {
            long tiempoActual = pausado ? tiempoUltimaPausa : System.currentTimeMillis();
            return (tiempoActual - tiempoInicio - tiempoPausaAcumulado) / 1000;
        }
        return duracionMilisegundos / 1000;
    }
}
