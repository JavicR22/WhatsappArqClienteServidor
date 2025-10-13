package org.example.persistencia.fabrica;

import org.example.persistencia.adaptador.AdaptadorBaseDatos;
import org.example.persistencia.adaptador.MySQLAdaptador;
import org.example.persistencia.properties.ApplicationProperties;

public class FabricaBaseDatos {

    private static AdaptadorBaseDatos adaptadorBaseDatos;

    public static AdaptadorBaseDatos getAdapter() {
        if (adaptadorBaseDatos == null) {
            String engine = ApplicationProperties.get("db.engine");
            switch (engine.toLowerCase()) {
                case "mysql":
                    adaptadorBaseDatos = new MySQLAdaptador();
                    break;
                default:
                    throw new IllegalArgumentException("Motor no soportado: " + engine);
            }
        }
        return adaptadorBaseDatos;
    }

}
