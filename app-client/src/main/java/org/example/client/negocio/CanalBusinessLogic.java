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

            MensajeCrearCanal mensaje = new MensajeCrearCanal(
                    UUID.randomUUID().toString(),
                    usuario,
                    nombre,
                    descripcion,
                    privado
            );

            gestorComunicacion.enviarMensaje(mensaje);
            Mensaje respuesta = gestorComunicacion.recibirMensaje();

            if (respuesta instanceof MensajeRespuesta mr && mr.isExito()) {
                Canal canal = new Canal(nombre, privado, usuario.getCorreo(), new ArrayList<>());
                repositorioLocal.guardarCanal(canal);

                // Notificar a los observadores
                servicioNotificaciones.notificar(mensaje);

                System.out.println("✅ Canal creado exitosamente: " + nombre);
                return true;
            }

            return false;

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

            MensajeInvitacion mensaje = new MensajeInvitacion(
                    UUID.randomUUID().toString(),
                    usuario,
                    idCanal,
                    nombreCanal,
                    correosInvitados
            );

            gestorComunicacion.enviarMensaje(mensaje);
            Mensaje respuesta = gestorComunicacion.recibirMensaje();

            if (respuesta instanceof MensajeRespuesta mr && mr.isExito()) {
                // Crear solicitudes localmente para cada usuario invitado
                for (String correo : correosInvitados) {
                    Solicitud solicitud = new Solicitud(
                            UUID.randomUUID().toString(),
                            correo,
                            idCanal
                    );
                    repositorioLocal.guardarSolicitud(solicitud);
                }

                System.out.println("✅ Invitaciones enviadas exitosamente");
                return true;
            }

            return false;

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

            MensajeRespuestaInvitacion mensaje = new MensajeRespuestaInvitacion(
                    UUID.randomUUID().toString(),
                    usuario,
                    idSolicitud,
                    idCanal,
                    aceptar
            );

            gestorComunicacion.enviarMensaje(mensaje);
            Mensaje respuesta = gestorComunicacion.recibirMensaje();

            if (respuesta instanceof MensajeRespuesta mr && mr.isExito()) {
                // Actualizar estado de la solicitud localmente
                String nuevoEstado = aceptar ? "ACEPTADA" : "RECHAZADA";
                repositorioLocal.actualizarEstadoSolicitud(idSolicitud, nuevoEstado);

                // Si se aceptó, agregar al usuario como miembro del canal
                if (aceptar) {
                    repositorioLocal.agregarMiembroCanal(idCanal, usuario.getCorreo());
                }

                // Notificar a los observadores
                servicioNotificaciones.notificar(mensaje);

                System.out.println("✅ Invitación " + (aceptar ? "aceptada" : "rechazada"));
                return true;
            }

            return false;

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
