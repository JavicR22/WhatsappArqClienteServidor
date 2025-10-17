package org.example.configuracioConexion;

import java.util.HashSet;
import java.util.Set;

public class AudioSession {
    String audioId;
    String receptor;
    StringBuilder buffer;
    int expectedChunks;
    Set<Integer> receivedChunks;
    long startTime;

    AudioSession(String audioId, String receptor, int expectedChunks) {
        this.audioId = audioId;
        this.receptor = receptor;
        this.expectedChunks = expectedChunks;
        this.buffer = new StringBuilder();
        this.receivedChunks = new HashSet<>();
        this.startTime = System.currentTimeMillis();
    }

    boolean isComplete() {
        return receivedChunks.size() == expectedChunks;
    }

    boolean isExpired() {
        return System.currentTimeMillis() - startTime > 300000; // 5 minutos timeout
    }
}
