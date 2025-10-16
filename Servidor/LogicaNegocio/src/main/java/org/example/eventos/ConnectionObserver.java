package org.example.eventos;

public interface ConnectionObserver {
    void onUserConnected(String username);
    void onUserDisconnected(String username);
}