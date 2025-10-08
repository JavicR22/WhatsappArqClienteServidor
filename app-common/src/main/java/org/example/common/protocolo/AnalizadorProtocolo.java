package org.example.common.protocolo;

import com.google.gson.*;
import org.example.common.entidades.Mensaje;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * AnalizadorProtocolo
 * -------------------
 * Se encarga de serializar y deserializar los objetos de tipo Mensaje
 * entre cliente y servidor utilizando JSON.
 *
 * Usa GSON con adaptadores especiales para:
 *  - LocalDateTime (sin usar reflexión interna, compatible con Java 17+)
 *  - Mensajes polimórficos (a través de MensajeAdapterFactory)
 */
public class AnalizadorProtocolo {

    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();

        // ✅ Adaptadores seguros para LocalDateTime (sin acceso a campos internos)
        builder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                // Formato ISO 8601: 2025-10-06T18:45:12
                return new JsonPrimitive(src.toString());
            }
        });

        builder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return LocalDateTime.parse(json.getAsString());
            }
        });

        // 🔹 Adaptador polimórfico para toda la jerarquía de Mensaje
        builder.registerTypeHierarchyAdapter(Mensaje.class, new MensajeAdapterFactory());


        gson = builder.create();
    }

    /**
     * Serializa un objeto Mensaje a JSON y devuelve sus bytes.
     */
    public static byte[] serializar(Mensaje mensaje) {
        String json = gson.toJson(mensaje);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Deserializa un arreglo de bytes (UTF-8) en un objeto Mensaje.
     */
    public static Mensaje deserializar(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        return gson.fromJson(json, Mensaje.class);
    }

    /**
     * Serializa un Mensaje directamente a cadena JSON.
     */
    public static String serializarAString(Mensaje mensaje) {
        return gson.toJson(mensaje);
    }

    /**
     * Deserializa un JSON String en un Mensaje.
     */
    public static Mensaje deserializarDesdeString(String json) {
        return gson.fromJson(json, Mensaje.class);
    }
}
