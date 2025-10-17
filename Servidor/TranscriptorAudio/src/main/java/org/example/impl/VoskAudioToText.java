package org.example.impl;



import org.example.api.AudioToTextService;
import org.example.util.AudioConverter;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class VoskAudioToText {
    private static Model model;

    static {
        try {
            // Carga el modelo de voz al iniciar la aplicación
            // Asegúrate de que la ruta al modelo es correcta
            model = new Model("vosk-model-es-0.42");
        } catch (Exception e) {
            System.err.println("Error al cargar el modelo de Vosk: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static String transcribe(File audioFile) throws Exception {
        if (model == null) {
            throw new IllegalStateException("El modelo de Vosk no se ha cargado correctamente.");
        }
        File convertedFile = AudioConverter.convertToWavForVosk(audioFile);
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(convertedFile);
             Recognizer recognizer = new Recognizer(model, ais.getFormat().getSampleRate())) {


            // Prepara un buffer para leer los datos del audio
            int nbytes;
            byte[] b = new byte[4096];
            StringBuilder transcript = new StringBuilder();

            // Lee el archivo de audio y lo procesa
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    String result = recognizer.getResult();
                    transcript.append(result);
                }
            }

            // Procesa el resultado final
            String finalResult = recognizer.getFinalResult();
            transcript.append(finalResult);

            // Vosk devuelve el texto en un formato JSON; extraemos solo el texto
            return transcript.toString();
        }
    }
}
