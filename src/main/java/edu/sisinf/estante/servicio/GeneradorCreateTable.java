// ============================================================
// ARCHIVO: GeneradorCreateTable.java
// RUTA: src/main/java/edu/sisinf/chuspadb/servicio/GeneradorCreateTable.java
// ============================================================
package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Genera una sentencia DDL CREATE TABLE a partir de la estructura de una tabla existente.
 *
 * <p>Utiliza {@link DatabaseMetaData} para inspeccionar columnas, tipos, columnas
 * autoincrementales y restricciones de clave primaria, y luego construye una cadena
 * CREATE TABLE sintácticamente correcta para el motor de base de datos objetivo
 * (MySQL, PostgreSQL o SQLite).</p>
 *
 * <p>Adapta por motor: el autoincremento (AUTO_INCREMENT, SERIAL o
 * INTEGER PRIMARY KEY AUTOINCREMENT), los tipos de texto largo, los booleanos
 * y los tipos de fecha.</p>
 *
 * <p>Útil para ingeniería inversa: replicar la estructura de una tabla en otro
 * servidor o guardar el esquema como un script.</p>
 */
public class GeneradorCreateTable {

    /** Máximo de caracteres de un VARCHAR en PostgreSQL. */
    private static final int POSTGRESQL_MAX_VARCHAR = 10_485_760;

    /** Máximo de caracteres de un VARCHAR en MySQL con utf8mb4 (límite de 65535 bytes por fila). */
    private static final int MYSQL_MAX_VARCHAR = 16_383;

    /** Constructor privado — clase utilitaria, no instanciable. */
    private GeneradorCreateTable() {}

    /**
     * Genera una sentencia DDL CREATE TABLE para la tabla dada.
     *
     * @param tableName nombre de la tabla a la que se le aplicará ingeniería inversa
     * @param conexion  datos de conexión utilizados para abrir la sesión JDBC
     * @param dao       implementación del DAO que coincide con el motor objetivo
     * @param motor     motor de base de datos objetivo (MYSQL, POSTGRESQL o SQLITE)
     * @return cadena DDL que comienza con {@code CREATE TABLE tableName (}
     * @throws SQLException si no se puede recuperar la metadatos
     */
    public static String generar(String tableName, Conexion conexion,
                                 IConexionDAO dao, TipoMotor motor) throws SQLException {

        try (Connection connection = dao.abrir(conexion)) {
            DatabaseMetaData metadata = connection.getMetaData();

            List<String> primaryKeys = getPrimaryKeys(metadata, tableName);
            Set<String> autoincrementales = getAutoincrementColumns(metadata, tableName);

            // SQLite solo admite AUTOINCREMENT junto a una PRIMARY KEY de una sola columna,
            // declarada en la propia columna ("INTEGER PRIMARY KEY AUTOINCREMENT").
            String pkEnLinea = null;
            if (motor == TipoMotor.SQLITE
                    && primaryKeys.size() == 1
                    && autoincrementales.contains(primaryKeys.get(0))) {
                pkEnLinea = primaryKeys.get(0);
            }

            List<String> columnDefinitions =
                    getColumnDefinitions(metadata, tableName, motor, autoincrementales, pkEnLinea);

            // Si la PK ya quedó declarada en la columna, no se repite al final de la tabla.
            List<String> pkDeTabla = (pkEnLinea == null) ? primaryKeys : new ArrayList<>();

            return buildDDL(tableName, columnDefinitions, pkDeTabla, motor);
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
     * Recupera los nombres de las columnas autoincrementales de la tabla dada,
     * según la columna estándar {@code IS_AUTOINCREMENT} de los metadatos.
     *
     * @param metadata  metadatos de la base de datos
     * @param tableName tabla a inspeccionar
     * @return conjunto con los nombres de las columnas autoincrementales
     * @throws SQLException si falla el acceso a los metadatos
     */
    private static Set<String> getAutoincrementColumns(DatabaseMetaData metadata,
                                                       String tableName) throws SQLException {
        Set<String> columnas = new HashSet<>();

        try (ResultSet rs = metadata.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                if ("YES".equals(rs.getString("IS_AUTOINCREMENT"))) {
                    columnas.add(rs.getString("COLUMN_NAME"));
                }
            }
        }

        return columnas;
    }

    /**
     * Construye las cadenas de definición de columnas a partir de los metadatos de la tabla.
     *
     * @param metadata          metadatos de la base de datos
     * @param tableName         tabla a inspeccionar
     * @param motor             motor objetivo, utilizado para dar formato a los nombres de los tipos
     * @param autoincrementales nombres de las columnas autoincrementales
     * @param pkEnLinea         columna que en SQLite se declara como
     *                          {@code INTEGER PRIMARY KEY AUTOINCREMENT}, o {@code null} si no aplica
     * @return lista de cadenas de definición de columnas (ej. {@code id INTEGER NOT NULL})
     * @throws SQLException si falla el acceso a los metadatos
     */
    private static List<String> getColumnDefinitions(DatabaseMetaData metadata,
                                                     String tableName,
                                                     TipoMotor motor,
                                                     Set<String> autoincrementales,
                                                     String pkEnLinea) throws SQLException {
        List<String> definitions = new ArrayList<>();

        try (ResultSet rs = metadata.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String typeName   = rs.getString("TYPE_NAME");
                int    columnSize = rs.getInt("COLUMN_SIZE");
                String nullable   = rs.getString("IS_NULLABLE");

                String definicion;

                if (columnName.equals(pkEnLinea)) {
                    // Solo ocurre en SQLite
                    definicion = "INTEGER PRIMARY KEY AUTOINCREMENT";
                } else {
                    // En SQLite el autoincremento solo es válido en la PK en línea;
                    // en cualquier otro caso la columna se genera como una normal.
                    boolean auto = motor != TipoMotor.SQLITE
                            && autoincrementales.contains(columnName);

                    String sqlType = auto
                            ? buildAutoincrementType(typeName, motor)
                            : buildSqlType(typeName, columnSize, motor);
                    String nullConstraint = "NO".equals(nullable) ? " NOT NULL" : "";
                    String autoSuffix = (auto && motor == TipoMotor.MYSQL) ? " AUTO_INCREMENT" : "";

                    definicion = sqlType + nullConstraint + autoSuffix;
                }

                definitions.add("    " + quoteIdentifier(columnName, motor) + " " + definicion);
            }
        }

        return definitions;
    }

