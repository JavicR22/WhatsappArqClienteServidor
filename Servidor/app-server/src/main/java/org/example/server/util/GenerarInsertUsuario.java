package org.example.server.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Generador de SQL INSERT para tabla 'usuario'.
 * Genera un salt aleatorio y el hash SHA-256 (salt + contraseña),
 * y muestra por consola el INSERT listo para pegar en la base de datos.
 *
 * Valores por defecto:
 *  id = "u1"
 *  nombre = "prueba"
 *  correo = "cliente@uni.edu"
 *  contrasena = "1234"
 *
 * Puedes cambiar las constantes abajo o adaptar para leer argumentos.
 */
public class GenerarInsertUsuario {

    public static void main(String[] args) throws Exception {
        // --- Valore por defecto (cámbialos si quieres) ---
        String id = "u4";                       // id que aparecerá en el INSERT
        String nombre = "prueba4";
        String correo = "cliente4@uni.edu";
        String contrasenaPlano = "1234";

        // --- Generar salt (16 bytes) ---
        SecureRandom rnd = new SecureRandom();
        byte[] saltBytes = new byte[16];
        rnd.nextBytes(saltBytes);
        String saltBase64 = Base64.getEncoder().encodeToString(saltBytes);

        // --- Calcular hash SHA-256 con el mismo método que AuthServiceImpl ---
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(saltBytes); // update con el salt
        byte[] hashBytes = md.digest(contrasenaPlano.getBytes(StandardCharsets.UTF_8));
        String hashBase64 = Base64.getEncoder().encodeToString(hashBytes);

        // --- Escapar/aplicar comillas simples para SQL (si alguna contiene ' -> se reemplaza) ---
        String safeNombre = nombre.replace("'", "''");
        String safeCorreo = correo.replace("'", "''");
        String safeHash = hashBase64.replace("'", "''");
        String safeSalt = saltBase64.replace("'", "''");

        // --- Construir SQL INSERT listo para pegar ---
        String sql = String.format(
                "INSERT INTO usuario (id, nombre, correo, contrasena, salt)%n" +
                        "VALUES (%n" +
                        "    '%s',%n" +
                        "    '%s',%n" +
                        "    '%s',%n" +
                        "    '%s',%n" +
                        "    '%s'%n" +
                        ");",
                id, safeNombre, safeCorreo, safeHash, safeSalt
        );

        // --- Mostrar resultados ---
        System.out.println("=== Valores generados ===");
        System.out.println("ID:           " + id);
        System.out.println("Nombre:       " + nombre);
        System.out.println("Correo:       " + correo);
        System.out.println("Contraseña:   " + contrasenaPlano);
        System.out.println("Salt (Base64): " + saltBase64);
        System.out.println("Hash (Base64): " + hashBase64);
        System.out.println();
        System.out.println("=== INSERT SQL (copiar + pegar en tu BD) ===");
        System.out.println(sql);
    }
}
