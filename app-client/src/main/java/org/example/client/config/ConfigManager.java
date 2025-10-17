package org.example.client.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private final Properties properties = new Properties();

    public ConfigManager(String nombreArchivo) {
        try (InputStream external = new FileInputStream(nombreArchivo)) {
            properties.load(external);
            System.out.println("Configuración cargada desde archivo externo: " + nombreArchivo);
        } catch (IOException e) {
            // Si no existe externo, intenta cargar desde dentro del JAR
            try (InputStream internal = getClass().getClassLoader().getResourceAsStream(nombreArchivo)) {
                if (internal == null) {
                    throw new RuntimeException("No se encontró el archivo de configuración: " + nombreArchivo);
                }
                properties.load(internal);
                System.out.println("Configuración cargada desde recursos internos.");
            } catch (IOException ex) {
                throw new RuntimeException("Error cargando configuración: " + nombreArchivo, ex);
            }
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager("client-config.properties");
        }
        return instance;
    }

    public static String get(String key, String defaultValue) {
        try {
            String value = getInstance().get(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getInt(String key, int defaultValue) {
        try {
            String value = getInstance().get(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        try {
            String value = getInstance().get(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
