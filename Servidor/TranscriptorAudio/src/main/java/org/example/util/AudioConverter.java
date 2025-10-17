package org.example.util;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioConverter {
    public static File convertToWavForVosk(File inputFile) throws UnsupportedAudioFileException, IOException {
        // Formato de destino para Vosk
        int targetSampleRate = 16000;
        int targetChannels = 1;

        // Crear un nuevo archivo de salida temporal
        File outputFile = File.createTempFile("converted-audio-", ".wav");

        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(inputFile)) {
            AudioFormat sourceFormat = sourceStream.getFormat();

            // Definir el nuevo formato de audio
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    targetSampleRate,
                    16,
                    targetChannels,
                    targetChannels * 2, // Frame size
                    targetSampleRate,
                    false
            );

            // Convertir el audio al nuevo formato
            try (AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream)) {
                // Escribir el audio convertido en el nuevo archivo
                AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, outputFile);
            }
        }
        System.out.println("✅ Audio convertido y guardado en: " + outputFile.getAbsolutePath());
        return outputFile;
    }
}
