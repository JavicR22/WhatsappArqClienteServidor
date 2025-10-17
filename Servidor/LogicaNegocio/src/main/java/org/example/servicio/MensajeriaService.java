package org.example.servicio;

import org.example.entidades.*;

public interface MensajeriaService {
    void enviarMensajeTextoPrivado(MensajeTextoPrivado mensaje) throws Exception;

    /**
     * Procesa un mensaje de audio privado: guarda la referencia en DB y notifica al receptor.
     * @param mensaje El objeto MensajeAudioPrivado a procesar.
     * @throws Exception si falla la persistencia o alguna validación.
     */
    void enviarMensajeAudioPrivado(String mensaje, String receptor, String emisor) throws Exception;

    /**
     * Procesa un mensaje de texto de canal: lo guarda en DB y lo notifica a todos los miembros del canal.
     * @param mensaje El objeto MensajeTextoCanal a procesar.
     * @throws Exception si falla la persistencia o alguna validación.
     */
    void enviarMensajeTextoCanal(MensajeTextoCanal mensaje) throws Exception;

    /**
     * Procesa un mensaje de audio de canal: guarda la referencia en DB y notifica a todos los miembros.
     * @param mensaje El objeto MensajeAudioCanal a procesar.
     * @throws Exception si falla la persistencia o alguna validación.
     */
    void enviarMensajeAudioCanal(MensajeAudioCanal mensaje) throws Exception;
    void guardarMensajeAudioPrivado(MensajeAudioPrivado mensajeAudioPrivado);
    void guardarCanal(Canal canal);
}
