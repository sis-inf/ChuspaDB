package edu.sisinf.estante.servicio;

import edu.sisinf.estante.modelo.EntradaHistorial;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Servicio que mantiene un registro de las últimas 50 queries ejecutadas.
 *
 * Usa política FIFO: cuando se supera el límite, la entrada más antigua
 * es descartada automáticamente.
 */
public class HistorialQuerys {

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
        if (historial.size() >= LIMITE) {
            historial.pollFirst();
        }
        historial.addLast(new EntradaHistorial(
                System.currentTimeMillis(),
                query,
                baseDatos,
                duracionMs,
                exitosa
        ));
    }

    /**
     * Retorna todas las entradas del historial en orden cronológico.
     *
     * @return lista de entradas del historial
     */
    public List<EntradaHistorial> obtenerTodos() {
        return new ArrayList<>(historial);
    }

    /**
     * Retorna la última entrada del historial.
     *
     * @return última entrada, o null si el historial está vacío
     */
    public EntradaHistorial obtenerUltimo() {
        return historial.peekLast();
    }

    /**
     * Vacía el historial completamente.
     */
    public void limpiar() {
        historial.clear();
    }
    
    /**
     * Retorna solo las entradas que coincidan con la base de datos indicada,
     * ignorando mayúsculas y minúsculas.
     *
     * @param baseDatos nombre de la base de datos a filtrar
     * @return lista filtrada de entradas del historial
     */
    public List<EntradaHistorial> obtenerPorBaseDatos(String baseDatos) {
        List<EntradaHistorial> filtradas = new ArrayList<>();
        if (baseDatos != null) {
            for (EntradaHistorial entrada : historial) {
                if (baseDatos.equalsIgnoreCase(entrada.getBaseDatos())) {
                    filtradas.add(entrada);
                }
            }
        }
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
            if (entrada.isExitosa()) {
                filtradas.add(entrada);
            }
        }
        return filtradas;
    }
}
