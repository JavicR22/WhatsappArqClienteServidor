package org.example.persistencia.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ApplicationProperties.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                throw new RuntimeException("No se encontró el archivo application.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error cargando properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
