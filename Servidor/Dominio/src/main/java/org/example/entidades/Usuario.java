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


}
