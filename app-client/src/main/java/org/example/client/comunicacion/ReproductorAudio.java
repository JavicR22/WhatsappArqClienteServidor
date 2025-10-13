package org.example.client.comunicacion;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class ReproductorAudio {
    private Clip clip;

    public void abrir(String ruta) throws Exception {
        File archivo = new File(ruta);
        AudioInputStream stream = AudioSystem.getAudioInputStream(archivo);
        clip = AudioSystem.getClip();
        clip.open(stream);
    }

    public void reproducir() {
        if (clip != null) {
            clip.start();
        }
    }

    public void cerrar() {
        if (clip != null) {
            clip.close();
        }
    }
}
