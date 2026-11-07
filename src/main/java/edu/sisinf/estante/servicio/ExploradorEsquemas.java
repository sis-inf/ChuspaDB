package edu.sisinf.chuspadb.servicio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import edu.sisinf.chuspadb.modelo.Esquema;
import edu.sisinf.chuspadb.modelo.Tabla;
import edu.sisinf.chuspadb.modelo.ColumnaInfo;
import edu.sisinf.chuspadb.core.ErrorQuery;

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

    /**
    * Obtiene las claves foráneas de una tabla.
    *
    * @return mapa con el nombre de la columna FK como clave y el nombre de la
    *         tabla referenciada como valor. Vacío si la tabla no tiene FK.
    */
   public Map<String, String> getClavesForaneas(Connection conexion, String esquema, String tabla) {
      Map<String, String> claves = new LinkedHashMap<>();

      try {
          DatabaseMetaData metaData = conexion.getMetaData();

          try (ResultSet rs = metaData.getImportedKeys(esquema, null, tabla)) {
              while (rs.next()) {
                  String columna = rs.getString("FKCOLUMN_NAME");
                  String tablaReferenciada = rs.getString("PKTABLE_NAME");

                  if (columna != null && tablaReferenciada != null) {
                      claves.put(columna, tablaReferenciada);
                 }
              }
          }
      } catch (SQLException e) {
          throw new ErrorQuery("No se pudieron obtener las claves foráneas de la tabla " + tabla, e);
      }

      return claves;
    }
}
