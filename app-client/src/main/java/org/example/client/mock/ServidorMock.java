package org.example.client.mock;

import org.example.client.modelo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SERVIDOR SIMULADO
 * Emula respuestas del servidor real para pruebas del cliente.
 */
public class ServidorMock {

    public static Mensaje procesar(Mensaje entrada) {
        if (entrada == null) {
            return new MensajeRespuesta(UUID.randomUUID().toString(), null, false, "Entrada nula (mock)");
        }

        if (entrada instanceof MensajeAutenticacion m) {
            MensajeRespuesta r = new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    m.getRemitente(),
                    true,
                    "Usuario autenticado (mock)"
            );
            // Simular asignación de id por parte del servidor
            r.setUsuarioId(UUID.randomUUID().toString());
            return r;
        }

        if (entrada instanceof MensajeTexto mt) {
            return new MensajeRespuesta(
                    UUID.randomUUID().toString(),
                    mt.getRemitente(),
                    true,
                    "Mensaje recibido: " + mt.getContenido()
            );
        }

        if (entrada instanceof MensajeCrearCanal c) {
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


}
