package org.example.client.negocio;

import org.example.client.modelo.Mensaje;
import java.util.ArrayList;
import java.util.List;

public class ServicioNotificaciones {
    public interface Observador {
        void actualizar(Mensaje mensaje);
    }

    private final List<Observador> observadores = new ArrayList<>();

    public void registrar(Observador o) {
        if (!observadores.contains(o)) observadores.add(o);
    }

    public void remover(Observador o) {
        observadores.remove(o);
    }

    public void notificar(Mensaje m) {
        for (Observador o : observadores) {
            o.actualizar(m);
        }
    }
}
