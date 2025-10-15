package org.example.client.comunicacion;

import org.example.client.modelo.*;
import org.example.client.mock.ServidorMock;
import org.example.client.protocolo.AnalizadorProtocolo;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

/**
 * GestorComunicacion actualizado:
 * - Soporta modo mock (igual que antes).
 * - Soporta conexión a servidor real por TCP (protocolo de texto sencillo).
 *
 * Nota: aquí se implementa lo mínimo necesario para la autenticación con el servidor real
 * (el servidor envía "LOGIN", cliente responde "username|password" y el servidor devuelve "OK:Bienvenido <user>" o "ERR:...").
 *
 * Ampliaciones futuras:
 * - Mapear más tipos de Mensaje a formatos de texto/JSON según especifique el servidor.
 * - Implementar listener / hilo de lectura continuo para mensajes asíncronos del servidor.
 */
public class GestorComunicacion {

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean conectado;
    private boolean modoMock = false; // NUEVO: modo de simulación sin servidor real
    private String usuarioIdConectado = null;

    // Para compatibilidad con mock existente
    private Mensaje ultimaRespuestaMock = null;

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
            conectado = true;
            System.out.println("✅ Conectado al servidor en " + host + ":" + puerto);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al conectar con el servidor: " + e.getMessage());
            conectado = false;
            return false;
        }
    }

    /**
     * Enviar un mensaje al servidor (o al mock).
     * Para MensajeAutenticacion: adapta al protocolo 'username|password' despues de esperar 'LOGIN'.
     * Para MensajeTexto: envía una línea simple con prefijo MSG|<destinatario>|<contenido>
     *
     * Lanza IOException si ocurre un problema en modo real.
     */
    public void enviarMensaje(Mensaje mensaje) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión activa con el servidor o mock.");
        }

        if (modoMock) {
            // Simular envío directo al servidor mock
            Mensaje respuesta = ServidorMock.procesar(mensaje);
            // En modo mock, guardamos la respuesta para el método recibirMensaje()
            ultimaRespuestaMock = respuesta;
            return;
        }

        // Modo real: traducir tipos básicos (autenticación y texto) al protocolo de texto
        if (mensaje instanceof MensajeAutenticacion ma) {
            // **Protocolo de autenticación esperado por el servidor real**
            // 1) Leer del servidor (debe mandar "LOGIN")
            String servidorLinea = reader.readLine(); // bloqueante
            if (servidorLinea == null) {
                throw new IOException("Conexión cerrada por el servidor antes de la autenticación.");
            }
            if (!"LOGIN".equals(servidorLinea.trim())) {
                // protocolo inesperado
                System.err.println("❌ Protocolo inesperado del servidor: esperado 'LOGIN', recibido: " + servidorLinea);
                // enviar algo por si acaso (opcional) y devolver una respuesta de error
                writer.write("ERROR-PROTOCOL\n");
                writer.flush();
                // Guardar ultimaRespuestaMock en forma de MensajeRespuesta para compatibilidad
                ultimaRespuestaMock = new MensajeRespuesta(UUID.randomUUID().toString(), ma.getRemitente(), false, "Protocolo inesperado");
                return;
            }

            // 2) Enviar credenciales en formato 'correo|contrasena'
            String cred = ma.getCorreo() + "|" + ma.getContrasena();
            writer.write(cred + "\n");
            writer.flush();

            // 3) Esperar respuesta del servidor (una línea)
            String respuestaServidor = reader.readLine();
            if (respuestaServidor == null) {
                throw new IOException("Conexión cerrada por el servidor después de enviar credenciales.");
            }

            // Convertir la respuesta a MensajeRespuesta y almacenarla para recibirMensaje()
            boolean ok = respuestaServidor.startsWith("OK:");
            MensajeRespuesta resp = new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    ma.getRemitente(),
                    ok,
                    respuestaServidor
            );

            // Si es OK, intentamos extraer un ID si el servidor lo incluyera (no obligatorio)
            if (ok) {
                // ejemplo: "OK:Bienvenido usuario"
                // dejamos usuarioIdConectado null a menos que el servidor mande ID explícito
                usuarioIdConectado = null;
                // pero podríamos guardar el nombre/correo en el Remitente del MensajeRespuesta
            }

            // Guardar la respuesta para que recibirMensaje() pueda devolverla si se llama después
            ultimaRespuestaMock = resp;
            return;
        }

        // MensajeTexto: traducir a línea simple "MSG|<destinatario>|<contenido>"
        if (mensaje instanceof MensajeTexto mt) {
            String dest = mt.getDestinatario() == null ? "" : mt.getDestinatario();
            String linea = "MSG|" + dest + "|" + mt.getContenido();
            writer.write(linea + "\n");
            writer.flush();

            // Leer confirmación del servidor y guardarla como MensajeRespuesta
            String respuestaServidor = reader.readLine();
            if (respuestaServidor == null) {
                throw new IOException("Conexión cerrada por el servidor tras enviar mensaje de texto.");
            }
            MensajeRespuesta resp = new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    mt.getRemitente(),
                    respuestaServidor.startsWith("OK"),
                    respuestaServidor
            );
            ultimaRespuestaMock = resp;
            return;
        }

        // Otros tipos: por ahora los rechazamos o enviamos una línea indicativa
        writer.write("UNKNOWN-MSG\n");
        writer.flush();
        String respuestaServidor = reader.readLine();
        MensajeRespuesta resp = new MensajeRespuesta(
                UUID.randomUUID().toString(),
                mensaje.getRemitente(),
                respuestaServidor != null && respuestaServidor.startsWith("OK"),
                respuestaServidor == null ? "Sin respuesta" : respuestaServidor
        );
        ultimaRespuestaMock = resp;
    }

    /**
     * Recibir mensaje del servidor real o mock.
     * En modo mock devuelve la última respuesta guardada por enviarMensaje(mock).
     * En modo real intenta leer una línea y convertirla en Mensaje (Respuesta o Texto).
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

        // Modo real: si hay una respuesta previa (ultimaRespuestaMock) devuélvela primero para compatibilidad
        if (ultimaRespuestaMock != null) {
            Mensaje tmp = ultimaRespuestaMock;
            ultimaRespuestaMock = null;
            return tmp;
        }

        // Bloquea esperando la siguiente línea del servidor (comportamiento equivalente a recibirMensaje)
        String linea = reader.readLine();
        if (linea == null) {
            throw new IOException("Conexión cerrada por el servidor.");
        }

        // Interpretemos la línea:
        // - Si comienza con "MSG|" -> MensajeTexto del servidor (formato: MSG|remitente|contenido)
        // - Si comienza con "OK:" o "ERR" -> MensajeRespuesta
        if (linea.startsWith("MSG|")) {
            // formato: MSG|<remitente>|<contenido>
            String[] partes = linea.split("\\|", 3);
            String remitenteCorreo = partes.length >= 2 ? partes[1] : "";
            String contenido = partes.length >= 3 ? partes[2] : "";
            Usuario remitente = new Usuario("?", remitenteCorreo, remitenteCorreo, "", "");
            MensajeTexto mt = new MensajeTexto(UUID.randomUUID().toString(), remitente, contenido);
            return mt;
        } else {
            // Tratar como MensajeRespuesta
            boolean ok = linea.startsWith("OK");
            Usuario remitente = new Usuario("server", "server@server", "", "", "");
            MensajeRespuesta mr = new MensajeRespuesta(UUID.randomUUID().toString(), remitente, ok, linea);
            return mr;
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
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            System.out.println("👋 Conexión cerrada correctamente");
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    /**
     * Verifica si hay conexión activa.
     */
    public boolean estaConectado() {
        return conectado;
    }

    /**
     * (Opcional) obtener el id de usuario conectado si se obtiene del servidor.
     */
    public String getUsuarioIdConectado() {
        return usuarioIdConectado;
    }

}
