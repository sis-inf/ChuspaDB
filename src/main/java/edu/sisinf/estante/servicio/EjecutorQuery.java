package edu.sisinf.estante.servicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Importaciones basadas en los issues bloqueantes #115 y #118
import edu.sisinf.estante.modelo.ResultadoQuery;
import edu.sisinf.estante.util.SqlValidator;
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
     * @return ResultadoQuery con los datos obtenidos, conteo de filas afectadas o mensaje de error.
     */
    public ResultadoQuery ejecutar(Connection conexion, String sql) {
        // 1. Medición de tiempo: Registrar el inicio
        long tiempoInicio = System.currentTimeMillis();

        try {
            // 2. Clasificación: Determinar el tipo de query
            // (Si SqlValidator devuelve un Enum, asegúrate de usar .name() o .toString() según corresponda)
            SqlValidator.TipoQuery tipoQuery =  SqlValidator.tipo(sql);

            // 3. Ramificación según el tipo
            if (tipoQuery == SqlValidator.TipoQuery.SELECT) {
                
                // --- Ejecución como lectura ---
                // Uso de try-with-resources para garantizar el cierre
                try (Statement stmt = conexion.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    int numColumnas = metaData.getColumnCount();

                    // Extraer los nombres de columnas (índice 1-based)
                    List<String> columnas = new ArrayList<>();
                    for (int i = 1; i <= numColumnas; i++) {
                        columnas.add(metaData.getColumnLabel(i));
                    }

                    // Iterar sobre las filas y construir la lista
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
                
                // --- Ejecución como escritura (DML, DDL, DESCONOCIDO) ---
                // Uso de try-with-resources solo para el Statement
                try (Statement stmt = conexion.createStatement()) {
                    int filasAfectadas = stmt.executeUpdate(sql);
                    
                    long tiempoMs = System.currentTimeMillis() - tiempoInicio;
                    return ResultadoQuery.deEscritura(filasAfectadas, tiempoMs);
                }
            }

       } catch (SQLException e) {
            // 4. Manejo de errores: Registrar el error con SLF4J
            long tiempoMs = System.currentTimeMillis() - tiempoInicio;
            logger.error("Error al ejecutar la consulta SQL: {}", sql, e);
            return ResultadoQuery.deError(e.getMessage(), tiempoMs);
        }
    }
}
