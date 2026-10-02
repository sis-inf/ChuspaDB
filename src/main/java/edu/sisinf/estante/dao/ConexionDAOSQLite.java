package edu.sisinf.estante.dao;

import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;
import edu.sisinf.estante.core.ErrorConexion;

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

    @Override
    public TipoMotor motor() {
        return TipoMotor.SQLITE;
    }

    @Override
    public String construirUrl(Conexion conexion) {
        return "jdbc:sqlite:" + conexion.getBasedatos();
    }

    @Override
    public Connection abrir(Conexion conexion) throws ErrorConexion, SQLException {

        if (conexion.getTipoMotor() != TipoMotor.SQLITE) {
            throw new ErrorConexion("El motor de la conexión no es SQLite.");
        }

        if (conexion.getBasedatos() == null || conexion.getBasedatos().isBlank()) {
            throw new ErrorConexion("La ruta de la base de datos no puede estar vacía.");
        }

        this.ultimaConexion = conexion;

        String url = construirUrl(conexion);
        return DriverManager.getConnection(url);
    }

    @Override
    public List<String> getTablas(String nombreBaseDatos) throws SQLException, ErrorConexion {
        if (ultimaConexion == null) {
            throw new ErrorConexion("No hay una conexión activa. Debe abrir una conexión antes de listar las tablas.");
        }

        List<String> tablas = new ArrayList<>();

        String url = "jdbc:sqlite:" + nombreBaseDatos;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table'")) {

            while (rs.next()) {
                tablas.add(rs.getString("name"));
            }
        }

        return tablas;
    }

    @Override
    public boolean probar(Conexion conexion) {
        try (Connection conn = abrir(conexion)) {
            return conn.isValid(3);
        } catch (Exception e) {
            logger.error("Error al probar la conexión con SQLite en: {}", conexion.getBasedatos(), e);
            return false;
        }
    }
}
