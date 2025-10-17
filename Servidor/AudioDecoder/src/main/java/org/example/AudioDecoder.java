package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class AudioDecoder {
    public static File decodeBase64ToWav(String base64Audio, String nombreArchivo) throws IOException {
        byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
        File tempFile = new File(System.getProperty("java.io.tmpdir"), nombreArchivo + ".wav");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(audioBytes);
        }

        return tempFile;
    }
}
