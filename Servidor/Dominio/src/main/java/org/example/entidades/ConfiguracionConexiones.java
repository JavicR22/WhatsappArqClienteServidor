package org.example.entidades;



public class ConfiguracionConexiones {
    private int maxUsuariosConectados;
    private boolean esLimitado;

    // ✅ Constructor completo
    public ConfiguracionConexiones(int maxUsuariosConectados, boolean esLimitado) {
        this.maxUsuariosConectados = maxUsuariosConectados;
        this.esLimitado = esLimitado;
    }

    // ✅ Constructor vacío (útil si se carga desde archivo o BD)
    public ConfiguracionConexiones() {
    }

    // Getters y setters
    public int getMaxUsuariosConectados() {
        return maxUsuariosConectados;
    }

    public void setMaxUsuariosConectados(int maxUsuariosConectados) {
        this.maxUsuariosConectados = maxUsuariosConectados;
    }

    public boolean esLimitado() {
        return esLimitado;
    }

    public void setEsLimitado(boolean esLimitado) {
        this.esLimitado = esLimitado;
    }

    @Override
    public String toString() {
        return "ConfiguracionConexiones{" +
                "maxUsuariosConectados=" + maxUsuariosConectados +
                ", esLimitado=" + esLimitado +
                '}';
    }
}
