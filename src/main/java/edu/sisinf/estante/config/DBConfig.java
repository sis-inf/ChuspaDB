package edu.sisinf.chuspadb.config;

/**
 * Configuración de conexión a base de datos.
 * Contiene los datos necesarios para establecer una conexión JDBC.
 */
public class DBConfig {

    private String url;
    private String usuario;
    private String password;
    private String driver;

    /**
     * Constructor con todos los campos.
     *
     * @param url      URL JDBC de conexión
     * @param usuario  Usuario de la base de datos
     * @param password Contraseña del usuario
     * @param driver   Clase del driver JDBC
     */
    public DBConfig(String url, String usuario, String password, String driver) {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
        this.driver = driver;
    }

    /** Constructor vacío para uso con setters. */
    public DBConfig() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
