package org.example.entidades;

public class Usuario {
    private String username;
    private String email;
    private String password;
    private String rutaFoto;
    private String direccionIP;

    public Usuario(String username, String email, String password,
                   String rutaFoto, String direccionIP){
        this.direccionIP=direccionIP;
        this.email=email;
        this.password=password;
        this.rutaFoto=rutaFoto;
        this.username=username;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }

    public String getDireccionIP() {
        return direccionIP;
    }

    public void setDireccionIP(String direccionIP) {
        this.direccionIP = direccionIP;
    }
}
