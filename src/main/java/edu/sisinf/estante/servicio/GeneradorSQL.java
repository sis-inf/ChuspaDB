package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.modelo.ColumnaInfo;
import edu.sisinf.chuspadb.modelo.Tabla;
import java.util.List;
import java.util.StringJoiner;

public class GeneradorSQL {

    private static final System.Logger LOGGER =
            System.getLogger(GeneradorSQL.class.getName());

    public String generarSelect(String tabla, List<String> columnas) {
        validarTexto(tabla, "El nombre de la tabla");
        validarColumnas(columnas);
        LOGGER.log(System.Logger.Level.DEBUG, "Generando SELECT");
        return "SELECT " + String.join(", ", columnas) + " FROM " + tabla;
    }

    public String generarInsert(String tabla, List<String> columnas) {
        validarTexto(tabla, "El nombre de la tabla");
        validarColumnas(columnas);
        LOGGER.log(System.Logger.Level.DEBUG, "Generando INSERT");
        StringJoiner cols = new StringJoiner(", ");
        StringJoiner valores = new StringJoiner(", ");
        for (String columna : columnas) {
            cols.add(columna);
            valores.add("?");
        }
        return "INSERT INTO " + tabla + " (" + cols + ") VALUES (" + valores + ")";
    }

    public String generarUpdate(String tabla, List<String> columnas, String colPK) {
        validarTexto(tabla, "El nombre de la tabla");
        validarColumnas(columnas);
        validarTexto(colPK, "La clave primaria");
        StringJoiner set = new StringJoiner(", ");
        for (String columna : columnas) {
            if (!columna.equals(colPK)) {
                set.add(columna + " = ?");
            }
        }
        if (set.length() == 0) {
            throw new IllegalArgumentException(
                    "Debe existir al menos una columna distinta de la clave primaria");
        }
        LOGGER.log(System.Logger.Level.DEBUG, "Generando UPDATE");
        return "UPDATE " + tabla + " SET " + set + " WHERE " + colPK + " = ?";
    }

    public String generarDelete(String tabla, String colPK) {
        validarTexto(tabla, "El nombre de la tabla");
        validarTexto(colPK, "La clave primaria");
        LOGGER.log(System.Logger.Level.DEBUG, "Generando DELETE");
        return "DELETE FROM " + tabla + " WHERE " + colPK + " = ?";
    }

    // Genera una plantilla INSERT utilizando los metadatos de Tabla.
    public String generarInsertPlantilla(Tabla tabla) {
        validarTabla(tabla);
        StringJoiner columnas = new StringJoiner(", ");
        StringJoiner valores = new StringJoiner(", ");
        for (ColumnaInfo columna : tabla.getColumnas()) {
            columnas.add(columna.nombre());
            valores.add("?");
        }
        LOGGER.log(System.Logger.Level.DEBUG, "Generando plantilla INSERT");
        return "INSERT INTO " + tabla.getNombre()
                + " (" + columnas + ") VALUES (" + valores + ")";
    }

    // Genera una plantilla UPDATE utilizando la clave primaria.
    public String generarUpdatePlantilla(Tabla tabla) {
        validarTabla(tabla);
        String clavePrimaria = null;
        StringJoiner set = new StringJoiner(", ");
        for (ColumnaInfo columna : tabla.getColumnas()) {
            if (columna.esPrimaryKey()) {
                if (clavePrimaria != null) {
                    throw new IllegalArgumentException(
                            "Esta plantilla no admite claves primarias compuestas");
                }
                clavePrimaria = columna.nombre();
            } else {
                set.add(columna.nombre() + " = ?");
            }
        }
        if (clavePrimaria == null) {
            throw new IllegalArgumentException("La tabla debe tener una clave primaria");
        }
        if (set.length() == 0) {
            throw new IllegalArgumentException(
                    "La tabla debe tener al menos una columna además de la clave primaria");
        }
        LOGGER.log(System.Logger.Level.DEBUG, "Generando plantilla UPDATE");
        return "UPDATE " + tabla.getNombre()
                + " SET " + set + " WHERE " + clavePrimaria + " = ?";
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede ser nulo ni vacío");
        }
    }

    private void validarColumnas(List<String> columnas) {
        if (columnas == null || columnas.isEmpty()) {
            throw new IllegalArgumentException("La lista de columnas no puede ser nula ni vacía");
        }
        for (String columna : columnas) {
            validarTexto(columna, "El nombre de la columna");
        }
    }

    private void validarTabla(Tabla tabla) {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla no puede ser nula");
        }
        validarTexto(tabla.getNombre(), "El nombre de la tabla");
        if (tabla.getColumnas() == null || tabla.getColumnas().isEmpty()) {
            throw new IllegalArgumentException("La tabla debe tener columnas");
        }
        for (ColumnaInfo columna : tabla.getColumnas()) {
            if (columna == null) {
                throw new IllegalArgumentException("Los metadatos de columna no pueden ser nulos");
            }
            validarTexto(columna.nombre(), "El nombre de la columna");
        }
    }
}
