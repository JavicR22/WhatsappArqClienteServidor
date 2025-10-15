package org.example;

import org.example.controladores.ServidorControlador;
import org.example.controladores.UsuarioControlador;
import org.example.repositorio.implementacion.UsuarioRepositorioImpl;
import org.example.servicio.impl.UsuarioServiceImpl;
import org.example.view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UsuarioRepositorioImpl usuarioRepositorio = new UsuarioRepositorioImpl();
        UsuarioServiceImpl usuarioService = new UsuarioServiceImpl(usuarioRepositorio);
        ServidorControlador servidorControlador = new ServidorControlador(usuarioService);
        UsuarioControlador usuarioControlador = new UsuarioControlador(usuarioService);
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(servidorControlador, usuarioControlador);
            frame.setVisible(true);
        });
    }
}