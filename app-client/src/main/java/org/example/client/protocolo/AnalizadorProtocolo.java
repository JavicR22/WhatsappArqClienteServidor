package org.example.client.protocolo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.client.modelo.Mensaje;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * PROTOCOLO DE COMUNICACIÓN
 * Responsable de serializar y deserializar mensajes usando JSON.
 * Utiliza Gson para la conversión entre objetos Java y JSON.
 */
public class AnalizadorProtocolo {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Mensaje.class, new MensajeTypeAdapter())
            .create();

    /**
     * Serializa un mensaje a bytes usando JSON
     */
    public static byte[] serializar(Mensaje mensaje) {
        String json = gson.toJson(mensaje);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Deserializa bytes a un objeto Mensaje usando JSON
     */
    public static Mensaje deserializar(byte[] datos) {
        String json = new String(datos, StandardCharsets.UTF_8);
        return gson.fromJson(json, Mensaje.class);
    }
}
