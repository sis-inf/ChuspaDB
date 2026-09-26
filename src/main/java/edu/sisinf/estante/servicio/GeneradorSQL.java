package edu.sisinf.estante.servicio;

import java.util.List;
import java.util.StringJoiner;

public class GeneradorSQL {

    public String generarSelect(String tabla, List<String> columnas) {
        return "SELECT " + String.join(", ", columnas) + " FROM " + tabla;
    }

    public String generarInsert(String tabla, List<String> columnas) {

        StringJoiner cols = new StringJoiner(", ");
        StringJoiner valores = new StringJoiner(", ");

        for (String columna : columnas) {
            cols.add(columna);
            valores.add("?");
        }

        return "INSERT INTO " + tabla + " (" + cols + ") VALUES (" + valores + ")";
    }

    public String generarUpdate(String tabla,
                                List<String> columnas,
                                String colPK) {

        StringJoiner set = new StringJoiner(", ");

        for (String columna : columnas) {
            if (!columna.equals(colPK)) {
                set.add(columna + " = ?");
            }
        }

        return "UPDATE " + tabla +
               " SET " + set +
               " WHERE " + colPK + " = ?";
    }

    public String generarDelete(String tabla, String colPK) {
        return "DELETE FROM " + tabla +
               " WHERE " + colPK + " = ?";
    }
}