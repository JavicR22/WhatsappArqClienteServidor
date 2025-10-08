package org.example.common.protocolo;

import com.google.gson.*;
import org.example.common.entidades.Mensaje;
import org.example.common.entidades.MensajeTexto;
import org.example.common.entidades.MensajeAudio;
import org.example.common.entidades.MensajeAutenticacion;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * Adaptador polimórfico para la jerarquía de clases Mensaje.
 * Evita recursión infinita tanto en serialización como en deserialización.
 */
public class MensajeAdapterFactory implements JsonSerializer<Mensaje>, JsonDeserializer<Mensaje> {

    @Override
    public JsonElement serialize(Mensaje src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        obj.addProperty("_tipo", src.getClass().getSimpleName());

        Gson gsonLimpio = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                            throws JsonParseException {
                        return LocalDateTime.parse(json.getAsString());
                    }
                })
                .create();

        JsonElement content = gsonLimpio.toJsonTree(src);
        for (String key : content.getAsJsonObject().keySet()) {
            obj.add(key, content.getAsJsonObject().get(key));
        }

        return obj;
    }

    @Override
    public Mensaje deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        if (!obj.has("_tipo")) {
            System.err.println("⚠️ Mensaje recibido sin campo '_tipo'. Asumiendo tipo MensajeTexto por defecto.");
            return crearGsonLimpio().fromJson(obj, MensajeTexto.class);
        }

        String tipo = obj.get("_tipo").getAsString();
        Gson gsonLimpio = crearGsonLimpio();

        switch (tipo) {
            case "MensajeTexto":
                return gsonLimpio.fromJson(obj, MensajeTexto.class);
            case "MensajeAudio":
                return gsonLimpio.fromJson(obj, MensajeAudio.class);
            case "MensajeAutenticacion":
                return gsonLimpio.fromJson(obj, MensajeAutenticacion.class);
            case "MensajeRespuesta":
                return gsonLimpio.fromJson(obj, org.example.common.entidades.MensajeRespuesta.class);
            default:
                System.err.println("⚠️ Tipo de mensaje desconocido: " + tipo + ". Usando MensajeTexto por defecto.");
                return gsonLimpio.fromJson(obj, MensajeTexto.class);
        }

    }

    private Gson crearGsonLimpio() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                            throws JsonParseException {
                        return LocalDateTime.parse(json.getAsString());
                    }
                })
                .create();
    }
}
