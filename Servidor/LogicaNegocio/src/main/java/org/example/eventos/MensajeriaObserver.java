package org.example.eventos;

public interface MensajeriaObserver {
    void onMensajePrivado(String usernameDestino, String protocolo);
    void onMensajeCanal(String idCanal, String protocolo);
    void onUsuarioConectado(String username);
    void onUsuarioDesconectado(String username);
}
