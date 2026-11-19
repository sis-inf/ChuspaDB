package edu.sisinf.chuspadb.dao;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Define las operaciones necesarias para abrir y validar
 * conexiones JDBC para distintos motores de base de datos.
 *
 * <p>Las implementaciones deben garantizar el cierre estricto de recursos JDBC
 * ({@link Connection}, {@code Statement}, {@code ResultSet}) mediante bloques
 * {@code try-with-resources} cuando corresponda, y propagar {@link ErrorConexion}
 * con mensajes descriptivos ante fallos de conexión o consulta.</p>
 */
public interface IConexionDAO {

    /**
     * Indica el motor que esta implementación maneja.
     *
     * @return motor soportado por la implementación
     */
    TipoMotor motor();

    /**
     * Construye la URL JDBC a partir de los datos de la conexión.
     *
     * @param conexion datos de conexión
     * @return URL JDBC construida
     * @throws IllegalArgumentException si los datos de conexión son nulos o inválidos
     */
    String construirUrl(Conexion conexion);

    /**
     * Abre una conexión JDBC.
     *
     * <p>Quien invoca este método es responsable de cerrar la {@link Connection}
     * devuelta utilizando {@code try-with-resources}.</p>
     *
     * @param conexion datos de conexión
     * @return conexión JDBC abierta
     * @throws ErrorConexion si los parámetros de conexión son inválidos o falla la autenticación/red
     * @throws SQLException si la conexión falla
     */
    Connection abrir(Conexion conexion) throws ErrorConexion, SQLException;

    /**
     * Lista las tablas de una base de datos garantizando el cierre de 
     * conexiones, sentencias y conjuntos de resultados mediante {@code try-with-resources}.
     *
     * @param nombreBaseDatos nombre de la base de datos
     * @return lista de nombres de tablas
     * @throws ErrorConexion si no existe conexión activa, el nombre es inválido o falla la consulta
     * @throws SQLException si ocurre un error consultando las tablas
     */
    List<String> getTablas(String nombreBaseDatos) throws ErrorConexion, SQLException;

    /**
     * Verifica si una conexión es válida intentando abrirla y cerrarla.
     * mediante {@code try-with-resources}.
     *
     * @param conexion datos de conexión
     * @return {@code true} si la conexión funciona, {@code false} en caso contrario
     */
    boolean probar(Conexion conexion);
}
