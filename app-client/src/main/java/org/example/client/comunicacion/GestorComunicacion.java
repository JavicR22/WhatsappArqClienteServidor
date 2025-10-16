package org.example.client.comunicacion;

import org.example.client.modelo.*;
import org.example.client.mock.ServidorMock;
import org.example.client.protocolo.AnalizadorProtocolo;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GestorComunicacion actualizado:
 * - Soporta modo mock (igual que antes).
 * - Soporta conexión a servidor real por TCP con el protocolo correcto.
 */
public class GestorComunicacion {

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private PrintWriter output;
    private boolean conectado;
    private boolean modoMock = false;
    private String usuarioIdConectado = null;
    private Thread hiloEscucha;
    private MensajeListener mensajeListener;
    private String fotoUsuarioActual = null;

    // Para compatibilidad con mock existente
    private Mensaje ultimaRespuestaMock = null;
    private List<UsuarioConectado> usuariosConectados = new ArrayList<>();


    public interface MensajeListener {
        void onMensajeRecibido(String mensaje);
    }

    public void setMensajeListener(MensajeListener listener) {
        this.mensajeListener = listener;
    }
    public List<UsuarioConectado> getUsuariosConectados() {
        return new ArrayList<>(usuariosConectados);
    }


    public void activarModoMock(boolean activar) {
        this.modoMock = activar;
        System.out.println("🔧 Modo mock: " + (activar ? "ACTIVADO" : "DESACTIVADO"));
    }

    /**
     * Conectar al host/puerto o activar mock.
     */
    public boolean conectar(String host, int puerto) {
        if (modoMock) {
            System.out.println("✅ Conectado al servidor simulado (mock)");
            conectado = true;
            return true;
        }

        try {
            socket = new Socket(host, puerto);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            output = new PrintWriter(socket.getOutputStream(), true);
            conectado = true;
            System.out.println("✅ Conectado al servidor en " + host + ":" + puerto);

            iniciarHiloEscucha();

            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al conectar con el servidor: " + e.getMessage());
            conectado = false;
            return false;
        }
    }

    private void iniciarHiloEscucha() {
        hiloEscucha = new Thread(() -> {
            try {
                String linea;
                while (conectado && (linea = reader.readLine()) != null) {
                    final String mensaje = linea;
                    System.out.println("[v0] Mensaje recibido del servidor: " + mensaje);
                    if (mensaje.startsWith("USUARIOS_CONECTADOS|")) {
                        String data = mensaje.substring("USUARIOS_CONECTADOS|".length());
                        usuariosConectados.clear();
                        String[] pares = data.split(";");
                        for (String p : pares) {
                            if (p.isBlank()) continue;
                            String[] partes = p.split(":");
                            String username = partes[0];
                            String foto = (partes.length > 1) ? partes[1] : "DEFAULT";
                            usuariosConectados.add(new UsuarioConectado(username, foto));
                        }
                        System.out.println("[v1] Lista actualizada de usuarios conectados (" + usuariosConectados.size() + ")");
                        if (mensajeListener != null) {
                            mensajeListener.onMensajeRecibido(mensaje);
                        }
                        continue;
                    }

                    if (mensaje.startsWith("FOTO_AVATAR:")) {
                        String foto = mensaje.substring("FOTO_AVATAR:".length());
                        if (!foto.equals("DEFAULT")) {
                            fotoUsuarioActual = foto;
                            System.out.println("[v0] Foto de avatar recibida (Base64 length: " + foto.length() + ")");
                        } else {
                            fotoUsuarioActual = null;
                            System.out.println("[v0] Usuario sin foto personalizada");
                        }
                        continue;
                    }

                    if (mensaje.startsWith("OK:")) {
                        System.out.println("[v0] Autenticación exitosa: " + mensaje);
                        // Notificar al listener
                        if (mensajeListener != null) {
                            mensajeListener.onMensajeRecibido(mensaje);
                        }
                        continue;
                    }

                    // Notificar al listener si existe
                    if (mensajeListener != null) {
                        mensajeListener.onMensajeRecibido(mensaje);
                    }

                    // formato: MSG|<remitente>|<contenido>
                    if (mensaje.startsWith("MSG|")) {
                        String[] partes = mensaje.split("\\|", 3);
                        String remitenteCorreo = partes.length >= 2 ? partes[1] : "";
                        String contenido = partes.length >= 3 ? partes[2] : "";
                        Usuario remitente = new Usuario("?", remitenteCorreo, remitenteCorreo, "", "");
                        ultimaRespuestaMock = new MensajeTexto(UUID.randomUUID().toString(), remitente, contenido);
                    }
                }
            } catch (IOException e) {
                if (conectado && !e.getMessage().contains("Socket closed")) {
                    System.err.println("Error en hilo de escucha: " + e.getMessage());
                }
            }
        });
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }

