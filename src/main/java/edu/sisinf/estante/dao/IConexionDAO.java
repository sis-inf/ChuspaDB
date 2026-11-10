package edu.sisinf.chuspadb.dao;

import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Define las operaciones necesarias para abrir y validar
 * conexiones JDBC para distintos motores de base de datos.
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
     */
    String construirUrl(Conexion conexion);

    /**
     * Abre una conexión JDBC.
     *
     * @param conexion datos de conexión
     * @return conexión JDBC abierta
     * @throws SQLException si la conexión falla
     */
    Connection abrir(Conexion conexion) throws SQLException;

    /**
     * Lista las tablas de una base de datos.
     *
     * @param nombreBaseDatos nombre de la base de datos
     * @return lista de nombres de tablas
     * @throws SQLException si ocurre un error consultando las tablas
     */
    List<String> getTablas(String nombreBaseDatos) throws SQLException;

    /**
     * Verifica si una conexión es válida intentando abrirla y cerrarla.
     *
     * @param conexion datos de conexión
     * @return true si la conexión funciona, false en caso contrario
     */
    boolean probar(Conexion conexion);
}
