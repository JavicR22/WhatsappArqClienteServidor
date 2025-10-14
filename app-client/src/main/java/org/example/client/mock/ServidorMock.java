package org.example.client.mock;

import org.example.client.modelo.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SERVIDOR SIMULADO
 * Emula respuestas del servidor real para pruebas del cliente.
 * Mantiene estado en memoria para simular persistencia del servidor.
 */
public class ServidorMock {

    private static final Map<String, Canal> canalesEnServidor = new ConcurrentHashMap<>();
    private static final Map<String, List<MensajeTexto>> mensajesPorCanal = new ConcurrentHashMap<>();
    private static final Map<String, List<MensajeTexto>> mensajesPrivados = new ConcurrentHashMap<>();

    static {
        // Inicializar canales predeterminados
        inicializarCanalesPredeterminados();
    }

    private static void inicializarCanalesPredeterminados() {
        long ahora = System.currentTimeMillis();

        // Canal General (público)
        Canal general = new Canal(
                "canal-general",
                "General",
                false,
                "admin@uni.edu",
                Arrays.asList("cliente@uni.edu", "carlos@uni.edu", "ana@uni.edu", "luis@uni.edu", "maria@uni.edu"),
                ahora
        );
        canalesEnServidor.put(general.getId(), general);
        mensajesPorCanal.put(general.getId(), new ArrayList<>());

        // Canal Investigación (público)
        Canal investigacion = new Canal(
                "canal-investigacion",
                "Investigación",
                false,
                "ana@uni.edu",
                Arrays.asList("cliente@uni.edu", "ana@uni.edu", "carlos@uni.edu"),
                ahora
        );
        canalesEnServidor.put(investigacion.getId(), investigacion);
        mensajesPorCanal.put(investigacion.getId(), new ArrayList<>());

        // Canal Clase-2025 (privado)
        Canal clase = new Canal(
                "canal-clase-2025",
                "Clase-2025",
                true,
                "ana@uni.edu",
                Arrays.asList("cliente@uni.edu", "ana@uni.edu", "luis@uni.edu"),
                ahora
        );
        canalesEnServidor.put(clase.getId(), clase);
        mensajesPorCanal.put(clase.getId(), new ArrayList<>());
    }

    public static Mensaje procesar(Mensaje entrada) {
        if (entrada == null) {
            return new MensajeRespuesta(UUID.randomUUID().toString(), null, false, "Entrada nula (mock)");
        }

        if (entrada instanceof MensajeAutenticacion m) {
            // Validar credenciales
            if ("cliente@uni.edu".equals(m.getCorreo()) && "1234".equals(m.getContrasena())) {
                MensajeRespuesta r = new MensajeRespuesta(
                        UUID.randomUUID().toString(),
                        m.getRemitente(),
                        true,
                        "Usuario autenticado (mock)"
                );
                r.setUsuarioId("5");
                return r;
            } else {
                return new MensajeRespuesta(
                        UUID.randomUUID().toString(),
                        m.getRemitente(),
                        false,
                        "Credenciales incorrectas (mock)"
                );
            }
        }

        if (entrada instanceof MensajeTexto mt) {
            String clave = generarClaveConversacion(mt.getRemitente().getCorreo(), mt.getDestinatario());
            mensajesPrivados.computeIfAbsent(clave, k -> new ArrayList<>()).add(mt);

            return new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    mt.getRemitente(),
                    true,
                    "Mensaje recibido: " + mt.getContenido()
            );
        }

        if (entrada instanceof MensajeCrearCanal c) {
            Canal nuevoCanal = new Canal(
                    UUID.randomUUID().toString(),
                    c.getNombre(),
                    c.isPrivado(),
                    c.getRemitente().getCorreo(),
                    Arrays.asList(c.getRemitente().getCorreo()), // Solo el creador inicialmente
                    System.currentTimeMillis()
            );
            canalesEnServidor.put(nuevoCanal.getId(), nuevoCanal);
            mensajesPorCanal.put(nuevoCanal.getId(), new ArrayList<>());

            return new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    c.getRemitente(),
                    true,
                    "Canal '" + c.getNombre() + "' creado exitosamente (mock)"
            );
        }

        if (entrada instanceof MensajeSolicitudUnion s) {
            return new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    s.getRemitente(),
                    true,
                    "Solicitud al canal " + s.getIdCanal() + " enviada (mock)"
            );
        }

        if (entrada instanceof MensajeInvitacion inv) {
            return new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    inv.getRemitente(),
                    true,
                    "Invitaciones enviadas a " + inv.getCorreosInvitados().size() + " usuarios (mock)"
            );
        }

        if (entrada instanceof MensajeRespuestaInvitacion resp) {
            String accion = resp.isAceptada() ? "aceptada" : "rechazada";
            return new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    resp.getRemitente(),
                    true,
                    "Invitación " + accion + " exitosamente (mock)"
            );
        }

        if (entrada instanceof MensajeAudio a) {
            return new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    a.getRemitente(),
                    true,
                    "Audio recibido (ruta: " + a.getRutaArchivo() + ", tamaño: " + a.getTamanoBytes() + " bytes) (mock)"
            );
        }

        return new MensajeRespuesta(
                UUID.randomUUID().toString(),
                entrada.getRemitente(),
                false,
                "Tipo de mensaje no reconocido en mock"
        );
    }

    public static List<Usuario> obtenerUsuariosVisibles() {
        List<Usuario> lista = new ArrayList<>();
        lista.add(new Usuario("1", "Carlos Pérez", "carlos@uni.edu", "1234", "Estudiante"));
        lista.add(new Usuario("2", "Ana Gómez", "ana@uni.edu", "1234", "Profesor"));
        lista.add(new Usuario("3", "Luis Torres", "luis@uni.edu", "1234", "Estudiante"));
        lista.add(new Usuario("4", "María Ruiz", "maria@uni.edu", "1234", "Administrativo"));
        lista.add(new Usuario("5", "Tú", "cliente@uni.edu", "1234", "Estudiante"));
        return lista;
    }

    public static List<String> obtenerCanalesPredeterminados() {
        List<String> nombres = new ArrayList<>();
        for (Canal canal : canalesEnServidor.values()) {
            nombres.add(canal.getNombre());
        }
        return nombres;
    }

    public static List<Canal> obtenerCanalesDelUsuario(String correoUsuario) {
        List<Canal> canalesUsuario = new ArrayList<>();
        for (Canal canal : canalesEnServidor.values()) {
            if (canal.getMiembros().contains(correoUsuario)) {
                canalesUsuario.add(canal);
            }
        }
        return canalesUsuario;
    }

    public static List<MensajeTexto> obtenerMensajesDeCanal(String idCanal) {
        return mensajesPorCanal.getOrDefault(idCanal, new ArrayList<>());
    }

    public static List<MensajeTexto> obtenerMensajesPrivados(String correo1, String correo2) {
        String clave = generarClaveConversacion(correo1, correo2);
        return mensajesPrivados.getOrDefault(clave, new ArrayList<>());
    }

    private static String generarClaveConversacion(String correo1, String correo2) {
        // Ordenar alfabéticamente para que la clave sea consistente
        if (correo1.compareTo(correo2) < 0) {
            return correo1 + ":" + correo2;
        } else {
            return correo2 + ":" + correo1;
        }
    }
}
