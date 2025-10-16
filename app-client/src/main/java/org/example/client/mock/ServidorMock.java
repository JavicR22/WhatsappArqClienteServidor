package org.example.client.mock;

import org.example.client.modelo.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SERVIDOR SIMULADO
 * Emula un servidor real en memoria (sin red).
 * Permite chat privado bidireccional y mensajes por canal.
 */
public class ServidorMock {

    private static final Map<String, Canal> canalesEnServidor = new ConcurrentHashMap<>();
    private static final Map<String, List<MensajeTexto>> mensajesPorCanal = new ConcurrentHashMap<>();
    private static final Map<String, List<MensajeTexto>> mensajesPrivados = new ConcurrentHashMap<>();
    private static final Map<String, Usuario> usuariosRegistrados = new ConcurrentHashMap<>();

    static {
        inicializarUsuarios();
        inicializarCanalesPredeterminados();
    }

    private static void inicializarUsuarios() {
        agregarUsuario("cliente@uni.edu", "1234", "Camilo", "Estudiante");
        agregarUsuario("carlos@uni.edu", "1234", "Carlos Pérez", "Estudiante");
        agregarUsuario("ana@uni.edu", "1234", "Ana Gómez", "Profesor");
        agregarUsuario("luis@uni.edu", "1234", "Luis Torres", "Estudiante");
        agregarUsuario("maria@uni.edu", "1234", "María Ruiz", "Administrativo");
        agregarUsuario("laura@uni.edu", "abcd", "Laura Silva", "Estudiante");
        agregarUsuario("andres@uni.edu", "1234", "Andrés López", "Estudiante");
    }

    private static void agregarUsuario(String correo, String contrasena, String nombre, String rol) {
        Usuario u = new Usuario(UUID.randomUUID().toString(), nombre, correo, contrasena, rol);
        usuariosRegistrados.put(correo, u);
    }

    private static void inicializarCanalesPredeterminados() {
        long ahora = System.currentTimeMillis();

        Canal general = new Canal(
                "canal-general",
                "General",
                false,
                "admin@uni.edu",
                new ArrayList<>(usuariosRegistrados.keySet()),
                ahora
        );
        canalesEnServidor.put(general.getId(), general);
        mensajesPorCanal.put(general.getId(), new ArrayList<>());
    }

    /** 🔧 Procesamiento principal */
    public static synchronized Mensaje procesar(Mensaje entrada) {
        if (entrada == null)
            return new MensajeRespuesta(UUID.randomUUID().toString(), null, false, "Entrada nula (mock)");

        // 🧩 AUTENTICACIÓN
        if (entrada instanceof MensajeAutenticacion m) {
            return autenticarUsuario(m);
        }

        // 💬 MENSAJE DE TEXTO
        if (entrada instanceof MensajeTexto mt) {
            if (mt.getDestinatario() != null && mt.getDestinatario().contains("@")) {
                // Chat privado entre usuarios
                String correoRemitente = mt.getRemitente().getCorreo();
                String correoDestinatario = mt.getDestinatario();

                String clave = generarClaveConversacion(correoRemitente, correoDestinatario);

                mensajesPrivados.computeIfAbsent(clave, k -> new ArrayList<>()).add(mt);

                System.out.println("💾 Guardado en chat privado (" + clave + "): " + mt.getContenido());

                return new MensajeRespuesta(
                        UUID.randomUUID().toString(),
                        mt.getRemitente(),
                        true,
                        "Mensaje privado entregado (mock)"
                );
            } else {
                // Mensaje en canal
                List<MensajeTexto> lista = mensajesPorCanal.computeIfAbsent(mt.getDestinatario(), k -> new ArrayList<>());
                lista.add(mt);
                return new MensajeRespuesta(
                        UUID.randomUUID().toString(),
                        mt.getRemitente(),
                        true,
                        "Mensaje publicado en canal (mock)"
                );
            }
        }

        // 📢 CREAR CANAL
        if (entrada instanceof MensajeCrearCanal c) {
            return crearCanal(c);
        }

        // ✉️ INVITACIONES
        if (entrada instanceof MensajeInvitacion inv) {
            return new MensajeRespuesta(UUID.randomUUID().toString(),
                    inv.getRemitente(), true,
                    "Invitaciones enviadas exitosamente (mock)");
        }

        // ❓TIPO DESCONOCIDO
        return new MensajeRespuesta(UUID.randomUUID().toString(), entrada.getRemitente(),
                false, "Tipo de mensaje no reconocido (mock)");
    }


