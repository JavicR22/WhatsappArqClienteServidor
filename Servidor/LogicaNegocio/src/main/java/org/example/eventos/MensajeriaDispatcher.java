package org.example.eventos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MensajeriaDispatcher {
    private final List<MensajeriaObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(MensajeriaObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(MensajeriaObserver observer) {
        observers.remove(observer);
    }

    // Notificaciones
    public void notificarMensajePrivado(String usernameDestino, String protocolo) {
        observers.forEach(o -> o.onMensajePrivado(usernameDestino, protocolo));
    }

    public void notificarMensajeCanal(String idCanal, String protocolo) {
        observers.forEach(o -> o.onMensajeCanal(idCanal, protocolo));
    }

    public void notificarUsuarioConectado(String username) {
        observers.forEach(o -> o.onUsuarioConectado(username));
    }

    public void notificarUsuarioDesconectado(String username) {
        observers.forEach(o -> o.onUsuarioDesconectado(username));
    }
}
