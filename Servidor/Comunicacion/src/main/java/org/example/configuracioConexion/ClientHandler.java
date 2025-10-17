package org.example.configuracioConexion;

import org.example.AudioDecoder;
import org.example.entidades.*;
import org.example.impl.VoskAudioToText;
import org.example.objectPool.ConnectionPool;
import org.example.servicio.CanalService;
import org.example.servicio.MensajeriaService;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maneja la comunicación con un cliente autenticado.
 * Soporta mensajes de texto, canal y audio en Base64 (dividido en partes).
 */
public class ClientHandler implements Runnable {

    private final ConnectionPool pool;
    private final Connection conexion;
    private final MensajeriaService mensajeriaService;
    private final CanalService canalService;

    // Mapa de sesiones de audio activas
    private final Map<String, AudioSession> audioSessions = new ConcurrentHashMap<>();

    public ClientHandler(ConnectionPool pool, Connection conexion, MensajeriaService mensajeriaService, CanalService canalService) {
        this.pool = pool;
        this.conexion = conexion;
        this.mensajeriaService = mensajeriaService;
        this.canalService=canalService;
    }

    @Override
    public void run() {
        try (
                BufferedReader input = new BufferedReader(new InputStreamReader(conexion.getSocket().getInputStream()));
                PrintWriter output = new PrintWriter(conexion.getSocket().getOutputStream(), true)
        ) {
            output.println("✅ Conectado al servidor. Usa 'exit' para salir.");

            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                if ("exit".equalsIgnoreCase(mensaje.trim())) {
                    break;
                }

                // Clasificación del mensaje
                if (mensaje.startsWith("PRIVADO_AUDIO|")) {
                    procesarFragmentoAudio(mensaje);
                } else if (mensaje.startsWith("CREAR_CANAL|")){
                    System.out.println("Proceso: Creación de la canal: "+mensaje);
                    procesarCreacionCanal(mensaje);
                }
                else {
                    procesarMensaje(mensaje);
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error I/O con el cliente " + conexion.getUsuario() + ": " + e.getMessage());
        } finally {
            String usuario = conexion.getUsuario();
            pool.removerConexion(usuario);
            System.out.println("👋 Usuario desconectado: " + usuario);
            conexion.cerrar();
        }
    }
    //Creación canal
    private void procesarCreacionCanal(String mensaje) throws Exception {
        String[] partes = mensaje.split("\\|");
        if (partes.length < 5) {
            throw new Exception("Formato de mensaje inválido: " + mensaje);
        }

        String nombreCanal = partes[1];
        String descripcion = partes[2];
        String privadoString = partes[3];
        String correoCreador = partes[4];
        LocalDateTime fechaCreacion = LocalDateTime.now();
        System.out.println("Privado: "+privadoString);

        Boolean privadoBool = Boolean.parseBoolean(privadoString);
        System.out.println("Canal en creación");
        Canal canalCreado = new Canal(nombreCanal,fechaCreacion,correoCreador,
                descripcion,privadoBool);

        canalService.crearCanal(canalCreado);


    }
    // =============================================================
    // 🟢 Procesamiento general de mensajes
    // =============================================================

    private void procesarMensaje(String mensaje) {
        try {
            if (mensaje.startsWith("PRIVADO_TEXTO|")) {
                procesarMensajeTextoPrivado(mensaje);
            } else if (mensaje.startsWith("CANAL|")) {
                procesarMensajeCanal(mensaje);
            }
        } catch (Exception e) {
            System.err.println("❌ Error procesando mensaje de " + conexion.getUsuario() + ": " + e.getMessage());
        }
    }

    private void procesarMensajeTextoPrivado(String mensaje) {
        String[] partes = mensaje.split("\\|", 3);
        if (partes.length < 3) return;

        String receptor = partes[1];
        String contenido = partes[2];

        try {
            MensajeTextoPrivado msg = new MensajeTextoPrivado();
            msg.setEmisor(new Usuario(conexion.getUsuario()));
            msg.setReceptor(new Usuario(receptor));
            msg.setContenidoTexto(contenido);
            msg.setFechaEnvio(LocalDateTime.now());

            mensajeriaService.enviarMensajeTextoPrivado(msg);
            System.out.println("📩 PRIVADO TEXTO → " + conexion.getUsuario() + " → " + receptor + ": " + contenido);
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje privado: " + e.getMessage());
        }
    }

    private void procesarMensajeCanal(String mensaje) {
        String[] partes = mensaje.split("\\|", 3);
        if (partes.length < 3) return;

        String idCanal = partes[1];
        String contenido = partes[2];

        try {
            MensajeTextoCanal msg = new MensajeTextoCanal();
            msg.setEmisor(new Usuario(conexion.getUsuario()));
            msg.setCanal(new Canal(Integer.parseInt(idCanal)));
            msg.setContenido(contenido);

            mensajeriaService.enviarMensajeTextoCanal(msg);
            System.out.println("💬 CANAL " + idCanal + " → " + conexion.getUsuario() + ": " + contenido);
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje de canal: " + e.getMessage());
        }
    }

    // =============================================================
    // 🎧 Procesamiento de audios en Base64 fragmentado
    // =============================================================

    private void procesarFragmentoAudio(String mensaje) {
        try {
            // Dividir solo los primeros 5 separadores
            String[] partes = mensaje.split("\\|");


            String tipo = partes[1];

            switch (tipo) {
                case "START" -> {
                    // Formato: PRIVADO_AUDIO|START|audioId|receptor|totalChunks
                    if (partes.length < 5) return;
                    iniciarRecepcionAudio(partes[2], partes[3], Integer.parseInt(partes[4]));
                }
                case "CHUNK" -> {
                    // Formato: PRIVADO_AUDIO|CHUNK|audioId|chunkIndex|totalChunks|base64Data
                    if (partes.length < 6) return;
                    String audioId = partes[2];
                    int chunkIndex = Integer.parseInt(partes[3]);
                    System.out.println(partes[3]);
                    int totalChunks = Integer.parseInt(partes[4]);
                    String fragmentoBase64 = partes[5]; // El resto es el data

                    agregarFragmentoAudio(audioId, chunkIndex, totalChunks, fragmentoBase64);
                }
                case "END" -> {
                    // Formato: PRIVADO_AUDIO|END|audioId|receptor
                    if (partes.length < 4) return;
                    finalizarRecepcionAudio(partes[2], partes[3]);
                }
                default -> System.err.println("⚠️ Tipo de fragmento desconocido: " + tipo);
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Error parsing números en fragmento: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error procesando fragmento: " + e.getMessage());
        }
    }


    private void iniciarRecepcionAudio(String audioId, String receptor, int totalChunks) {
        AudioSession session = new AudioSession(audioId, receptor, totalChunks);
        audioSessions.put(audioId, session);
        System.out.println("🎙️ Inicio recepción de audio [" + audioId + "] → " + receptor +
                " (" + totalChunks + " chunks)");
    }

    private void agregarFragmentoAudio(String audioId, int chunkIndex, int totalChunks, String fragmentoBase64) {
        AudioSession session = audioSessions.get(audioId);
        if (session == null) {
            System.err.println("⚠️ No hay sesión activa para: " + audioId);
            return;
        }

        if (session.isExpired()) {
            audioSessions.remove(audioId);
            System.err.println("⚠️ Sesión expirada: " + audioId);
            return;
        }

        session.buffer.append(fragmentoBase64);
        session.receivedChunks.add(chunkIndex);

        if (chunkIndex % 5 == 0 || session.isComplete()) {
            System.out.println("📥 Chunk " + (chunkIndex + 1) + "/" + totalChunks +
                    " [" + audioId + "]");
        }
    }

    private void finalizarRecepcionAudio(String audioId, String receptor) {
        AudioSession session = audioSessions.remove(audioId);

        if (session == null) {
            System.err.println("⚠️ No había sesión para finalizar: " + audioId);
            return;
        }

        if (!session.isComplete()) {
            System.err.println("❌ Audio incompleto [" + audioId + "]: " +
                    session.receivedChunks.size() + "/" + session.expectedChunks);
            return;
        }

        String base64Completo = session.buffer.toString();
        System.out.println(base64Completo);
        System.out.println("🔥 Audio recibido completo [" + audioId + "] (" +
                base64Completo.length() + " chars)");

        // Procesar en hilo aparte
        new Thread(() -> procesarAudioFinal(audioId, receptor, base64Completo)).start();
    }


    private void procesarAudioFinal(String audioId, String receptor, String base64Completo) {
        try {
            // Validar Base64
            byte[] decodificado = Base64.getDecoder().decode(base64Completo);
            System.out.println("✅ Base64 válido: " + decodificado.length + " bytes");

            MensajeAudioPrivado msg = new MensajeAudioPrivado();
            msg.setEmisor(new Usuario(conexion.getUsuario()));
            msg.setReceptor(new Usuario(receptor));
            msg.setFechaEnvio(LocalDateTime.now());

            // Decodificar a WAV
            File archivoWAV = AudioDecoder.decodeBase64ToWav(base64Completo,
                    "audio_" + audioId);
            System.out.println("📁 WAV guardado: " + archivoWAV.getAbsolutePath());

            // Transcribir con Vosk
            String transcripcion = VoskAudioToText.transcribe(archivoWAV);

            msg.setRutaAudio(archivoWAV.getAbsolutePath());
            msg.setContenidoTexto(transcripcion);

            // Guardar en BD
            mensajeriaService.enviarMensajeAudioPrivado(base64Completo,receptor, msg.getEmisor().getUsername());
            mensajeriaService.guardarMensajeAudioPrivado(msg);


            System.out.println("🎧 Audio procesado [" + audioId + "] → " + receptor +
                    " | Texto: " + transcripcion);

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Error Base64 inválido [" + audioId + "]: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error procesando audio [" + audioId + "]: " + e.getMessage());
        }
    }
    /**
     * Limpiar sesiones de audio expiradas (ejecutar periódicamente)
     */
    public void limpiarSesionesExpiradas() {
        audioSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}