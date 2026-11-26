package edu.sisinf.chuspadb.dao;

import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;
import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.core.ErrorQuery;

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
 * La clase guarda internamente la última conexión abierta para poder
 * reutilizar sus datos reales (host, puerto, usuario, password) en getTablas().
 * No implementa pooling de conexiones.
 */
public class ConexionDAOPostgreSQL implements IConexionDAO {
    private static final Logger logger = LoggerFactory.getLogger(ConexionDAOPostgreSQL.class);
    private static final String PUERTO_DEFAULT = "5432";

    private Conexion ultimaConexion;

    /**
     * Devuelve el motor de base de datos que maneja esta implementación.
     *
     * @return {@link TipoMotor#POSTGRESQL}
     */
    @Override
    public TipoMotor motor() {
        return TipoMotor.POSTGRESQL;
    }

    /**
     * Construye la URL JDBC para PostgreSQL con el formato
     * {@code jdbc:postgresql://<host>:<puerto>/<basedatos>}.
     * Si el puerto es {@code null}, se usa {@code 5432} por defecto.
     *
     * @param conexion Objeto con los datos de conexión.
     * @return URL JDBC para PostgreSQL.
     */
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

    /**
     * Abre una conexión a la base de datos PostgreSQL.
     *
     * @param conexion Objeto con los datos de conexión.
     * @return Conexión JDBC activa.
     * @throws ErrorConexion si el motor no es PostgreSQL o faltan campos obligatorios.
     * @throws SQLException  si el driver falla al conectar.
     */
    @Override
    public Connection abrir(Conexion conexion) throws ErrorConexion, SQLException {
        if (conexion == null) {
            throw new ErrorConexion("Los datos de conexión no pueden ser nulos.");
        }
        if (conexion.getTipoMotor() != TipoMotor.POSTGRESQL) {
            throw new ErrorConexion("El motor de la conexión no es PostgreSQL.");
        }
        if (conexion.getHost() == null || conexion.getHost().isBlank()) {
            throw new ErrorConexion("El host no puede estar vacío.");
        }
        if (conexion.getBasedatos() == null || conexion.getBasedatos().isBlank()) {
            throw new ErrorConexion("El nombre de la base de datos no puede estar vacío.");
        }
        if (conexion.getUsuario() == null || conexion.getUsuario().isBlank()) {
            throw new ErrorConexion("El usuario de PostgreSQL no puede estar vacío.");
        }
        if (conexion.getPassword() == null || conexion.getPassword().isBlank()) {
            throw new ErrorConexion("La contraseña de PostgreSQL no puede estar vacía.");
        }

        String url = construirUrl(conexion);
        
        try {
            Connection conn = DriverManager.getConnection(
                    url,
                    conexion.getUsuario(),
                    conexion.getPassword()
            );
            this.ultimaConexion = conexion;
            return conn;
        } catch (SQLException e) {
            logger.error(
                    "Error al abrir conexión PostgreSQL. Host: {}, SQLState: {}",
                    conexion.getHost(),e.getSQLState(),e
            );
            throw new ErrorConexion("No se pudo establecer la conexión con PostgreSQL en el host: "
                            + conexion.getHost(),e);
        }
    }

    @Override
    public List<String> getTablas(String nombreBaseDatos) throws SQLException, ErrorConexion {
        if (ultimaConexion == null) {
            throw new ErrorConexion("No hay una conexión activa. Debe abrir una conexión antes de listar las tablas.");
        }
        if (nombreBaseDatos == null || nombreBaseDatos.isBlank()) {
            throw new ErrorConexion("El nombre de la base de datos no puede estar vacío.");
        }

        List<String> tablas = new ArrayList<>();

        String puerto = (ultimaConexion.getPuerto() != null)
                ? ultimaConexion.getPuerto().toString()
                : PUERTO_DEFAULT;

        String url = String.format("jdbc:postgresql://%s:%s/%s",
                ultimaConexion.getHost(),
                puerto,
                nombreBaseDatos);
        Connection conexion;

        try {
            conexion = DriverManager.getConnection(
                    url,
                    ultimaConexion.getUsuario(),
                    ultimaConexion.getPassword()
            );

        } catch (SQLException e) {
            logger.error(
                    "Error de conexión al obtener las tablas de PostgreSQL. Base de datos: {}, SQLState: {}",
                    nombreBaseDatos,e.getSQLState(),e
            );

            throw new ErrorConexion(
                    "No se pudo conectar a PostgreSQL para obtener las tablas de la base de datos: "
                            + nombreBaseDatos,e);
        }
        try (Connection conn = conexion;
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT tablename FROM pg_tables WHERE schemaname='public'")) {

            while (rs.next()) {
                tablas.add(rs.getString("tablename"));
            }

        } catch (SQLException e) {
            logger.error(
                    "Error al consultar las tablas de PostgreSQL. Base de datos: {}, SQLState: {}",
                    nombreBaseDatos,
                    e.getSQLState(),
                    e
            );

            throw new ErrorQuery(
                    "No se pudieron consultar las tablas de la base de datos: "
                            + nombreBaseDatos,
                    e
            );
        }
        return tablas;
    }

    /**
     * Prueba si la conexión a la base de datos PostgreSQL es válida.
     *
     * @param conexion Objeto con los datos de conexión.
     * @return {@code true} si la conexión es válida, {@code false} en caso contrario.
     */
    @Override
    public boolean probar(Conexion conexion) {
        String host = (conexion != null)
                ? conexion.getHost()
                : "desconocido";
        try (Connection conn = abrir(conexion)) {
            return conn.isValid(3);
       } catch (Exception e) {
         logger.error("Error de conexión al probar PostgreSQL. Host: {}. Motivo: {}",
                    host,e.getMessage(),e);
         return false;
      }
    }
}
