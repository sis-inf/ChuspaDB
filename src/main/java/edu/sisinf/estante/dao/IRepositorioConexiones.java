package edu.sisinf.chuspadb.dao;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.modelo.Conexion;
import java.util.List;
import java.util.Optional;

public interface IRepositorioConexiones {

    /**
     * Devuelve todas las conexiones guardadas.
     *
     * @return lista de conexiones guardadas; si no existen conexiones, devuelve una lista vacía.
     */
    List<Conexion> listar();

    /**
     * Busca una conexión por su identificador.
     *
     * @param id identificador de la conexión a buscar.
     * @return Optional con la conexión si existe; Optional vacío si no existe.
     */
    Optional<Conexion> buscarPorId(String id);

    /**
     * Persiste una conexión nueva.
     * Si la conexión recibida no tiene id, la implementación debe asignarle uno antes de guardarla.
     *
     * @param conexion conexión a guardar.
     * @return conexión persistida con id asignado.
     * @throws ErrorPersistencia si ocurre un error durante la persistencia.
     */
    Conexion guardar(Conexion conexion) throws ErrorPersistencia;

    /**
     * Actualiza una conexión existente identificándola por su id.
     *
     * @param conexion conexión a actualizar.
     * @throws ErrorPersistencia si no existe una conexión con el id indicado o si ocurre un error de persistencia.
     */
    void actualizar(Conexion conexion) throws ErrorPersistencia;

    /**
     * Elimina la conexión con el id indicado.
     *
     * @param id identificador de la conexión a eliminar.
     * @throws ErrorPersistencia si no existe una conexión con el id indicado o si ocurre un error de persistencia.
     */
    void eliminar(String id) throws ErrorPersistencia;
}
