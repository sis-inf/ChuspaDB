
package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.modelo.Tabla;
import edu.sisinf.chuspadb.modelo.ColumnaInfo;

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

    // Genera una plantilla INSERT utilizando los metadatos de Tabla.
    public String generarInsertPlantilla(Tabla tabla) {

        if (tabla == null || tabla.getColumnas() == null
                || tabla.getColumnas().isEmpty()) {
            throw new IllegalArgumentException(
                    "La tabla debe tener columnas");
        }

        StringJoiner columnas = new StringJoiner(", ");
        StringJoiner valores = new StringJoiner(", ");

        for (ColumnaInfo columna : tabla.getColumnas()) {
            columnas.add(columna.nombre());
            valores.add("?");
        }

        return "INSERT INTO " + tabla.getNombre()
                + " (" + columnas + ") VALUES (" + valores + ")";
    }

    // Genera una plantilla UPDATE utilizando la clave primaria.
    public String generarUpdatePlantilla(Tabla tabla) {

        if (tabla == null || tabla.getColumnas() == null
                || tabla.getColumnas().isEmpty()) {
            throw new IllegalArgumentException(
                    "La tabla debe tener columnas");
        }

        String clavePrimaria = null;
        StringJoiner set = new StringJoiner(", ");

        for (ColumnaInfo columna : tabla.getColumnas()) {

            if (columna.esPrimaryKey()) {
                clavePrimaria = columna.nombre();
            } else {
                set.add(columna.nombre() + " = ?");
            }
        }

        if (clavePrimaria == null) {
            throw new IllegalArgumentException(
                    "La tabla debe tener una clave primaria");
        }

        if (set.length() == 0) {
            throw new IllegalArgumentException(
                    "La tabla debe tener al menos una columna además de la clave primaria");
        }

        return "UPDATE " + tabla.getNombre()
                + " SET " + set
                + " WHERE " + clavePrimaria + " = ?";
    }
}
