package edu.sisinf.chuspadb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración de conexión a base de datos.
 * Contiene los datos necesarios para establecer una conexión JDBC
 * y valida la consistencia de sus parámetros.
 */
public class DBConfig {

    private static final Logger logger = LoggerFactory.getLogger(DBConfig.class);

    private String url;
    private String usuario;
    private String password;
    private String driver;

    /**
     * Constructor con todos los campos.
     *
     * @param url      URL JDBC de conexión (obligatoria, no nula ni vacía)
     * @param usuario  Usuario de la base de datos (no nulo; puede ser vacío en motores embebidos)
     * @param password Contraseña del usuario (si es {@code null}, se normaliza a cadena vacía)
     * @param driver   Clase del driver JDBC (obligatoria, no nula ni vacía)
     * @throws IllegalArgumentException si {@code url}, {@code usuario} o {@code driver} son inválidos
     */
    public DBConfig(String url, String usuario, String password, String driver) {
        setUrl(url);
        setUsuario(usuario);
        setPassword(password);
        setDriver(driver);
        logger.debug("Instancia de DBConfig creada correctamente para URL '{}' con driver '{}'.",
                this.url, this.driver);
    }

    /** Constructor vacío para uso con setters. */
    public DBConfig() {
        logger.debug("Instancia vacía de DBConfig inicializada.");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url == null || url.isBlank()) {
            logger.warn("Intento de asignar una URL JDBC nula o vacía en DBConfig.");
            throw new IllegalArgumentException("La URL JDBC de conexión no puede ser nula ni estar vacía.");
        }
        String urlLimpia = url.trim();
        if (!urlLimpia.startsWith("jdbc:")) {
            logger.warn("Intento de asignar una URL JDBC con formato inválido: '{}'.", urlLimpia);
            throw new IllegalArgumentException("La URL debe comenzar con el prefijo 'jdbc:'.");
        }
        this.url = urlLimpia;
        logger.debug("URL JDBC actualizada en DBConfig: '{}'.", this.url);
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        if (usuario == null) {
            logger.warn("Intento de asignar un usuario nulo en DBConfig.");
            throw new IllegalArgumentException("El usuario de la base de datos no puede ser nulo.");
        }
        this.usuario = usuario.trim();
        logger.debug("Usuario actualizado en DBConfig.");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null) {
            logger.debug("Contraseña nula recibida en DBConfig; normalizando a cadena vacía.");
            this.password = "";
            return;
        }
        this.password = password;
        logger.debug("Contraseña actualizada en DBConfig.");
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        if (driver == null || driver.isBlank()) {
            logger.warn("Intento de asignar un driver JDBC nulo o vacío en DBConfig.");
            throw new IllegalArgumentException("El nombre de la clase del driver JDBC no puede ser nulo ni estar vacío.");
        }
        this.driver = driver.trim();
        logger.debug("Driver JDBC actualizado en DBConfig: '{}'.", this.driver);
    }

    /**
     * Verifica que la configuración contenga los datos mínimos requeridos para conectar.
     *
     * @throws IllegalStateException si la URL o el driver no han sido configurados.
     */
    public void validar() {
        if (url == null || url.isBlank() || driver == null || driver.isBlank()) {
            logger.error("Estado inconsistente en DBConfig: faltan la URL JDBC o el driver.");
            throw new IllegalStateException("La configuración de base de datos es incompleta: la URL y el driver son obligatorios.");
        }
        if (usuario == null) {
            logger.error("Estado inconsistente en DBConfig: el usuario no ha sido configurado (es nulo).");
            throw new IllegalStateException("La configuración de base de datos es incompleta: el usuario no puede ser nulo.");
        }
        if (password == null) {
            this.password = "";
        }
        logger.debug("Configuración DBConfig validada correctamente.");
    }
}
