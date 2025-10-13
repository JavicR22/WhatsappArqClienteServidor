package org.example.client.controladores;

import org.example.client.negocio.AuthBusinessLogic;
import org.example.client.modelo.Usuario;

/**
 * CAPA DE INTERMEDIARIOS/CONTROLADORES
 * Actúa como mediador entre la presentación (UI) y la lógica de negocio.
 * Recibe peticiones de la GUI y las delega a la capa de negocio.
 */
public class AuthController {
    
    private final AuthBusinessLogic authBusinessLogic;
    
    public AuthController(AuthBusinessLogic authBusinessLogic) {
        this.authBusinessLogic = authBusinessLogic;
    }
    
    /**
     * Procesa la solicitud de autenticación desde la UI
     */
    public boolean autenticar(String correo, String contrasena) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            return false;
        }
        
        return authBusinessLogic.autenticar(correo, contrasena);
    }
    
    /**
     * Procesa la solicitud de desconexión
     */
    public void desconectar(String usuarioId) {
        authBusinessLogic.cerrarSesion(usuarioId);
    }
    
    /**
     * Obtiene el usuario autenticado actual
     */
    public Usuario obtenerUsuarioActual() {
        return authBusinessLogic.obtenerUsuarioActual();
    }
}