    /**
     * Enviar un mensaje al servidor (o al mock).
     * Para MensajeAutenticacion: adapta al protocolo del servidor real.
     */
    public void enviarMensaje(Mensaje mensaje) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor o mock.");
        }

        if (modoMock) {
            // Simular envío directo al servidor mock
            Mensaje respuesta = ServidorMock.procesar(mensaje);
            ultimaRespuestaMock = respuesta;
            return;
        }

        if (mensaje instanceof MensajeAutenticacion ma) {
            // **Protocolo de autenticación del servidor real**
            // 1) Esperar "LOGIN" del servidor (esto se maneja en el hilo de escucha)
            // 2) Enviar credenciales en formato 'correo|contrasena'
            String cred = ma.getCorreo() + "|" + ma.getContrasena();
            output.println(cred);
            output.flush();
            System.out.println("[v0] Credenciales enviadas: " + ma.getCorreo());

            // 3) Esperar respuesta del servidor (USUARIOS_CONECTADOS|...)
            // La respuesta se procesará en el hilo de escucha
            return;
        }

        // MensajeTexto: enviar al servidor
        if (mensaje instanceof MensajeTexto mt) {
            String dest = mt.getDestinatario() == null ? "" : mt.getDestinatario();
            String linea = mt.getContenido();
            output.println(linea);
            output.flush();
            System.out.println("[v0] Mensaje enviado: " + linea);
            return;
        }

        // Otros tipos: enviar línea indicativa
        output.println("UNKNOWN-MSG");
        output.flush();
    }

    /**
     * Recibir mensaje del servidor real o mock.
     * En modo real, devuelve la última respuesta guardada por el hilo de escucha.
     */
    public Mensaje recibirMensaje() throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor o mock.");
        }

        if (modoMock) {
            Mensaje respuesta = ultimaRespuestaMock;
            ultimaRespuestaMock = null;
            return respuesta;
        }

        // Esperamos un poco para dar tiempo al hilo de escucha
        int intentos = 0;
        while (ultimaRespuestaMock == null && intentos < 50) {
            try {
                Thread.sleep(100);
                intentos++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupción mientras se esperaba respuesta");
            }
        }

        if (ultimaRespuestaMock != null) {
            Mensaje tmp = ultimaRespuestaMock;
            ultimaRespuestaMock = null;
            return tmp;
        }

        throw new IOException("Timeout esperando respuesta del servidor");
    }

    public boolean procesarRespuestaAutenticacion(String respuesta, String username) {
        if (respuesta == null) {
            System.err.println("❌ No se recibió respuesta del servidor.");
            return false;
        }

        // Validar formato: USUARIOS_CONECTADOS|user1:foto1;user2:foto2;...
        if (!respuesta.startsWith("USUARIOS_CONECTADOS|")) {
            System.err.println("❌ Respuesta inesperada del servidor: " + respuesta);
            return false;
        }

        // Buscar el usuario en la lista
        Pattern patron = Pattern.compile("(?:\\||;)" + Pattern.quote(username) + ":");
        Matcher matcher = patron.matcher(respuesta);

        if (matcher.find()) {
            System.out.println("✅ Sesión iniciada correctamente para el usuario: " + username);
            usuarioIdConectado = username;

            // Crear MensajeRespuesta exitoso
            Usuario remitente = new Usuario(username, username, username, "", "");
            if (fotoUsuarioActual != null) {
                remitente.setFotoBase64(fotoUsuarioActual);
            }
            ultimaRespuestaMock = new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    remitente,
                    true,
                    "Autenticación exitosa"
            );
            return true;
        } else {
            System.err.println("❌ El usuario '" + username + "' no aparece en la lista enviada: " + respuesta);
            Usuario remitente = new Usuario(username, username, username, "", "");
            ultimaRespuestaMock = new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    remitente,
                    false,
                    "Usuario no encontrado en la respuesta del servidor"
            );
            return false;
        }
    }

    /**
     * Cierra la conexión o termina el mock.
     */
    public void cerrarConexion() {
        if (modoMock) {
            conectado = false;
            System.out.println("👋 Conexión mock cerrada correctamente");
            return;
        }

        try {
            conectado = false;
            if (hiloEscucha != null) {
                hiloEscucha.interrupt();
            }
            if (output != null) output.close();
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            System.out.println("👋 Conexión cerrada correctamente");
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    public boolean estaConectado() {
        return conectado;
    }

    public String getUsuarioIdConectado() {
        return usuarioIdConectado;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public String getFotoUsuarioActual() {
        return fotoUsuarioActual;
    }
}
