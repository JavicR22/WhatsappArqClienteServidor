package org.example.persistencia.adaptador;

import org.example.persistencia.properties.ApplicationProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLAdaptador implements AdaptadorBaseDatos{
    @Override
    public Connection obtenerConexion() throws SQLException {
        String url = ApplicationProperties.get("db.url");
        String user = ApplicationProperties.get("db.user");
        String password = ApplicationProperties.get("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
