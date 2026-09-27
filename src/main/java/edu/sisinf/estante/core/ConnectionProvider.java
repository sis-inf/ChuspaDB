package edu.sisinf.estante.core;

import edu.sisinf.estante.config.DBConfig;
import edu.sisinf.estante.dto.QueryResult;
import edu.sisinf.estante.util.SqlValidator;
import edu.sisinf.estante.core.ErrorQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Proveedor de conexiones JDBC.
 * Abre conexiones a partir de un {@link DBConfig}.
 */
public class ConnectionProvider {

    /**
     * Abre una conexión JDBC usando la configuración dada.
     *
     * @param config Configuración de conexión
     * @return Conexión JDBC activa
     * @throws SQLException Si no se puede establecer la conexión
     */
    public static Connection abrir(DBConfig config) throws SQLException {
        try {
            Class.forName(config.getDriver());
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Driver no encontrado: " + config.getDriver(),
                    e
            );
        }

        return DriverManager.getConnection(
                config.getUrl(),
                config.getUsuario(),
                config.getPassword()
        );
    }

    /**
     * Ejecuta una sentencia SELECT y retorna el resultado encapsulado en un {@link QueryResult}.
     *
     * @param connection conexión JDBC activa sobre la que se ejecuta la consulta
     * @param sql        sentencia SQL de lectura (debe ser SELECT)
     * @return {@link QueryResult} con las columnas y filas del resultado
     * @throws IllegalArgumentException si la sentencia no es un SELECT
     * @throws ErrorQuery               si ocurre un error durante la ejecución SQL
     */
    public static QueryResult executeSelect(Connection connection, String sql) throws IllegalArgumentException, ErrorQuery {
        if (!SqlValidator.esLectura(sql)) {
            throw new IllegalArgumentException(
                    "executeSelect() solo acepta sentencias SELECT. Use executeUpdate() para escrituras."
            );
        }

        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

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

            return QueryResult.of(columns, rows);

        } catch (SQLException e) {
            throw new ErrorQuery("Error al ejecutar la sentencia SELECT: " + e.getMessage(), e);
        } finally {
             // Cierre seguro de recursos JDBC para evitar fugas de conexión
            if (resultSet != null) {
                try { resultSet.close(); } catch (SQLException ignored) {}
            }
            if (statement != null) {
                try { statement.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Ejecuta una sentencia de escritura (INSERT, UPDATE, DELETE) y retorna
     * el número de filas afectadas.
     *
     * @param connection conexión JDBC activa sobre la que se ejecuta la sentencia
     * @param sql        sentencia SQL de escritura (INSERT, UPDATE o DELETE)
     * @return número de filas afectadas por la sentencia
     * @throws IllegalArgumentException si la sentencia es un SELECT
     * @throws ErrorQuery               si ocurre un error durante la ejecución SQL
     */
 public static int executeUpdate(Connection connection, String sql)
        throws IllegalArgumentException, ErrorQuery {

    if (sql == null) {
        throw new IllegalArgumentException("La sentencia SQL no puede ser nula.");
    }

    if (SqlValidator.esLectura(sql)) {
        throw new IllegalArgumentException(
                "executeUpdate() no permite sentencias SELECT de selección. " +
                "Use executeSelect() para consultas. Sentencia rechazada: [" + sql + "]"
        );
    }

    try (Statement statement = connection.createStatement()) {
        return statement.executeUpdate(sql);
    } catch (SQLException e) {
        throw new ErrorQuery(
                "Error al ejecutar la sentencia de escritura: " + e.getMessage(),
                e
        );
    }
} 
}
