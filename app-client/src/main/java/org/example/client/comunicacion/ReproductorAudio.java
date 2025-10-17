package org.example.client.comunicacion;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class ReproductorAudio {
    private Clip clip;

    public void cargar(File archivo) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(archivo);
        clip = AudioSystem.getClip();
        clip.open(stream);
    }

    public void reproducir() {
        if (clip != null) {
            clip.start();
        }
    }

    public void pausar() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void detener() {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public boolean estaReproduciendo() {
        return clip != null && clip.isRunning();
    }

    public void cerrar() {
        if (clip != null) {
            clip.close();
        }
    }
}
