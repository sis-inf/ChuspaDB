package edu.sisinf.chuspadb.modelo;

/**
 * Registro de una query ejecutada en la sesión.
 *
 * @param timestamp   momento en que se ejecutó la query (Unix ms)
 * @param query       sentencia SQL ejecutada
 * @param baseDatos   nombre de la base de datos o conexión activa
 * @param duracionMs  tiempo de ejecución en milisegundos
 * @param exitosa     indica si la query se ejecutó sin errores
 */
public record EntradaHistorial(
        long timestamp,
        String query,
        String baseDatos,
        long duracionMs,
        boolean exitosa
) {}