    /** 🧍‍♂️ Autenticación */
    private static Mensaje autenticarUsuario(MensajeAutenticacion m) {
        Usuario usuario = usuariosRegistrados.get(m.getCorreo());
        if (usuario != null && usuario.getContrasena().equals(m.getContrasena())) {
            MensajeRespuesta r = new MensajeRespuesta(UUID.randomUUID().toString(), m.getRemitente(),
                    true, "Usuario autenticado correctamente (mock)");
            r.setUsuarioId(usuario.getId());
            return r;
        } else {
            return new MensajeRespuesta(UUID.randomUUID().toString(), m.getRemitente(),
                    false, "Credenciales incorrectas (mock)");
        }
    }

    /** 💬 Procesamiento de mensajes de texto */
    private static Mensaje procesarMensajeTexto(MensajeTexto mt) {
        if (mt.getDestinatario() == null || mt.getDestinatario().isEmpty())
            return new MensajeRespuesta(UUID.randomUUID().toString(), mt.getRemitente(),
                    false, "Destinatario vacío (mock)");

        if (mt.getDestinatario().contains("@")) {
            // 🗣️ Chat privado
            String clave = generarClaveConversacion(mt.getRemitente().getCorreo(), mt.getDestinatario());
            mensajesPrivados.computeIfAbsent(clave, k -> new ArrayList<>()).add(mt);

            System.out.println("💾 Guardado en chat privado (" + clave + "): " + mt.getContenido());

            return new MensajeRespuesta(UUID.randomUUID().toString(), mt.getRemitente(),
                    true, "Mensaje privado entregado (mock)");
        } else {
            // 📢 Canal
            mensajesPorCanal.computeIfAbsent(mt.getDestinatario(), k -> new ArrayList<>()).add(mt);
            return new MensajeRespuesta(UUID.randomUUID().toString(), mt.getRemitente(),
                    true, "Mensaje publicado en canal (mock)");
        }
    }

    /** 🧱 Crear canal */
    private static Mensaje crearCanal(MensajeCrearCanal c) {
        Canal nuevoCanal = new Canal(
                UUID.randomUUID().toString(),
                c.getNombre(),
                c.isPrivado(),
                c.getRemitente().getCorreo(),
                Arrays.asList(c.getRemitente().getCorreo()),
                System.currentTimeMillis()
        );
        canalesEnServidor.put(nuevoCanal.getId(), nuevoCanal);
        mensajesPorCanal.put(nuevoCanal.getId(), new ArrayList<>());

        return new MensajeRespuesta(UUID.randomUUID().toString(), c.getRemitente(),
                true, "Canal '" + c.getNombre() + "' creado exitosamente (mock)");
    }

    /** 📜 Utilidades */
    public static List<Usuario> obtenerUsuariosVisibles() {
        return new ArrayList<>(usuariosRegistrados.values());
    }

    public static List<Canal> obtenerCanalesPredeterminados() {
        return new ArrayList<>(canalesEnServidor.values());
    }

    public static List<MensajeTexto> obtenerMensajesPrivados(String correo1, String correo2) {
        String clave = generarClaveConversacion(correo1, correo2);
        return new ArrayList<>(mensajesPrivados.getOrDefault(clave, new ArrayList<>()));
    }


    private static String generarClaveConversacion(String correo1, String correo2) {
        if (correo1.compareToIgnoreCase(correo2) < 0)
            return correo1 + ":" + correo2;
        else
            return correo2 + ":" + correo1;
    }
}