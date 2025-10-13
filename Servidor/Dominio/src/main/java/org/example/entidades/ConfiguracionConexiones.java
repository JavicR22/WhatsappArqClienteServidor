package org.example.entidades;

public class ConfiguracionConexiones {
    private int maxUsuariosConectados;
    private boolean esLimitado;

    public ConfiguracionConexiones(int maxUsuariosConectados, boolean esLimitado) {
        this.maxUsuariosConectados = maxUsuariosConectados;
        this.esLimitado = esLimitado;
    }

    public int getMaxUsuariosConectados() {
        return maxUsuariosConectados;
    }

    public boolean esLimitado() {
        return esLimitado;
    }
}
