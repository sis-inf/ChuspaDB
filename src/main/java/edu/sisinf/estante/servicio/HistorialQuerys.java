package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.modelo.EntradaHistorial;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio que mantiene un registro de las últimas 50 queries ejecutadas.
 *
 * Usa política FIFO: cuando se supera el límite, la entrada más antigua
 * es descartada automáticamente.
 */
public class HistorialQuerys {

    private static final Logger LOGGER = Logger.getLogger(HistorialQuerys.class.getName());
    private static final int LIMITE = 50;

    private final Deque<EntradaHistorial> historial = new ArrayDeque<>();

    /**
     * Agrega una nueva entrada al historial marcada como exitosa.
     *
     * @param query      sentencia SQL ejecutada
     * @param baseDatos  nombre de la base de datos o conexión activa
     * @param duracionMs tiempo de ejecución en milisegundos
     */
    public void agregar(String query, String baseDatos, long duracionMs) {
        agregar(query, baseDatos, duracionMs, true);
    }

    /**
     * Agrega una nueva entrada al historial con indicador de éxito.
     *
     * @param query      sentencia SQL ejecutada
     * @param baseDatos  nombre de la base de datos o conexión activa
     * @param duracionMs tiempo de ejecución en milisegundos
     * @param exitosa    indica si la query se ejecutó sin errores
     */
    public void agregar(String query, String baseDatos, long duracionMs, boolean exitosa) {
        if (query == null || query.isBlank()) {
            LOGGER.log(Level.WARNING, "Intento de registrar una query nula o vacía.");
            throw new IllegalArgumentException("La sentencia SQL (query) no puede ser nula ni estar vacía.");
        }
        if (baseDatos == null || baseDatos.isBlank()) {
            LOGGER.log(Level.WARNING, "Intento de registrar una query sin nombre de base de datos válido.");
            throw new IllegalArgumentException("El nombre de la base de datos no puede ser nulo ni estar vacío.");
        }
        if (duracionMs < 0) {
            LOGGER.log(Level.WARNING, "Duración negativa recibida al registrar query: {0} ms", duracionMs);
            throw new IllegalArgumentException("La duración en milisegundos no puede ser negativa.");
        }

        if (historial.size() >= LIMITE) {
            EntradaHistorial descartada = historial.pollFirst();
            LOGGER.log(Level.FINE, "Límite de historial alcanzado ({0}). Entrada más antigua descartada: {1}",
                    new Object[]{LIMITE, descartada});
        }

        EntradaHistorial nuevaEntrada = new EntradaHistorial(
                System.currentTimeMillis(),
                query.trim(),
                baseDatos.trim(),
                duracionMs,
                exitosa
        );
        historial.addLast(nuevaEntrada);
        LOGGER.log(Level.FINE, "Query registrada en historial para base de datos ''{0}'' (duración: {1} ms, exitosa: {2})",
                new Object[]{baseDatos.trim(), duracionMs, exitosa});
    }

    /**
     * Retorna todas las entradas del historial en orden cronológico.
     *
     * @return lista de entradas del historial
     */
    public List<EntradaHistorial> obtenerTodos() {
        LOGGER.log(Level.FINE, "Consultando todas las entradas del historial (total: {0})", historial.size());
        return new ArrayList<>(historial);
    }

    /**
     * Retorna la última entrada del historial.
     *
     * @return última entrada, o null si el historial está vacío
     */
    public EntradaHistorial obtenerUltimo() {
        LOGGER.log(Level.FINE, "Consultando la última entrada del historial.");
        return historial.peekLast();
    }

    /**
     * Vacía el historial completamente.
     */
    public void limpiar() {
        int totalPrevio = historial.size();
        historial.clear();
        LOGGER.log(Level.FINE, "Historial de queries limpiado ({0} entradas eliminadas).", totalPrevio);
    }

    /**
     * Retorna solo las entradas que coincidan con la base de datos indicada,
     * ignorando mayúsculas y minúsculas.
     *
     * @param baseDatos nombre de la base de datos a filtrar
     * @return lista filtrada de entradas del historial
     */
    public List<EntradaHistorial> obtenerPorBaseDatos(String baseDatos) {
        if (baseDatos == null || baseDatos.isBlank()) {
            LOGGER.log(Level.WARNING, "Se solicitó filtrar historial con un nombre de base de datos nulo o vacío.");
            throw new IllegalArgumentException("El nombre de la base de datos a filtrar no puede ser nulo ni estar vacío.");
        }

        String baseLimpia = baseDatos.trim();
        List<EntradaHistorial> filtradas = new ArrayList<>();
        for (EntradaHistorial entrada : historial) {
            if (entrada != null && baseLimpia.equalsIgnoreCase(entrada.baseDatos())) {
                filtradas.add(entrada);
            }
        }
        LOGGER.log(Level.FINE, "Filtrado por base de datos ''{0}'' completado: {1} coincidencias.",
                new Object[]{baseLimpia, filtradas.size()});
        return filtradas;
    }

    /**
     * Retorna solo las entradas cuyo campo exitosa sea true.
     *
     * @return lista filtrada de entradas exitosas
     */
    public List<EntradaHistorial> obtenerSoloExitosas() {
        List<EntradaHistorial> filtradas = new ArrayList<>();
        for (EntradaHistorial entrada : historial) {
            if (entrada != null && entrada.exitosa()) {
                filtradas.add(entrada);
            }
        }
        LOGGER.log(Level.FINE, "Consulta de queries exitosas completada: {0} coincidencias.", filtradas.size());
        return filtradas;
    }
}
