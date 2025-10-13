package org.example.client.comunicacion;

import org.example.client.modelo.Mensaje;
import org.example.client.mock.ServidorMock;
import org.example.client.protocolo.AnalizadorProtocolo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * CAPA DE COMUNICACIÓN
 * Controla los servicios del protocolo TCP y gestiona la comunicación con el servidor.
 * Implementa un patrón de pool de conexiones para optimizar recursos.
 *
 * (Modificado para incluir modo Mock de pruebas locales sin servidor real)
 */
public class GestorComunicacion {

    private Socket socket;
    private DataOutputStream salida;
    private DataInputStream entrada;
    private boolean conectado;
    private boolean modoMock = false; // NUEVO: modo de simulación sin servidor real
    private String usuarioIdConectado = null;

    public void activarModoMock(boolean activar) {
        this.modoMock = activar;
        System.out.println("🔧 Modo mock: " + (activar ? "ACTIVADO" : "DESACTIVADO"));
    }

    /**
     * Establece conexión TCP con el servidor o inicia modo mock.
     */
    public boolean conectar(String host, int puerto) {
        if (modoMock) {
            System.out.println("✅ Conectado al servidor simulado (mock)");
            conectado = true;
            return true;
        }

        // 🔻 --- CÓDIGO ORIGINAL (comentado, se reactivará cuando haya servidor real) ---
        /*
        try {
            socket = new Socket(host, puerto);
            salida = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());
            conectado = true;
            System.out.println("✅ Conectado al servidor en " + host + ":" + puerto);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al conectar con el servidor: " + e.getMessage());
            conectado = false;
            return false;
        }
        */
        // 🔺 --- FIN CÓDIGO ORIGINAL ---

        return false;
    }

    /**
     * Envía un mensaje al servidor real o simulado.
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

        // 🔻 --- CÓDIGO ORIGINAL ---
        /*
        byte[] datos = AnalizadorProtocolo.serializar(mensaje);
        salida.writeInt(datos.length);
        salida.write(datos);
        salida.flush();
        */
        // 🔺 --- FIN CÓDIGO ORIGINAL ---
    }

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
            // Devuelve la última respuesta generada por el mock
            Mensaje respuesta = ultimaRespuestaMock;
            ultimaRespuestaMock = null;
            return respuesta;
        }

        // 🔻 --- CÓDIGO ORIGINAL ---
        /*
        int longitud = entrada.readInt();
        byte[] datos = new byte[longitud];
        entrada.readFully(datos);
        return AnalizadorProtocolo.deserializar(datos);
        */
        // 🔺 --- FIN CÓDIGO ORIGINAL ---

        return null;
    }

    /**
     * Cierra la conexión o termina el modo mock.
     */
    public void cerrarConexion() {
        if (modoMock) {
            conectado = false;
            System.out.println("👋 Conexión mock cerrada correctamente");
            return;
        }

        // 🔻 --- CÓDIGO ORIGINAL ---
        /*
        try {
            conectado = false;
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (socket != null) socket.close();
            System.out.println("👋 Conexión cerrada correctamente");
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
        */
        // 🔺 --- FIN CÓDIGO ORIGINAL ---
    }

    /**
     * Verifica si hay conexión activa.
     */
    public boolean estaConectado() {
        return conectado;
    }
}
