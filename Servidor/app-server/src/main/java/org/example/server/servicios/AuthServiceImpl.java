package org.example.server.servicios;

import org.example.common.entidades.Usuario;
import org.example.common.servicios.AuthService;
import org.example.common.repositorios.UsuarioRepository;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ✅ REGISTRO DE NUEVO USUARIO
    @Override
    public boolean registrar(Usuario usuario, String contrasena) {
        if (usuarioRepository.buscarPorCorreo(usuario.getCorreo()).isPresent()) {
            System.out.println("⚠️ El correo ya está registrado: " + usuario.getCorreo());
            return false;
        }

        String salt = generarSalt();
        String hash = hashContraseña(contrasena, salt);

        usuario.setId(UUID.randomUUID().toString());
        usuario.setSalt(salt);
        usuario.setContrasena(hash);

        usuarioRepository.guardar(usuario);
        System.out.println("✅ Usuario registrado correctamente: " + usuario.getCorreo());
        return true;
    }

    // ✅ LOGIN
    @Override
    public Optional<Usuario> login(String correo, String contrasena) {
        Optional<Usuario> op = usuarioRepository.buscarPorCorreo(correo);
        if (op.isEmpty()) {
            System.out.println("⚠️ Usuario no encontrado: " + correo);
            return Optional.empty();
        }

        Usuario usuario = op.get();
        String salt = usuario.getSalt();
        String hashCalculado = hashContraseña(contrasena, salt);

        if (hashCalculado.equals(usuario.getContrasena())) {
            System.out.println("🔓 Autenticación exitosa para: " + correo);
            return Optional.of(usuario);
        } else {
            System.out.println("❌ Contraseña incorrecta para: " + correo);
            return Optional.empty();
        }
    }

    // ✅ LOGOUT
    @Override
    public void logout(String usuarioId) {
        System.out.println("👋 Usuario desconectado (logout): " + usuarioId);
    }

    // ✅ VERIFICAR SI EXISTE CORREO
    @Override
    public boolean existeCorreo(String correo) {
        return usuarioRepository.buscarPorCorreo(correo).isPresent();
    }

    // 🔒 GENERAR SALT ALEATORIO
    private String generarSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // 🔐 GENERAR HASH SHA-256 + SALT
    private String hashContraseña(String contrasena, String saltBase64) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            digest.update(salt);
            byte[] hashed = digest.digest(contrasena.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar hash de contraseña", e);
        }
    }
}
