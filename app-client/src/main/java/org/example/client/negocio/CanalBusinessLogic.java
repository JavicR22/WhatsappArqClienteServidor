package org.example.client.negocio;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.datos.RepositorioLocal;
import org.example.client.modelo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CAPA DE LÓGICA DE NEGOCIO
 * Gestiona la lógica de canales, invitaciones y solicitudes
 */
public class CanalBusinessLogic {

    private final GestorComunicacion gestorComunicacion;
    private final RepositorioLocal repositorioLocal;
    private final AuthBusinessLogic authBusinessLogic;
    private final ServicioNotificaciones servicioNotificaciones;

    public CanalBusinessLogic(GestorComunicacion gestorComunicacion,
                              RepositorioLocal repositorioLocal,
                              AuthBusinessLogic authBusinessLogic,
                              ServicioNotificaciones servicioNotificaciones) {
        this.gestorComunicacion = gestorComunicacion;
        this.repositorioLocal = repositorioLocal;
        this.authBusinessLogic = authBusinessLogic;
        this.servicioNotificaciones = servicioNotificaciones;
    }

    /**
     * Crea un nuevo canal
     */
    public boolean crearCanal(String nombre, String descripcion, boolean privado) {
        try {
            Usuario usuario = authBusinessLogic.obtenerUsuarioActual();
            if (usuario == null) {
                System.err.println("No hay usuario autenticado");
                return false;
            }

            gestorComunicacion.crearCanal(nombre, descripcion, privado, usuario.getCorreo());

            System.out.println("✅ Solicitud de creación de canal enviada: " + nombre);
            return true;

        } catch (Exception e) {
            System.err.println("Error creando canal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Invita usuarios a un canal
     */
    public boolean invitarUsuarios(String idCanal, String nombreCanal, List<String> correosInvitados) {
        try {
            Usuario usuario = authBusinessLogic.obtenerUsuarioActual();
            if (usuario == null) {
                System.err.println("No hay usuario autenticado");
                return false;
            }

            gestorComunicacion.invitarUsuariosCanal(idCanal, nombreCanal, usuario.getCorreo(), correosInvitados);

            System.out.println("✅ Invitaciones enviadas exitosamente");
            return true;

        } catch (Exception e) {
            System.err.println("Error invitando usuarios: " + e.getMessage());
            return false;
        }
    }

    /**
     * Responde a una invitación (aceptar o rechazar)
     */
    public boolean responderInvitacion(String idSolicitud, String idCanal, boolean aceptar) {
        try {
            Usuario usuario = authBusinessLogic.obtenerUsuarioActual();
            if (usuario == null) {
                System.err.println("No hay usuario autenticado");
                return false;
            }

            gestorComunicacion.responderInvitacion(idSolicitud, idCanal, usuario.getCorreo(), aceptar);

            // Actualizar estado de la solicitud localmente
            String nuevoEstado = aceptar ? "ACEPTADA" : "RECHAZADA";
            repositorioLocal.actualizarEstadoSolicitud(idSolicitud, nuevoEstado);

            System.out.println("✅ Invitación " + (aceptar ? "aceptada" : "rechazada"));
            return true;

        } catch (Exception e) {
            System.err.println("Error respondiendo invitación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene los canales del usuario actual
     */
    public List<Canal> obtenerCanalesDelUsuario() {
        Usuario usuario = authBusinessLogic.obtenerUsuarioActual();
        if (usuario == null) {
            System.err.println("No hay usuario autenticado");
            return List.of();
        }

        return repositorioLocal.obtenerCanalesDelUsuario(usuario.getCorreo());
    }

    /**
     * Obtiene las solicitudes pendientes del usuario actual
     */
    public List<Solicitud> obtenerSolicitudesPendientes() {
        Usuario usuario = authBusinessLogic.obtenerUsuarioActual();
        if (usuario == null) {
            System.err.println("No hay usuario autenticado");
            return List.of();
        }

        return repositorioLocal.obtenerSolicitudesPendientes(usuario.getCorreo());
    }
}
