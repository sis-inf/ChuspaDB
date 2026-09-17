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
 * Implementación concreta de {@link IConexionDAO} para PostgreSQL.
 * PostgreSQL requiere host, puerto, usuario y password para conectarse.
 * La clase es stateless: no almacena conexiones internamente
 * y no implementa pooling de conexiones.
 */
public class ConexionDAOPostgreSQL implements IConexionDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(ConexionDAOPostgreSQL.class);

    private static final String PUERTO_DEFAULT = "5432";

    @Override
    public TipoMotor motor() {
        return TipoMotor.POSTGRESQL;
    }

    @Override
    public String construirUrl(Conexion conexion) {

        String puerto = (conexion.getPuerto() != null)
                ? conexion.getPuerto().toString()
                : PUERTO_DEFAULT;

        return String.format("jdbc:postgresql://%s:%s/%s",
                conexion.getHost(),
                puerto,
                conexion.getBasedatos());
    }

    @Override
    public Connection abrir(Conexion conexion)
            throws ErrorConexion, SQLException {

        if (conexion.getTipoMotor() != TipoMotor.POSTGRESQL) {
            throw new ErrorConexion(
                    "El motor de la conexión no es PostgreSQL.");
        }

        if (conexion.getHost() == null
                || conexion.getHost().isBlank()) {
            throw new ErrorConexion(
                    "El host no puede estar vacío.");
        }

        if (conexion.getBasedatos() == null
                || conexion.getBasedatos().isBlank()) {
            throw new ErrorConexion(
                    "El nombre de la base de datos no puede estar vacío.");
        }

        // validación agregada 
        if (conexion.getUsuario() == null
                || conexion.getUsuario().isBlank()) {
            throw new ErrorConexion(
                    "El usuario de PostgreSQL no puede estar vacío.");
        }

        if (conexion.getPassword() == null
                || conexion.getPassword().isBlank()) {
            throw new ErrorConexion(
                    "La contraseña de PostgreSQL no puede estar vacía.");
        }

        String url = construirUrl(conexion);

        return DriverManager.getConnection(
                url,
                conexion.getUsuario(),
                conexion.getPassword());
    }

    @Override
    public List<String> getTablas(String nombreBaseDatos)
            throws SQLException {

        List<String> tablas = new ArrayList<>();

        String url = String.format(
                "jdbc:postgresql://localhost:%s/%s",
                PUERTO_DEFAULT,
                nombreBaseDatos);

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT tablename FROM pg_tables WHERE schemaname='public'")) {

            while (rs.next()) {
                tablas.add(rs.getString("tablename"));
            }
        }

        return tablas;
    }

    @Override
    public boolean probar(Conexion conexion) {

        try (Connection conn = abrir(conexion)) {

            return conn.isValid(3);

        } catch (Exception e) {

            logger.error(
                    "Error al probar la conexión con PostgreSQL para host: {}",
                    conexion.getHost(),
                    e);

            return false;
        }
    }
}
