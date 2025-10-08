package org.example.client.tcp;

import org.example.common.entidades.Mensaje;
import org.example.common.protocolo.AnalizadorProtocolo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GestorConexionesCliente {

    private Socket socket;
    private DataOutputStream salida;
    private DataInputStream entrada;

    // Establecer conexión con el servidor
    public boolean conectar(String host, int puerto) {
        try {
            socket = new Socket(host, puerto);
            salida = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());
            System.out.println("Conectado al servidor en " + host + ":" + puerto);
            return true;
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    // Enviar mensaje serializado al servidor
    public void enviarMensaje(Mensaje mensaje) throws IOException {
        byte[] datos = AnalizadorProtocolo.serializar(mensaje);
        salida.writeInt(datos.length);
        salida.write(datos);
        salida.flush();
    }

    // 🆕 Nuevo: Leer mensaje recibido del servidor
    public Mensaje leerMensaje() throws IOException {
        try {
            int longitud = entrada.readInt(); // lee tamaño del mensaje
            byte[] datos = new byte[longitud];
            entrada.readFully(datos); // lee mensaje completo
            return AnalizadorProtocolo.deserializar(datos);
        } catch (IOException e) {
            System.err.println("Error al leer mensaje del servidor: " + e.getMessage());
            throw e;
        }
    }

    // Cerrar conexión
    public void cerrarConexion() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
