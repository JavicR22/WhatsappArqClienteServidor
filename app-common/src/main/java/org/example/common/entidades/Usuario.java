package org.example.common.entidades;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario implements Serializable {
    private String id;
    private String nombre;
    private String correo;
    private String contrasena; // hash de la contraseña
    private String salt;       // valor aleatorio único por usuario

    // NUEVOS ATRIBUTOS
    private String direccionIP;
    private LocalDateTime fechaRegistro;

    public Usuario() {}

    public Usuario(String id, String nombre, String correo, String contrasena, String salt) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.salt = salt;
    }

    // Nuevo constructor con los campos extra
    public Usuario(String id, String nombre, String correo, String contrasena, String salt,
                   String direccionIP, LocalDateTime fechaRegistro) {
        this(id, nombre, correo, contrasena, salt);
        this.direccionIP = direccionIP;
        this.fechaRegistro = fechaRegistro;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public String getSalt() { return salt; }

    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public void setSalt(String salt) { this.salt = salt; }

    // Getters/Setters nuevos
    public String getDireccionIP() { return direccionIP; }
    public void setDireccionIP(String direccionIP) { this.direccionIP = direccionIP; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
