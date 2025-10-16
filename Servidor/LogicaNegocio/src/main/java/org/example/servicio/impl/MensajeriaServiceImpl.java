package org.example.servicio.impl;

import org.example.entidades.*;

import org.example.eventos.MensajeriaDispatcher;
import org.example.repositorio.*;
import org.example.servicio.MensajeriaService;

import java.util.Optional;

public class MensajeriaServiceImpl implements MensajeriaService {
    // 📥 Inyección de dependencias de Repositorios
    private final MensajeTextoPrivadoRepositorio textoPrivadoRepo;
    private final MensajeTextoCanalRepositorio textoCanalRepo;
    private final MensajeAudioPrivadoRepositorio audioPrivadoRepo;
    private final MensajeAudioCanalRepositorio audioCanalRepo;
    private final UsuarioRepositorio usuarioRepositorio;
    private final CanalRepositorio canalRepositorio;
    private final MensajeriaDispatcher dispatcher; // <<--- Nuevo

    public MensajeriaServiceImpl(
            MensajeTextoPrivadoRepositorio textoPrivadoRepo,
            MensajeTextoCanalRepositorio textoCanalRepo,
            MensajeAudioPrivadoRepositorio audioPrivadoRepo,
            MensajeAudioCanalRepositorio audioCanalRepo,
            UsuarioRepositorio usuarioRepositorio,
            CanalRepositorio canalRepositorio,
            MensajeriaDispatcher dispatcher)
    {
        this.textoPrivadoRepo = textoPrivadoRepo;
        this.textoCanalRepo = textoCanalRepo;
        this.audioPrivadoRepo = audioPrivadoRepo;
        this.audioCanalRepo = audioCanalRepo;
        this.usuarioRepositorio = usuarioRepositorio;
        this.canalRepositorio = canalRepositorio;
        this.dispatcher = dispatcher;
    }

    @Override
    public void enviarMensajeAudioPrivado(MensajeAudioPrivado mensaje) throws Exception {

    }

    @Override
    public void enviarMensajeAudioCanal(MensajeAudioCanal mensaje) throws Exception {

    }

    @Override
    public void enviarMensajeTextoPrivado(MensajeTextoPrivado mensaje) throws Exception {
        Usuario receptor = mensaje.getReceptor();
        System.out.println(receptor.getUsername());
        if (usuarioRepositorio.buscarPorUsername(receptor.getUsername()).isEmpty())
            throw new Exception("El receptor '" + receptor.getUsername() + "' no existe.");

        textoPrivadoRepo.guardar(mensaje);

        String protocolo = String.format("MSG_TEXT_PRIVADO|%s|%s",
                mensaje.getEmisor().getUsername(),
                mensaje.getContenidoTexto());

        dispatcher.notificarMensajePrivado(receptor.getUsername(), protocolo);
    }

    @Override
    public void enviarMensajeTextoCanal(MensajeTextoCanal mensaje) throws Exception {
        Canal canal = mensaje.getCanal();
        if (canalRepositorio.buscarPorId(canal.getIdCanal()).isEmpty())
            throw new Exception("El canal no existe.");

        textoCanalRepo.guardar(mensaje);

        String protocolo = String.format("MSG_TEXT_CANAL|%s|%s|%s",
                canal.getNombreCanal(),
                mensaje.getEmisor().getUsername(),
                mensaje.getContenido());

        dispatcher.notificarMensajeCanal(String.valueOf(canal.getIdCanal()), protocolo);
    }
}
