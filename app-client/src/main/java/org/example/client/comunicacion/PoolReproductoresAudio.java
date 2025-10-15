package org.example.client.comunicacion;

import java.util.ArrayDeque;
import java.util.Queue;

public class PoolReproductoresAudio {
    private final Queue<ReproductorAudio> pool = new ArrayDeque<>();
    private final int maxTamano;

    public PoolReproductoresAudio(int maxTamano) {
        this.maxTamano = maxTamano;
    }

    public synchronized ReproductorAudio adquirir() {
        if (!pool.isEmpty()) return pool.poll();
        return new ReproductorAudio();
    }

    public synchronized void liberar(ReproductorAudio r) {
        if (pool.size() < maxTamano) pool.offer(r);
    }
}
