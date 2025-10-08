package org.example.server.tcp;

import org.example.common.entidades.*;
import org.example.common.protocolo.AnalizadorProtocolo;
import org.example.common.servicios.NotificationService;
import org.example.server.servicios.AuthServiceImpl;
import org.example.server.servicios.MessageServiceImpl;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientSession implements Runnable, NotificationService.ObservadorNotificacion {

    private final String idSesion;
    private final Socket socket;
    private final ClientRegistry registro;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private final AuthServiceImpl authService;
    private final NotificationService notificationService;
    private final MessageServiceImpl messageService;
    private Usuario usuario;

    public ClientSession(Socket socket, ClientRegistry registro,
                         AuthServiceImpl authService,
                         NotificationService notificationService,
                         MessageServiceImpl messageService) {
        this.idSesion = UUID.randomUUID().toString();
        this.socket = socket;
        this.registro = registro;
        this.authService = authService;
        this.notificationService = notificationService;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        try {
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());
            System.out.println("Cliente conectado (socket): " + idSesion +
                    " desde IP: " + socket.getInetAddress().getHostAddress());

            // --- AUTENTICACIÓN ---
            Mensaje mensaje = recibirMensaje();
            if (!(mensaje instanceof MensajeAutenticacion authMsg)) {
                enviarRespuesta(false, "Se requiere autenticación antes de enviar mensajes.");
                return;
            }

            var opt = authService.login(authMsg.getCorreo(), authMsg.getContrasena());
            if (opt.isEmpty()) {
                enviarRespuesta(false, "Credenciales inválidas.");
                return;
            }

            this.usuario = opt.get();
            if (!registro.registrarSesion(this)) {
                enviarRespuesta(false, "Límite de usuarios alcanzado.");
                return;
            }

            notificationService.registrarObservador(usuario.getId(), this);
            enviarRespuesta(true, "Autenticación exitosa. Bienvenido.", usuario.getId());
            System.out.println("✅ Usuario autenticado: " + usuario.getCorreo());

            // --- LOOP PRINCIPAL ---
            while (true) {
                try {
                    Mensaje m = recibirMensaje();
                    if (m == null) break;
                    System.out.println("💬 Mensaje recibido de [" + usuario.getCorreo() + "]: " + m.getClass().getSimpleName());
                    messageService.enviarMensajeBroadcast(usuario, m);
                } catch (EOFException e) {
                    System.out.println("🔌 Cliente cerró conexión: " + usuario.getCorreo());
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Error en sesión (" + idSesion + "): " + e.getMessage());
        } finally {
            cerrarSesion();
        }
    }

    private Mensaje recibirMensaje() throws IOException {
        int longitud;
        try {
            longitud = entrada.readInt();
        } catch (EOFException e) {
            return null;
        }
        byte[] datos = new byte[longitud];
        entrada.readFully(datos);
        return AnalizadorProtocolo.deserializar(datos);
    }

    private void cerrarSesion() {
        try {
            if (usuario != null) {
                registro.eliminarSesion(idSesion);
                notificationService.eliminarObservador(usuario.getId());
                authService.logout(usuario.getId());
                System.out.println("🧹 Sesión limpiada para usuario: " + usuario.getCorreo());
            }
            socket.close();
            System.out.println("🔒 Socket cerrado para sesión: " + idSesion);
        } catch (IOException ex) {
            System.err.println("Error cerrando socket: " + ex.getMessage());
        }
    }

    private void enviarRespuesta(boolean exito, String texto, String usuarioId) throws IOException {
        MensajeRespuesta resp = new MensajeRespuesta(UUID.randomUUID().toString(), null, exito, texto, usuarioId);
        byte[] bytes = AnalizadorProtocolo.serializar(resp);
        salida.writeInt(bytes.length);
        salida.write(bytes);
        salida.flush();
    }

    private void enviarRespuesta(boolean exito, String texto) throws IOException {
        enviarRespuesta(exito, texto, usuario != null ? usuario.getId() : null);
    }

    @Override
    public void onMensajeNuevo(Mensaje mensaje) {
        try {
            enviarMensaje(mensaje);
        } catch (IOException e) {
            System.err.println("Error enviando notificación a cliente: " + e.getMessage());
        }
    }

    @Override public void onUsuarioConectado(Usuario usuario) {}
    @Override public void onUsuarioDesconectado(Usuario usuario) {}

    public String getIdSesion() {
        return idSesion;
    }

    public Socket getSocket() {
        return socket;
    }

    public ClientRegistry getRegistro() {
        return registro;
    }

    public DataInputStream getEntrada() {
        return entrada;
    }

    public void setEntrada(DataInputStream entrada) {
        this.entrada = entrada;
    }

    public DataOutputStream getSalida() {
        return salida;
    }

    public void setSalida(DataOutputStream salida) {
        this.salida = salida;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public AuthServiceImpl getAuthService() {
        return authService;
    }

    public MessageServiceImpl getMessageService() {
        return messageService;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void enviarMensaje(Mensaje mensaje) throws IOException {
        byte[] bytes = AnalizadorProtocolo.serializar(mensaje);
        salida.writeInt(bytes.length);
        salida.write(bytes);
        salida.flush();
    }
}
