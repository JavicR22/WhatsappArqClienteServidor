package org.example.persistencia.adaptador;

import java.sql.Connection;
import java.sql.SQLException;

public interface AdaptadorBaseDatos {
    Connection obtenerConexion() throws SQLException;
}
