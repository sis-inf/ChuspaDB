package edu.sisinf.estante.servicio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import edu.sisinf.estante.modelo.Esquema;
import edu.sisinf.estante.modelo.Tabla;
import edu.sisinf.estante.modelo.ColumnaInfo;
import edu.sisinf.estante.core.ErrorQuery;

/**
 * Servicio stateless para explorar la estructura de la base de datos.
 */
public class ExploradorEsquemas {

    // Prefijo utilizado por el driver JDBC de SQLite para la deteccion de la URL de conexion.
    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    /**
     * Lista los esquemas (catálogos) visibles en la conexión.
     */
    public List<Esquema> listarEsquemas(Connection conexion) {
        List<Esquema> esquemas = new ArrayList<>();
        try {
            DatabaseMetaData metaData = conexion.getMetaData();
            String url = metaData.getURL();

            // Criterio: Detección de SQLite
            if (url != null && url.startsWith(SQLITE_URL_PREFIX)) {
                esquemas.add(new Esquema("main",new ArrayList<>()));
            } else {
                // Criterio: Otros motores (MySQL) usando TABLE_CAT
                try (ResultSet rs = metaData.getCatalogs()) {
                    while (rs.next()) {
                        String nombreEsquema = rs.getString("TABLE_CAT");
                        if (nombreEsquema != null) {
                            esquemas.add(new Esquema(nombreEsquema,new ArrayList<>()));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new ErrorQuery("No se pudieron listar los esquemas", e);
        }
        return esquemas;
    }

    /**
     * Lista las tablas dentro del esquema indicado.
     */
    public List<Tabla> listarTablas(Connection conexion, String esquema) {
        List<Tabla> tablas = new ArrayList<>();
        try {
            DatabaseMetaData metaData = conexion.getMetaData();
            // Criterio: Usar getTables con tipo "TABLE"
            try (ResultSet rs = metaData.getTables(esquema, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String nombreTabla = rs.getString("TABLE_NAME");
                    tablas.add(new Tabla(nombreTabla, esquema ,new ArrayList<>()));
                }
            }
        } catch (SQLException e) {
            throw new ErrorQuery("No se pudieron listar las tablas del esquema " + esquema, e);
        }
        return tablas;
    }

    /**
     * Obtiene las columnas de una tabla específica, incluyendo si es Primary Key.
     */
    public List<ColumnaInfo> getColumnas(Connection conexion, String esquema, String tabla) {
        List<ColumnaInfo> columnas = new ArrayList<>();

        try {
            DatabaseMetaData metaData = conexion.getMetaData();
            
            // 1. Obtener Primary Keys para comparar
            List<String> pks = new ArrayList<>();
            try (ResultSet rsPk = metaData.getPrimaryKeys(esquema, null, tabla)) {
                while (rsPk.next()) {
                    pks.add(rsPk.getString("COLUMN_NAME"));
                }
            }

            // 2. Obtener metadatos y combinar con PKs
            try (ResultSet rs = metaData.getColumns(esquema, null, tabla, "%")) {
                while (rs.next()) {
                    String nombre = rs.getString("COLUMN_NAME");
                    String tipoSQL = rs.getString("TYPE_NAME");
                    boolean nullable = "YES".equals(rs.getString("IS_NULLABLE"));
                    
                    Integer tamano = rs.getInt("COLUMN_SIZE");
                    if (rs.wasNull()) {
                        tamano = null;
                    }

                    String valorDefault = rs.getString("COLUMN_DEF");
                    boolean esPrimaryKey = pks.contains(nombre);

                    columnas.add(new ColumnaInfo(nombre, tipoSQL, nullable, tamano, valorDefault, esPrimaryKey));
                }
            }
        } catch (SQLException e) {
            throw new ErrorQuery("No se pudieron obtener las columnas de la tabla " + tabla, e);
        }

        return columnas;
    }
}
