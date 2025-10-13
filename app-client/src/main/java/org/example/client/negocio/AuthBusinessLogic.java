package org.example.client.negocio;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.modelo.*;

import java.util.UUID;

/**
 * CAPA DE LÓGICA DE NEGOCIO
 * Contiene la lógica principal de autenticación del cliente.
 * Coordina la comunicación con el servidor y gestiona el estado del usuario.
 */
public class AuthBusinessLogic {
    
    private final GestorComunicacion gestorComunicacion;
    private Usuario usuarioActual;
    
    public AuthBusinessLogic(GestorComunicacion gestorComunicacion) {
        this.gestorComunicacion = gestorComunicacion;
    }
    
    /**
     * Lógica de autenticación: conecta al servidor y valida credenciales
     */
    public boolean autenticar(String correo, String contrasena) {
        try {
            // Crear usuario temporal para autenticación
            Usuario usuario = new Usuario(null, null, correo, null, null);
            
            // Crear mensaje de autenticación
            MensajeAutenticacion mensajeAuth = new MensajeAutenticacion(
                UUID.randomUUID().toString(),
                usuario,
                correo,
                contrasena
            );
            
            // Enviar al servidor
            gestorComunicacion.enviarMensaje(mensajeAuth);
            
            // Esperar respuesta
            Mensaje respuesta = gestorComunicacion.recibirMensaje();
            
            if (respuesta instanceof MensajeRespuesta mr) {
                if (mr.isExito()) {
                    // Actualizar usuario con datos del servidor
                    usuario.setId(mr.getUsuarioId());
                    this.usuarioActual = usuario;
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Error en lógica de autenticación: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void cerrarSesion(String usuarioId) {
        this.usuarioActual = null;
        gestorComunicacion.cerrarConexion();
        System.out.println("Sesión cerrada para usuario: " + usuarioId);
    }
    
    /**
     * Obtiene el usuario autenticado
     */
    public Usuario obtenerUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean estaAutenticado() {
        return usuarioActual != null;
    }
}
