package edu.sisinf.chuspadb.core;

import edu.sisinf.chuspadb.config.DBConfig;
import edu.sisinf.chuspadb.dto.QueryResult;
import edu.sisinf.chuspadb.util.SqlValidator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proveedor de conexiones JDBC.
 * Abre conexiones a partir de un {@link DBConfig}.
 */
public class ConnectionProvider {

    private static final Logger LOGGER = Logger.getLogger(ConnectionProvider.class.getName());

    /**
     * Abre una conexión JDBC usando la configuración dada.
     *
     * @param config Configuración de conexión
     * @return Conexión JDBC activa
     * @throws IllegalArgumentException Si la configuración, el driver o la URL son nulos o vacíos
     * @throws SQLException             Si no se puede establecer la conexión
     */
    public static Connection abrir(DBConfig config) throws SQLException {
        if (config == null) {
            throw new IllegalArgumentException("La configuración de conexión (DBConfig) no puede ser nula.");
        }
        if (config.getDriver() == null || config.getDriver().trim().isEmpty()) {
            throw new IllegalArgumentException("El driver JDBC no puede ser nulo ni estar vacío.");
        }
        if (config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de conexión JDBC no puede ser nula ni estar vacía.");
        }

        LOGGER.log(Level.FINE, "Abriendo conexión JDBC con driver: {0}", config.getDriver());

        try {
            Class.forName(config.getDriver());
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver no encontrado: {0}", config.getDriver());
            throw new SQLException(
                    "Driver no encontrado: " + config.getDriver(),
                    e
            );
        }

        Connection connection = DriverManager.getConnection(
                config.getUrl(),
                config.getUsuario(),
                config.getPassword()
        );

        if (connection == null || connection.isClosed()) {
            LOGGER.log(Level.SEVERE, "No se pudo obtener una conexión JDBC activa para: {0}", config.getUrl());
            throw new SQLException("La conexión JDBC obtenida es nula o se encuentra cerrada.");
        }

        LOGGER.log(Level.FINE, "Conexión JDBC abierta correctamente.");
        return connection;
    }

    /**
     * Ejecuta una sentencia SELECT y retorna el resultado encapsulado en un {@link QueryResult}.
     *
     * @param connection conexión JDBC activa sobre la que se ejecuta la consulta
     * @param sql        sentencia SQL de lectura (debe ser SELECT)
     * @return {@link QueryResult} con las columnas y filas del resultado
     * @throws IllegalArgumentException si la conexión es nula, o la sentencia es nula, vacía o no es un SELECT válido
     * @throws ErrorQuery               si la conexión está cerrada o ocurre un error SQL durante la ejecución de la consulta
     */
    public static QueryResult executeSelect(Connection connection, String sql)
            throws IllegalArgumentException, ErrorQuery {
        if (connection == null) {
            throw new IllegalArgumentException("La conexión JDBC no puede ser nula.");
        }
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("La sentencia SQL no puede ser nula ni estar vacía.");
        }
        if (!SqlValidator.esLectura(sql)) {
            throw new IllegalArgumentException(
                    "executeSelect() solo acepta sentencias SELECT. Use executeUpdate() para escrituras."
            );
        }

        LOGGER.log(Level.FINE, "Ejecutando consulta SELECT: {0}", sql);

        try {
            if (connection.isClosed()) {
                throw new SQLException("La conexión JDBC se encuentra cerrada.");
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metadata.getColumnName(i));
                }

                List<List<Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(resultSet.getObject(i));
                    }
                    rows.add(row);
                }

                LOGGER.log(Level.FINE, "Consulta SELECT ejecutada. Filas obtenidas: {0}", rows.size());
                return QueryResult.of(columns, rows);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error SQL al ejecutar SELECT: {0}", e.getMessage());
            throw new ErrorQuery("Error al ejecutar la sentencia SELECT: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta una sentencia de escritura (INSERT, UPDATE, DELETE) y retorna
     * el número de filas afectadas.
     *
     * @param connection conexión JDBC activa sobre la que se ejecuta la sentencia
     * @param sql        sentencia SQL de escritura (INSERT, UPDATE o DELETE)
     * @return número de filas afectadas por la sentencia
     * @throws IllegalArgumentException si la conexión es nula, o la sentencia es nula, vacía o es un SELECT
     * @throws ErrorQuery               si la conexión está cerrada o ocurre un error SQL durante la ejecución de la sentencia
     */
    public static int executeUpdate(Connection connection, String sql)
            throws IllegalArgumentException, ErrorQuery {
        if (connection == null) {
            throw new IllegalArgumentException("La conexión JDBC no puede ser nula.");
        }
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("La sentencia SQL no puede ser nula ni estar vacía.");
        }
        if (SqlValidator.esLectura(sql)) {
            throw new IllegalArgumentException(
                    "executeUpdate() no permite sentencias SELECT de selección. " +
                    "Use executeSelect() para consultas. Sentencia rechazada: [" + sql + "]"
            );
        }

        LOGGER.log(Level.FINE, "Ejecutando sentencia de escritura: {0}", sql);

        try {
            if (connection.isClosed()) {
                throw new SQLException("La conexión JDBC se encuentra cerrada.");
            }

            try (Statement statement = connection.createStatement()) {
                int filasAfectadas = statement.executeUpdate(sql);
                LOGGER.log(Level.FINE, "Escritura ejecutada. Filas afectadas: {0}", filasAfectadas);
                return filasAfectadas;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error SQL al ejecutar sentencia de escritura: {0}", e.getMessage());
            throw new ErrorQuery(
                    "Error al ejecutar la sentencia de escritura: " + e.getMessage(),
                    e
            );
        }
    }
}
