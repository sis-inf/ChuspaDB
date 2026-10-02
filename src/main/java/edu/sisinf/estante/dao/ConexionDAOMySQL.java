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
 * Implementación concreta de {@link IConexionDAO} para MySQL.
 */
public class ConexionDAOMySQL implements IConexionDAO {
    private static final Logger logger = LoggerFactory.getLogger(ConexionDAOMySQL.class);
    private static final String PUERTO_DEFAULT = "3306";
    private static final String PARAMS_BASE =
            "serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String PARAMS_SIN_SSL =
            "useSSL=false&" + PARAMS_BASE;
    private static final String PARAMS_CON_SSL =
            "useSSL=true&requireSSL=true&" + PARAMS_BASE;

    // Guarda la última conexión abierta para reutilizar sus datos reales en getTablas().
    private Conexion ultimaConexion;

    @Override
    public TipoMotor motor() {
        return TipoMotor.MYSQL;
    }

    /**
     * Construye la URL JDBC para MySQL.
     * Si usarSSL es true, incluye useSSL=true&requireSSL=true.
     *
     * @param conexion Objeto con los datos de conexión.
     * @return URL JDBC para MySQL.
     */
    @Override
    public String construirUrl(Conexion conexion) {
        String puerto = (conexion.getPuerto() != null)
                ? conexion.getPuerto().toString()
                : PUERTO_DEFAULT;

        String params = conexion.isUsarSSL() ? PARAMS_CON_SSL : PARAMS_SIN_SSL;

        return String.format("jdbc:mysql://%s:%s/%s?%s",
                conexion.getHost(),
                puerto,
                conexion.getBasedatos(),
                params);
    }

    @Override
    public Connection abrir(Conexion conexion) throws ErrorConexion, SQLException {
        if (conexion.getTipoMotor() != TipoMotor.MYSQL) {
            throw new ErrorConexion("El motor de la conexión no es MySQL.");
        }
        if (conexion.getHost() == null || conexion.getHost().isBlank()) {
            throw new ErrorConexion("El host no puede estar vacío.");
        }
        if (conexion.getBasedatos() == null || conexion.getBasedatos().isBlank()) {
            throw new ErrorConexion("El nombre de la base de datos no puede estar vacío.");
        }

        this.ultimaConexion = conexion;

        String url = construirUrl(conexion);
        return DriverManager.getConnection(url,
                conexion.getUsuario(),
                conexion.getPassword());
    }

    @Override
    public List<String> getTablas(String nombreBaseDatos) throws SQLException, ErrorConexion {
        if (nombreBaseDatos == null || !nombreBaseDatos.matches("[a-zA-Z0-9_]+")) {
            throw new ErrorConexion("Nombre de base de datos inválido: " + nombreBaseDatos);
        }
        if (ultimaConexion == null) {
            throw new ErrorConexion("No hay una conexión activa. Debe abrir una conexión antes de listar las tablas.");
        }

        List<String> tablas = new ArrayList<>();

        String puerto = (ultimaConexion.getPuerto() != null)
                ? ultimaConexion.getPuerto().toString()
                : PUERTO_DEFAULT;
        String params = ultimaConexion.isUsarSSL() ? PARAMS_CON_SSL : PARAMS_SIN_SSL;

        String url = String.format("jdbc:mysql://%s:%s/%s?%s",
                ultimaConexion.getHost(),
                puerto,
                nombreBaseDatos,
                params);

        try (Connection conn = DriverManager.getConnection(url, ultimaConexion.getUsuario(), ultimaConexion.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES IN " + nombreBaseDatos)) {

            while (rs.next()) {
                tablas.add(rs.getString(1));
            }
        }

        return tablas;
    }

    @Override
    public boolean probar(Conexion conexion) {
        try (Connection conn = abrir(conexion)) {
            return conn.isValid(3);
        } catch (Exception e) {
            logger.error("Error al probar la conexión con MySQL para host: {}", conexion.getHost(), e);
            return false;
        }
    }
}
