package org.example.persistencia.adaptador;

import org.example.persistencia.properties.ApplicationProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLAdaptador implements AdaptadorBaseDatos{
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // ✅ carga explícita del driver
            System.out.println("✅ Driver MySQL JDBC cargado correctamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se pudo cargar el driver MySQL: " + e.getMessage());
        }
    }
    @Override
    public Connection obtenerConexion() throws SQLException {
        String url = ApplicationProperties.get("db.url");
        String user = ApplicationProperties.get("db.user");
        String password = ApplicationProperties.get("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
