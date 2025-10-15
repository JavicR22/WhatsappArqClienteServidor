package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FileBase64Encoder {
    public static String encodeFileToBase64(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            System.err.println("❌ Archivo no encontrado en la ruta: " + filePath);
            return null;
        }

        try {
            // Lee todos los bytes del archivo
            byte[] fileContent = Files.readAllBytes(path);

            // Codifica los bytes usando el encoder Basic de Base64
            return Base64.getEncoder().encodeToString(fileContent);

        } catch (IOException e) {
            System.err.println("Error de lectura al codificar el archivo a Base64: " + e.getMessage());
            return null;
        }
    }
}
