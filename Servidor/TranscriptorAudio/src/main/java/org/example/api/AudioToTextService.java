package org.example.api;


import java.io.File;

public interface AudioToTextService {
    String transcribe(File audioFile) throws Exception;
}