    /**
     * Devuelve el tipo de una columna autoincremental para MySQL o PostgreSQL.
     *
     * <p>PostgreSQL usa los pseudotipos SERIAL; MySQL usa un tipo entero seguido de
     * {@code AUTO_INCREMENT} (el sufijo lo agrega quien llama).</p>
     *
     * @param typeName nombre crudo del tipo proveniente de los metadatos
     * @param motor    motor objetivo (MYSQL o POSTGRESQL)
     * @return tipo entero o SERIAL correspondiente
     */
    private static String buildAutoincrementType(String typeName, TipoMotor motor) {
        String upperType = typeName.toUpperCase();

        if (motor == TipoMotor.POSTGRESQL) {
            if (upperType.contains("BIGINT")
                    || upperType.contains("BIGSERIAL")
                    || upperType.equals("INT8")) return "BIGSERIAL";
            if (upperType.contains("SMALLINT")
                    || upperType.contains("TINYINT")
                    || upperType.contains("SMALLSERIAL")
                    || upperType.equals("INT2")) return "SMALLSERIAL";
            return "SERIAL";
        }

        // MySQL: si el origen era SERIAL de PostgreSQL se convierte a su entero equivalente;
        // si ya era un entero de MySQL (INT, BIGINT, TINYINT...) se conserva tal cual.
        if (upperType.contains("BIGSERIAL"))   return "BIGINT";
        if (upperType.contains("SMALLSERIAL")) return "SMALLINT";
        if (upperType.contains("SERIAL"))      return "INT";
        return typeName;
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
            return buildSqliteType(upperType);
        }

        if (motor == TipoMotor.POSTGRESQL) {
            return buildPostgresqlType(upperType, columnSize);
        }

