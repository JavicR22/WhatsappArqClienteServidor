package org.example.presentacion;

import org.example.controladores.ServidorControlador;
import org.example.controladores.UsuarioControlador;
import org.example.eventos.MensajeriaDispatcher;
import org.example.repositorio.*;
import org.example.repositorio.implementacion.*;
import org.example.servicio.CanalService;
import org.example.servicio.MensajeriaService;
import org.example.servicio.impl.CanalServiceImpl;
import org.example.servicio.impl.MensajeriaServiceImpl;
import org.example.servicio.impl.UsuarioServiceImpl;
import org.example.presentacion.view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        UsuarioRepositorioImpl usuarioRepositorio = new UsuarioRepositorioImpl();
        UsuarioServiceImpl usuarioService = new UsuarioServiceImpl(usuarioRepositorio);
        CanalRepositorio canalRepositorio = new CanalRepositorioImpl();
        MensajeTextoCanalRepositorio mensajeTextoCanalRepositorio = new MensajeTextoCanalRepositorioImpl();
        MensajeAudioCanalRepositorio mensajeAudioCanalRepositorio = new MensajeAudioCanalRepositorioImpl();
        MensajeAudioPrivadoRepositorio mensajeAudioPrivadoRepositorio = new MensajeAudioPrivadoRepositorioImpl();
        MensajeTextoPrivadoRepositorio mensajeTextoPrivado = new MensajeTextoPrivadoRepositorioImpl();
        // ✅ Nuevo: crear dispatcher
        MensajeriaDispatcher dispatcher = new MensajeriaDispatcher();
        MensajeriaService mensajeriaService = new MensajeriaServiceImpl(mensajeTextoPrivado,mensajeTextoCanalRepositorio,
                mensajeAudioPrivadoRepositorio,
                mensajeAudioCanalRepositorio,usuarioRepositorio,canalRepositorio,dispatcher);

        // ✅ Pasar dispatcher al controlador
        CanalService canalService = new CanalServiceImpl(canalRepositorio,dispatcher, usuarioService);
        ServidorControlador servidorControlador = new ServidorControlador(usuarioService, dispatcher,mensajeriaService,canalService);
        UsuarioControlador usuarioControlador = new UsuarioControlador(usuarioService);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(servidorControlador, usuarioControlador);
            frame.setVisible(true);
        });
    }
}
