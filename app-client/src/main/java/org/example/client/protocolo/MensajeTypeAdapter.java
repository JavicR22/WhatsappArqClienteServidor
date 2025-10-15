package org.example.client.protocolo;

import com.google.gson.*;
import org.example.client.modelo.*;

import java.lang.reflect.Type;

/**
 * PROTOCOLO DE COMUNICACIÓN
 * TypeAdapter personalizado para deserializar mensajes polimórficos.
 * Lee el campo _tipo del JSON y crea la instancia concreta correcta.
 */
public class MensajeTypeAdapter implements JsonDeserializer<Mensaje>, JsonSerializer<Mensaje> {

    @Override
    public Mensaje deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Leer el campo _tipo para determinar qué clase instanciar
        String tipo = jsonObject.has("_tipo")
                ? jsonObject.get("_tipo").getAsString()
                : "MensajeTexto"; // Default

        // Deserializar a la clase concreta correcta
        // dentro del switch (tipo) { ... }
        switch (tipo) {
            case "MensajeAutenticacion":
                return context.deserialize(json, MensajeAutenticacion.class);
            case "MensajeTexto":
                return context.deserialize(json, MensajeTexto.class);
            case "MensajeRespuesta":
                return context.deserialize(json, MensajeRespuesta.class);
            case "MensajeAudio": // NUEVO
                return context.deserialize(json, MensajeAudio.class);
            case "MensajeCrearCanal": // NUEVO
                return context.deserialize(json, MensajeCrearCanal.class);
            case "MensajeSolicitudUnion": // NUEVO
                return context.deserialize(json, MensajeSolicitudUnion.class);
            default:
                throw new JsonParseException("Tipo de mensaje desconocido: " + tipo);
        }

    }

    @Override
    public JsonElement serialize(Mensaje src, Type typeOfSrc, JsonSerializationContext context) {
        // Serializar normalmente, Gson manejará los campos automáticamente
        return context.serialize(src, src.getClass());
    }
}
