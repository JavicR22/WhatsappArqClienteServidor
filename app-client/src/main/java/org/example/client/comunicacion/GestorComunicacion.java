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
    private MensajePrivadoListener mensajePrivadoListener;
    private String fotoUsuarioActual;

    // Para compatibilidad con mock existente
    private Mensaje ultimaRespuestaMock = null;
    private List<UsuarioConectado> usuariosConectados = new ArrayList<>();

    private CanalCreadoListener canalCreadoListener;

    public interface MensajeListener {
        void onMensajeRecibido(String mensaje);
    }

    public interface MensajePrivadoListener {
        void onMensajePrivadoRecibido(String emisor, String contenido);
    }

    public interface CanalCreadoListener {
        void onCanalCreado(String idCanal, String nombre, String descripcion, boolean privado, String creadorEmail, long creadoEn);
    }

    public void setMensajeListener(MensajeListener listener) {
        this.mensajeListener = listener;
    }

    public void setMensajePrivadoListener(MensajePrivadoListener listener) {
        this.mensajePrivadoListener = listener;
    }

    public void setCanalCreadoListener(CanalCreadoListener listener) {
        this.canalCreadoListener = listener;
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

                    if (mensaje.startsWith("CANAL_CREADO|")) {
                        String[] partes = mensaje.split("\\|");
                        System.out.println("[v0] Parseando CANAL_CREADO - partes: " + partes.length);

                        // Formato del servidor: CANAL_CREADO|idCanal|nombre|descripcion|privado|usernameCreador
                        if (partes.length >= 6) {
                            String idCanal = partes[1];
                            String nombre = partes[2];
                            String descripcion = partes[3];
                            boolean privado = Boolean.parseBoolean(partes[4]);
                            String creadorEmail = partes[5];
                            long creadoEn = System.currentTimeMillis(); // Usar timestamp actual si no viene del servidor

                            System.out.println("[v0] ✅ Canal parseado correctamente:");
                            System.out.println("    - ID: " + idCanal);
                            System.out.println("    - Nombre: " + nombre);
                            System.out.println("    - Descripción: " + descripcion);
                            System.out.println("    - Privado: " + privado);
                            System.out.println("    - Creador: " + creadorEmail);

                            // Notificar al listener
                            if (canalCreadoListener != null) {
                                System.out.println("[v0] Notificando al listener de CANAL_CREADO...");
                                canalCreadoListener.onCanalCreado(idCanal, nombre, descripcion, privado, creadorEmail, creadoEn);
                            } else {
                                System.err.println("[v0] ⚠️ No hay listener registrado para CANAL_CREADO");
                            }
                        } else {
                            System.err.println("[v0] ❌ Formato incorrecto de CANAL_CREADO. Se esperaban al menos 6 partes, se recibieron: " + partes.length);
                        }
                        continue;
                    }

                    if (mensaje.startsWith("MIS_CANALES|")) {
                        System.out.println("[v0] Lista de canales recibida del servidor");
                        if (mensajeListener != null) {
                            mensajeListener.onMensajeRecibido(mensaje);
                        }
                        continue;
                    }

                    if (mensaje.startsWith("INVITACION_CANAL|")) {
                        System.out.println("[v0] Invitación a canal recibida");
                        if (mensajeListener != null) {
                            mensajeListener.onMensajeRecibido(mensaje);
                        }
                        continue;
                    }

                    if (mensaje.startsWith("MSG_TEXT_PRIVADO|")) {
                        String[] partes = mensaje.split("\\|", 3);
                        if (partes.length >= 3) {
                            String emisor = partes[1];
                            String contenido = partes[2];
                            System.out.println("[v0] Mensaje privado recibido de " + emisor + ": " + contenido);

                            // Notify listener
                            if (mensajePrivadoListener != null) {
                                mensajePrivadoListener.onMensajePrivadoRecibido(emisor, contenido);
                            }
                        }
                        continue;
                    }

                    if (mensaje.startsWith("MSG_TEXT_CANAL|")) {
                        String[] partes = mensaje.split("\\|", 4);
                        if (partes.length >= 4) {
                            String nombreCanal = partes[1];
                            String emisor = partes[2];
                            String contenido = partes[3];
                            System.out.println("[v0] Mensaje de canal [" + nombreCanal + "] de " + emisor + ": " + contenido);

                            // Notify general listener
                            if (mensajeListener != null) {
                                mensajeListener.onMensajeRecibido(mensaje);
                            }
                        }
                        continue;
                    }

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
                        System.out.println("[v0] Lista actualizada de usuarios conectados (" + usuariosConectados.size() + ")");
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
            //Mensaje respuesta = ServidorMock.procesar(mensaje);
            //ultimaRespuestaMock = respuesta;
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

        if (mensaje instanceof MensajeTextoPrivado mtp) {
            String destinatario = mtp.getReceptorCorreo();
            String contenido = mtp.getContenido();
            String protocolo = "PRIVADO|" + destinatario + "|" + contenido;
            output.println(protocolo);
            output.flush();
            System.out.println("[v0] Mensaje privado enviado a " + destinatario + ": " + contenido);
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
     * Envía el mensaje "Exit" al servidor para cerrar sesión correctamente
     */
    public void enviarMensajeExit() throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor.");
        }

        if (modoMock) {
            System.out.println("[v0] Modo mock: simulando envío de Exit");
            return;
        }

        output.println("Exit");
        output.flush();
        System.out.println("[v0] Mensaje 'Exit' enviado al servidor");
    }

    /**
     * Crea un canal en el servidor
     * Protocolo: CREAR_CANAL|nombre|descripcion|privado|correoCreador
     */
    public void crearCanal(String nombre, String descripcion, boolean privado, String correoCreador) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor.");
        }

        String protocolo = "CREAR_CANAL|" + nombre + "|" + descripcion + "|" + privado + "|" + correoCreador;
        output.println(protocolo);
        output.flush();
        System.out.println("[v0] Solicitud de creación de canal enviada: " + protocolo);
    }

    /**
     * Invita usuarios a un canal privado
     * Protocolo: INVITAR_CANAL|idCanal|nombreCanal|correoInvitador|correo1,correo2,correo3
     */
    public void invitarUsuariosCanal(String idCanal, String nombreCanal, String correoInvitador, List<String> correosInvitados) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor.");
        }

        String correosStr = String.join(",", correosInvitados);
        String protocolo = "INVITAR_CANAL|" + idCanal + "|" + nombreCanal + "|" + correoInvitador + "|" + correosStr;
        output.println(protocolo);
        output.flush();
        System.out.println("[v0] Invitaciones enviadas: " + protocolo);
    }

    /**
     * Responde a una invitación de canal
     * Protocolo: RESPONDER_INVITACION|idInvitacion|idCanal|correoUsuario|ACEPTAR/RECHAZAR
     */
    public void responderInvitacion(String idInvitacion, String idCanal, String correoUsuario, boolean aceptar) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor.");
        }

        String accion = aceptar ? "ACEPTAR" : "RECHAZAR";
        String protocolo = "RESPONDER_INVITACION|" + idInvitacion + "|" + idCanal + "|" + correoUsuario + "|" + accion;
        output.println(protocolo);
        output.flush();
        System.out.println("[v0] Respuesta a invitación enviada: " + protocolo);
    }

    /**
     * Envía un mensaje a un canal
     * Protocolo: CANAL|idCanal|contenido
     */
    public void enviarMensajeCanal(String idCanal, String contenido) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor.");
        }

        String protocolo = "CANAL|" + idCanal + "|" + contenido;
        output.println(protocolo);
        output.flush();
        System.out.println("[v0] Mensaje enviado al canal " + idCanal + ": " + contenido);
    }

    /**
     * Solicita la lista de canales del usuario
     * Protocolo: OBTENER_MIS_CANALES|correoUsuario
     */
    public void solicitarMisCanales(String correoUsuario) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor.");
        }

        String protocolo = "OBTENER_MIS_CANALES|" + correoUsuario;
        output.println(protocolo);
        output.flush();
        System.out.println("[v0] Solicitud de canales enviada para: " + correoUsuario);
    }

    /**
     * Verifica si el usuario es creador de un canal
     * Protocolo: VERIFICAR_CREADOR|idCanal|correoUsuario
     */
    public void verificarCreador(String idCanal, String correoUsuario) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor.");
        }

        String protocolo = "VERIFICAR_CREADOR|" + idCanal + "|" + correoUsuario;
        output.println(protocolo);
        output.flush();
        System.out.println("[v0] Verificación de creador enviada: " + protocolo);
    }

    /**
     * Recibir mensaje del servidor real o mock.
        // 🔻 --- CÓDIGO ORIGINAL ---
        /*
        byte[] datos = AnalizadorProtocolo.serializar(mensaje);
        salida.writeInt(datos.length);
        salida.write(datos);
        salida.flush();
        */
        // 🔺 --- FIN CÓDIGO ORIGINAL ---


    // NUEVO: para almacenar la respuesta simulada del mock
    private Mensaje ultimaRespuestaMock = null;

    /**
     * Recibe un mensaje del servidor real o mock.
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
