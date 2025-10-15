package org.example.client.controladores;

import org.example.client.modelo.Canal;
import org.example.client.modelo.Solicitud;
import org.example.client.negocio.CanalBusinessLogic;

import java.util.List;

/**
 * CAPA DE CONTROLADORES
 * Coordina las operaciones de canales entre la UI y la lógica de negocio
 */
public class CanalController {

    private final CanalBusinessLogic canalBusinessLogic;

    public CanalController(CanalBusinessLogic canalBusinessLogic) {
        this.canalBusinessLogic = canalBusinessLogic;
    }

    /**
     * Crea un nuevo canal
     */
    public boolean crearCanal(String nombre, String descripcion, boolean privado) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("El nombre del canal no puede estar vacío");
            return false;
        }

        return canalBusinessLogic.crearCanal(nombre, descripcion, privado);
    }

    /**
     * Invita usuarios a un canal
     */
    public boolean invitarUsuarios(String idCanal, String nombreCanal, List<String> correosInvitados) {
        if (correosInvitados == null || correosInvitados.isEmpty()) {
            System.err.println("Debe seleccionar al menos un usuario para invitar");
            return false;
        }

        return canalBusinessLogic.invitarUsuarios(idCanal, nombreCanal, correosInvitados);
    }

    /**
     * Acepta una invitación
     */
    public boolean aceptarInvitacion(String idSolicitud, String idCanal) {
        return canalBusinessLogic.responderInvitacion(idSolicitud, idCanal, true);
    }

    /**
     * Rechaza una invitación
     */
    public boolean rechazarInvitacion(String idSolicitud, String idCanal) {
        return canalBusinessLogic.responderInvitacion(idSolicitud, idCanal, false);
    }

    /**
     * Obtiene los canales del usuario actual
     */
    public List<Canal> obtenerCanales() {
        return canalBusinessLogic.obtenerCanalesDelUsuario();
    }

    /**
     * Obtiene las solicitudes pendientes
     */
    public List<Solicitud> obtenerSolicitudesPendientes() {
        return canalBusinessLogic.obtenerSolicitudesPendientes();
    }
}
