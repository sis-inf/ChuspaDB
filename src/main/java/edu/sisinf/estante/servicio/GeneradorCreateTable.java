// ============================================================
// ARCHIVO: GeneradorCreateTable.java
// RUTA: src/main/java/edu/sisinf/estante/servicio/GeneradorCreateTable.java
// ============================================================
package edu.sisinf.estante.servicio;

import edu.sisinf.estante.dao.IConexionDAO;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera una sentencia DDL CREATE TABLE a partir de la estructura de una tabla existente.
 *
 * <p>Utiliza {@link DatabaseMetaData} para inspeccionar columnas, tipos y restricciones
 * de clave primaria, y luego construye una cadena CREATE TABLE sintácticamente correcta
 * para el motor de base de datos objetivo (MySQL o SQLite).</p>
 *
 * <p>Útil para ingeniería inversa: replicar la estructura de una tabla en otro
 * servidor o guardar el esquema como un script.</p>
 */
public class GeneradorCreateTable {

    /** Constructor privado — clase utilitaria, no instanciable. */
    private GeneradorCreateTable() {}

    /**
     * Genera una sentencia DDL CREATE TABLE para la tabla dada.
     *
     * @param tableName nombre de la tabla a la que se le aplicará ingeniería inversa
     * @param conexion  datos de conexión utilizados para abrir la sesión JDBC
     * @param dao       implementación del DAO que coincide con el motor objetivo
     * @param motor     motor de base de datos objetivo (MYSQL o SQLITE)
     * @return cadena DDL que comienza con {@code CREATE TABLE tableName (}
     * @throws SQLException si no se puede recuperar la metadatos
     */
    public static String generar(String tableName, Conexion conexion,
                                 IConexionDAO dao, TipoMotor motor) throws SQLException {

        try (Connection connection = dao.abrir(conexion)) {
            DatabaseMetaData metadata = connection.getMetaData();

            List<String> primaryKeys = getPrimaryKeys(metadata, tableName);
            List<String> columnDefinitions = getColumnDefinitions(metadata, tableName, motor);

            return buildDDL(tableName, columnDefinitions, primaryKeys, motor);
        }
    }
    // Metodos ayudantes privados (Helpers)
    /**
     * Recupera los nombres de las columnas que forman la clave primaria para la tabla dada.
     *
     * @param metadata  metadatos de la base de datos
     * @param tableName tabla a inspeccionar
     * @return lista con los nombres de las columnas de la clave primaria
     * @throws SQLException si falla el acceso a los metadatos
     */
    private static List<String> getPrimaryKeys(DatabaseMetaData metadata,
                                               String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet rs = metadata.getPrimaryKeys(null, null, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }

        return primaryKeys;
    }

    /**
     * Construye las cadenas de definición de columnas a partir de los metadatos de la tabla.
     *
     * @param metadata  metadatos de la base de datos
     * @param tableName tabla a inspeccionar
     * @param motor     motor objetivo, utilizado para dar formato a los nombres de los tipos
     * @return lista de cadenas de definición de columnas (ej. {@code id INTEGER NOT NULL})
     * @throws SQLException si falla el acceso a los metadatos
     */
    private static List<String> getColumnDefinitions(DatabaseMetaData metadata,
                                                     String tableName,
                                                     TipoMotor motor) throws SQLException {
        List<String> definitions = new ArrayList<>();

        try (ResultSet rs = metadata.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String typeName   = rs.getString("TYPE_NAME");
                int    columnSize = rs.getInt("COLUMN_SIZE");
                String nullable   = rs.getString("IS_NULLABLE");

                String sqlType = buildSqlType(typeName, columnSize, motor);
                String nullConstraint = "NO".equals(nullable) ? " NOT NULL" : "";

                definitions.add("    " + columnName + " " + sqlType + nullConstraint);
            }
        }

        return definitions;
    }

    /**
     * Da formato a la cadena del tipo SQL de acuerdo con el motor objetivo.
     *
     * @param typeName   nombre crudo del tipo proveniente de los metadatos
     * @param columnSize tamaño de la columna (utilizado para VARCHAR/CHAR)
     * @param motor      motor objetivo
     * @return cadena del tipo SQL formateada
     */
    private static String buildSqlType(String typeName, int columnSize, TipoMotor motor) {
        String upperType = typeName.toUpperCase();

        if (motor == TipoMotor.SQLITE) {
            // SQLite utiliza afinidad de tipos flexible
            if (upperType.contains("INT"))                 return "INTEGER";
            if (upperType.contains("CHAR")
                    || upperType.contains("TEXT")
                    || upperType.contains("CLOB")) return "TEXT";
            if (upperType.contains("REAL")
                    || upperType.contains("FLOA")
                    || upperType.contains("DOUB")) return "REAL";
            if (upperType.contains("BLOB"))    return "BLOB";
            return "NUMERIC";
        }

        // MySQL: mantiene el tipo original con tamaño cuando corresponda
        if ((upperType.contains("VARCHAR") || upperType.contains("CHAR"))
                && columnSize > 0) {
            return typeName + "(" + columnSize + ")";
        }

        return typeName;
    }

    /**
     * Ensambla la cadena DDL final del CREATE TABLE.
     *
     * @param tableName         nombre de la tabla
     * @param columnDefinitions lista de cadenas con las definiciones de columnas
     * @param primaryKeys       lista con los nombres de las columnas de la clave primaria
     * @param motor             motor objetivo
     * @return cadena DDL completa del CREATE TABLE
     */
    private static String buildDDL(String tableName,
                                   List<String> columnDefinitions,
                                   List<String> primaryKeys,
                                   TipoMotor motor) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(tableName).append(" (\n");

        ddl.append(String.join(",\n", columnDefinitions));

        if (!primaryKeys.isEmpty()) {
            ddl.append(",\n    PRIMARY KEY (")
               .append(String.join(", ", primaryKeys))
               .append(")");
        }

        ddl.append("\n)");

        if (motor == TipoMotor.MYSQL) {
            ddl.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }

        ddl.append(";");

        return ddl.toString();
    }
}