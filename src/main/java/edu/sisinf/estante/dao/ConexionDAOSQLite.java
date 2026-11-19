package edu.sisinf.chuspadb.dao;

import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;
import edu.sisinf.chuspadb.core.ErrorConexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación concreta de {@link IConexionDAO} para SQLite.
 * SQLite es una base de datos embebida que se accede vía un archivo
 * {@code .db} en disco. No requiere host, puerto, usuario ni password.
 * La clase guarda internamente la última conexión abierta para mantener
 * el mismo patrón que las demás implementaciones de {@link IConexionDAO}.
 */
public class ConexionDAOSQLite implements IConexionDAO {
    private static final Logger logger = LoggerFactory.getLogger(ConexionDAOSQLite.class);

    private Conexion ultimaConexion;

    /**
     * Devuelve el motor de base de datos que maneja esta implementación.
     *
     * @return {@link TipoMotor#SQLITE}
     */
    @Override
    public TipoMotor motor() {
        return TipoMotor.SQLITE;
    }

    /**
     * Construye la URL JDBC para SQLite con el formato
     * {@code jdbc:sqlite:<ruta>}, donde {@code <ruta>} es el valor
     * del campo {@code basedatos} del objeto {@link Conexion}.
     *
     * @param conexion Objeto con los datos de conexión.
     * @return URL JDBC para SQLite.
     */
    @Override
    public String construirUrl(Conexion conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("El objeto de conexión no puede ser nulo.");
        }
        return "jdbc:sqlite:" + conexion.getBasedatos();
    }

    /**
     * Abre una conexión a la base de datos SQLite.
     *
     * @param conexion Objeto con los datos de conexión.
     * @return Conexión JDBC activa.
     * @throws ErrorConexion si el motor no es SQLite o la ruta está vacía.
     * @throws SQLException  si el driver falla al conectar.
     */
    @Override
    public Connection abrir(Conexion conexion) throws ErrorConexion, SQLException {
        if (conexion == null) {
            throw new ErrorConexion("Los datos de conexión a SQLite no pueden ser nulos.");
        }

        if (conexion.getTipoMotor() != TipoMotor.SQLITE) {
            throw new ErrorConexion("El motor de la conexión no es SQLite.");
        }

        if (conexion.getBasedatos() == null || conexion.getBasedatos().isBlank()) {
            throw new ErrorConexion("La ruta de la base de datos no puede estar vacía.");
        }

        String url = construirUrl(conexion);

        try {
            Connection conn = DriverManager.getConnection(url);
            this.ultimaConexion = conexion;
            return conn;
        } catch (SQLException e) {
            logger.error("Error al abrir conexión SQLite en ruta '{}'", conexion.getBasedatos(), e);
            throw new ErrorConexion("No se pudo establecer la conexión con SQLite en '"
                    + conexion.getBasedatos() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getTablas(String nombreBaseDatos) throws SQLException, ErrorConexion {
        if (ultimaConexion == null) {
            throw new ErrorConexion("No hay una conexión activa. Debe abrir una conexión antes de listar las tablas.");
        }

        List<String> tablas = new ArrayList<>();

        String url = construirUrl(ultimaConexion);

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table'")) {

            while (rs.next()) {
                tablas.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Error SQL al consultar las tablas de la base de datos SQLite '{}'",
                    ultimaConexion.getBasedatos(), e);
            throw new ErrorConexion("Error al obtener las tablas de SQLite en '"
                    + ultimaConexion.getBasedatos() + "': " + e.getMessage(), e);
        }

        return tablas;
    }

    /**
     * Prueba si la conexión a la base de datos SQLite es válida.
     *
     * @param conexion Objeto con los datos de conexión.
     * @return {@code true} si la conexión es válida, {@code false} en caso contrario.
     */
    @Override
    public boolean probar(Conexion conexion) {
        if (conexion == null) {
            logger.warn("Se intentó probar una conexión SQLite nula.");
            return false;
        }
        try (Connection conn = abrir(conexion)) {
            return conn != null && conn.isValid(3);
        } catch (Exception e) {
            logger.error("Error al probar la conexión con SQLite en: {}", conexion.getBasedatos(), e);
            return false;
        }
    }
}
