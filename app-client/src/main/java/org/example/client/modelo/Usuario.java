package org.example.client.modelo;

import java.io.Serializable;

/**
 * MODELO DE DOMINIO
 * Representa un usuario del sistema.
 * Contiene la información básica de identidad y autenticación.
 */
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String nombre;
    private String correo;
    private String contrasena;
    private String rol;
    
    public Usuario() {
    }
    
    public Usuario(String id, String nombre, String correo, String contrasena, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getCorreo() {
        return correo;
    }
    
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    
    public String getContrasena() {
        return contrasena;
    }
    
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public String getRol() {
        return rol;
    }
    
    public void setRol(String rol) {
        this.rol = rol;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
}