        return buildMysqlType(typeName, upperType, columnSize);
    }

    /**
     * Convierte tipos comunes de JDBC y otros motores a tipos válidos de SQLite.
     */
    private static String buildSqliteType(String upperType) {
        // SQLite no tiene tipo booleano: se guarda como entero 0/1
        if (upperType.contains("BOOL") || upperType.equals("BIT")) return "INTEGER";

        // SQLite no tiene tipos de fecha nativos: se conserva el nombre declarado
        if (upperType.contains("TIMESTAMP")) return "TIMESTAMP";
        if (upperType.contains("DATETIME"))  return "DATETIME";
        if (upperType.equals("DATE") || upperType.equals("TIME")) return upperType;

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

    /**
     * Convierte tipos comunes de JDBC y otros motores a tipos válidos de MySQL.
     */
    private static String buildMysqlType(String typeName, String upperType, int columnSize) {
        // Texto largo: CLOB y LONGVARCHAR no existen en MySQL
        if (upperType.contains("CLOB") || upperType.contains("LONGVARCHAR")) return "LONGTEXT";

        // VARCHAR sin límite (PostgreSQL) o más grande de lo que cabe en una fila
        if (upperType.contains("VARCHAR") && columnSize > MYSQL_MAX_VARCHAR) return "LONGTEXT";

        // Booleanos
        if (upperType.equals("BOOL") || upperType.equals("BOOLEAN")) return "BOOLEAN";

        // Fechas: MySQL no maneja zona horaria en estos tipos
        if (upperType.equals("TIMESTAMPTZ")) return "DATETIME";
        if (upperType.equals("TIMETZ"))      return "TIME";

        // Mantiene el tipo original con tamaño cuando corresponda
        if ((upperType.contains("VARCHAR") || upperType.contains("CHAR"))
                && columnSize > 0) {
            return typeName + "(" + columnSize + ")";
        }

        return typeName;
    }

    /**
     * Convierte tipos comunes de JDBC y otros motores a tipos válidos de PostgreSQL.
     */
    private static String buildPostgresqlType(String upperType, int columnSize) {
        if (upperType.contains("SMALLSERIAL")) return "SMALLSERIAL";
        if (upperType.contains("BIGSERIAL"))   return "BIGSERIAL";
        if (upperType.contains("SERIAL"))      return "SERIAL";

        // Booleanos: BIT(1) de MySQL equivale a BOOLEAN; un BIT de más bits es una cadena de bits
        if (upperType.contains("BOOL")) return "BOOLEAN";
        if (upperType.equals("BIT")) {
            return columnSize > 1 ? "BIT(" + columnSize + ")" : "BOOLEAN";
        }

        // Texto largo
        if (upperType.contains("TEXT")
                || upperType.contains("CLOB")
                || upperType.contains("LONGVARCHAR")) return "TEXT";
        if (upperType.contains("VARCHAR")
                || upperType.contains("CHARACTER VARYING")) {
            if (columnSize > POSTGRESQL_MAX_VARCHAR) return "TEXT";
            return columnSize > 0 ? "VARCHAR(" + columnSize + ")" : "VARCHAR";
        }
        if (upperType.contains("CHAR") || upperType.contains("BPCHAR")) {
            return columnSize > 0 ? "CHAR(" + columnSize + ")" : "CHAR";
        }

        // Fechas y horas (antes de los enteros para que INTERVAL no se confunda con INT)
        if (upperType.contains("DATETIME")) return "TIMESTAMP";
        if (upperType.contains("TIMESTAMP")
                || upperType.equals("DATE")
                || upperType.equals("TIME")
                || upperType.equals("TIMETZ")
                || upperType.equals("INTERVAL")) return upperType;
        if (upperType.equals("YEAR")) return "SMALLINT";

        if (upperType.contains("BIGINT") || upperType.equals("INT8")) return "BIGINT";
        if (upperType.contains("SMALLINT")
                || upperType.contains("TINYINT")
                || upperType.equals("INT2")) return "SMALLINT";
        if (upperType.contains("INT") || upperType.equals("INT4")) return "INTEGER";
        if (upperType.contains("BLOB")
                || upperType.contains("BINARY")
                || upperType.contains("VARBINARY")) return "BYTEA";
        if (upperType.contains("DOUBLE") || upperType.equals("FLOAT8")) {
            return "DOUBLE PRECISION";
        }
        if (upperType.contains("FLOAT")
                || upperType.contains("REAL")
                || upperType.equals("FLOAT4")) return "REAL";
        return upperType;
    }

    /**
     * Ensambla la cadena DDL final del CREATE TABLE.
     *
     * @param tableName         nombre de la tabla
     * @param columnDefinitions lista de cadenas con las definiciones de columnas
     * @param primaryKeys       lista con los nombres de las columnas de la clave primaria
     *                          que se declaran al final de la tabla
     * @param motor             motor objetivo
     * @return cadena DDL completa del CREATE TABLE
     */
    private static String buildDDL(String tableName,
                                   List<String> columnDefinitions,
                                   List<String> primaryKeys,
                                   TipoMotor motor) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(quoteIdentifier(tableName, motor)).append(" (\n");

        ddl.append(String.join(",\n", columnDefinitions));

        if (!primaryKeys.isEmpty()) {
            ddl.append(",\n    PRIMARY KEY (")
               .append(String.join(", ", primaryKeys.stream()
                       .map(primaryKey -> quoteIdentifier(primaryKey, motor))
                       .toList()))
               .append(")");
        }

        ddl.append("\n)");

        if (motor == TipoMotor.MYSQL) {
            ddl.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }

        ddl.append(";");

        return ddl.toString();
    }

    /**
     * Aplica las comillas de identificador apropiadas para cada motor.
     */
    private static String quoteIdentifier(String identifier, TipoMotor motor) {
        if (motor == TipoMotor.MYSQL) {
            return "`" + identifier.replace("`", "``") + "`";
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
