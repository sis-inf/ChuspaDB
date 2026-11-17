package edu.sisinf.chuspadb.servicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.sisinf.chuspadb.core.ErrorQuery;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import edu.sisinf.chuspadb.util.SqlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio centralizado para la ejecución de consultas SQL.
 */
public class EjecutorQuery {
    private static final Logger logger = LoggerFactory.getLogger(EjecutorQuery.class);

    /**
     * Ejecuta una consulta SQL en la conexión proporcionada sin alterar el ciclo de vida de la conexión.
     *
     * @param conexion Conexión activa a la base de datos (responsabilidad del consumidor).
     * @param sql      Cadena SQL a ejecutar.
     * @return ResultadoQuery con los datos obtenidos o conteo de filas afectadas.
     * @throws ErrorQuery si faltan la conexión o la sentencia SQL, si la conexión está
     *                    cerrada o si falla la ejecución.
     */
    public ResultadoQuery ejecutar(Connection conexion, String sql) throws ErrorQuery {
        if (conexion == null) {
            throw new ErrorQuery("Se requiere una conexión JDBC para ejecutar la consulta.");
        }
        if (sql == null || sql.isBlank()) {
            throw new ErrorQuery("La sentencia SQL no puede estar vacía.");
        }

        SqlValidator.TipoQuery tipoQuery = SqlValidator.tipo(sql);
        long tiempoInicio = System.currentTimeMillis();

        try {
            if (conexion.isClosed()) {
                throw new ErrorQuery("La conexión JDBC está cerrada.");
            }
            logger.debug("Ejecutando consulta de tipo {}.", tipoQuery);

            if (tipoQuery == SqlValidator.TipoQuery.SELECT) {

                try (Statement stmt = conexion.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    int numColumnas = metaData.getColumnCount();

                    List<String> columnas = new ArrayList<>();
                    for (int i = 1; i <= numColumnas; i++) {
                        columnas.add(metaData.getColumnLabel(i));
                    }

                    List<List<Object>> filas = new ArrayList<>();
                    while (rs.next()) {
                        List<Object> fila = new ArrayList<>();
                        for (int i = 1; i <= numColumnas; i++) {
                            fila.add(rs.getObject(i));
                        }
                        filas.add(fila);
                    }

                    long tiempoMs = System.currentTimeMillis() - tiempoInicio;
                    return ResultadoQuery.deLectura(columnas, filas, tiempoMs);
                }

            } else {

                try (Statement stmt = conexion.createStatement()) {
                    int filasAfectadas = stmt.executeUpdate(sql);

                    long tiempoMs = System.currentTimeMillis() - tiempoInicio;
                    return ResultadoQuery.deEscritura(filasAfectadas, tiempoMs);
                }
            }

        } catch (SQLException e) {
            logger.error("Falló la consulta de tipo {} (SQLState: {}).", tipoQuery, e.getSQLState());
            throw new ErrorQuery(e.getMessage(), e);
        }
    }
}
