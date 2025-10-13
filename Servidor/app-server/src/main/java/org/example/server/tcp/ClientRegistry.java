package org.example.server.tcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {

    private final Map<String, ClientSession> sesionesActivas = new ConcurrentHashMap<>();
    private final int maxUsuarios;

    public ClientRegistry(int maxUsuarios) {
        this.maxUsuarios = maxUsuarios;
    }

    public boolean registrarSesion(ClientSession sesion) {
        if (sesionesActivas.size() >= maxUsuarios) {
            System.out.println("Límite máximo de usuarios alcanzado.");
            return false;
        }
        sesionesActivas.put(sesion.getIdSesion(), sesion);
        return true;
    }

    public void eliminarSesion(String idSesion) {
        sesionesActivas.remove(idSesion);
    }

    public int contarUsuariosConectados() {
        return sesionesActivas.size();
    }
}
