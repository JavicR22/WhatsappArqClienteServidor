package org.example.client.negocio;

import org.example.client.comunicacion.GestorComunicacion;
import org.example.client.config.ConfigManager;
import org.example.client.modelo.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public List<UsuarioConectado> obtenerUsuariosConectados() {
        return gestorComunicacion.getUsuariosConectados();
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

            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicBoolean autenticado = new AtomicBoolean(false);
            final String[] respuestaServidor = new String[1];

            gestorComunicacion.setMensajeListener(mensaje -> {
                System.out.println("[v0] AuthBusinessLogic recibió: " + mensaje);
                respuestaServidor[0] = mensaje;

                // Procesar respuesta de autenticación
                if (mensaje.startsWith("USUARIOS_CONECTADOS|") || mensaje.startsWith("LOGIN") || mensaje.startsWith("OK:")) {
                    if (mensaje.startsWith("LOGIN")) {
                        // El servidor está pidiendo credenciales, no hacer nada aquí
                        return;
                    }

                    if (mensaje.startsWith("OK:")) {
                        System.out.println("[v0] Servidor confirmó autenticación");
                        return;
                    }

                    boolean resultado = gestorComunicacion.procesarRespuestaAutenticacion(mensaje, correo);
                    autenticado.set(resultado);
                    latch.countDown();
                }
            });

            // Enviar al servidor
            gestorComunicacion.enviarMensaje(mensajeAuth);

            long timeoutSeconds = ConfigManager.getLong("auth.timeout.seconds", 30);
            boolean recibido = latch.await(timeoutSeconds, TimeUnit.SECONDS);

            if (!recibido) {
                System.err.println("❌ Timeout esperando respuesta de autenticación (" + timeoutSeconds + " segundos)");
                return false;
            }

            if (autenticado.get()) {
                // Esperar respuesta del gestor
                Mensaje respuesta = gestorComunicacion.recibirMensaje();

                if (respuesta instanceof MensajeRespuesta mr && mr.isExito()) {
                    usuario.setId(correo);
                    usuario.setNombre(correo);
                    usuario.setRol("Estudiante");

                    int maxIterations = ConfigManager.getInt("photo.wait.iterations", 20);
                    long delayMs = ConfigManager.getLong("photo.wait.delay.ms", 500);

                    // Intentar obtener la foto (esperar un poco si aún no ha llegado)
                    String foto = gestorComunicacion.getFotoUsuarioActual();
                    if (foto == null) {
                        System.out.println("[v0] Esperando foto del usuario (máx " + (maxIterations * delayMs / 1000) + " segundos)...");
                        for (int i = 0; i < maxIterations; i++) {
                            Thread.sleep(delayMs);
                            foto = gestorComunicacion.getFotoUsuarioActual();
                            if (foto != null) {
                                System.out.println("[v0] Foto recibida después de " + ((i + 1) * delayMs / 1000.0) + " segundos");
                                break;
                            }
                        }
                    }

                    if (foto != null && !foto.equals("DEFAULT")) {
                        usuario.setFotoBase64(foto);
                        System.out.println("[v0] Foto asignada al usuario autenticado (Base64 length: " + foto.length() + ")");
                    } else {
                        System.out.println("[v0] Usuario autenticado sin foto personalizada");
                    }

                    this.usuarioActual = usuario;
                    System.out.println("✅ Usuario autenticado: " + correo);
                    List<UsuarioConectado> lista = gestorComunicacion.getUsuariosConectados();
                    System.out.println("[v1] Usuarios conectados tras autenticación: " + lista.size());

                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error en lógica de autenticación: " + e.getMessage());
            e.printStackTrace();
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
