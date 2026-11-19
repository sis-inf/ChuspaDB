package edu.sisinf.chuspadb.core;

import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase utilitaria para medir el tiempo de ejecución de queries.
 * Utiliza System.nanoTime() para mayor precisión.
 */
public class QueryTimer {

    private static final Logger LOGGER = Logger.getLogger(QueryTimer.class.getName());
    private static final String NOMBRE_POR_DEFECTO = "query";

    private final String nombre;
    private Long tiempoInicio;
    private Long tiempoFin;

    public QueryTimer() {
        this(NOMBRE_POR_DEFECTO);
    }

    /**
     * @param nombre identificador usado en las trazas de depuración
     * @throws NullPointerException     si nombre es nulo
     * @throws IllegalArgumentException si nombre está vacío
     */
    public QueryTimer(String nombre) {
        Objects.requireNonNull(nombre, "El nombre del timer no puede ser nulo.");
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del timer no puede estar vacío.");
        }
        this.nombre = nombre;
    }

    /**
     * Registra el tiempo de inicio de la medición.
     *
     * @throws IllegalStateException si hay una medición en curso sin detener
     */
    public void iniciar() {
        if (tiempoInicio != null && tiempoFin == null) {
            throw new IllegalStateException(
                "No se puede iniciar: la medición '" + nombre + "' ya está en curso.");
        }
        tiempoInicio = System.nanoTime();
        tiempoFin = null;
        LOGGER.log(Level.FINE, "[{0}] medición iniciada", nombre);
    }

    /**
     * Registra el tiempo de fin de la medición.
     *
     * @throws IllegalStateException si no se llamó a iniciar() o ya se detuvo
     */
    public void detener() {
        if (tiempoInicio == null) {
            throw new IllegalStateException(
                "No se puede detener el tiempo: debe llamar a iniciar() antes de detener().");
        }
        if (tiempoFin != null) {
            throw new IllegalStateException(
                "No se puede detener: la medición '" + nombre + "' ya fue detenida.");
        }
        tiempoFin = System.nanoTime();
        LOGGER.log(Level.FINE, "[{0}] medición detenida: {1} ms",
                new Object[] { nombre, calcularMs() });
    }

    /**
     * Devuelve la diferencia entre inicio y fin en milisegundos.
     *
     * @return tiempo transcurrido en milisegundos
     * @throws IllegalStateException si no se llamó a iniciar() y detener()
     */
    public double getTiempoMs() {
        if (tiempoInicio == null) {
            throw new IllegalStateException(
                "No se puede obtener el tiempo: debe llamar a iniciar() antes de obtener el tiempo transcurrido.");
        }
        if (tiempoFin == null) {
            throw new IllegalStateException(
                "No se puede obtener el tiempo: debe llamar a iniciar() y luego a detener() antes de obtener el tiempo transcurrido.");
        }
        return calcularMs();
    }

    /**
     * Devuelve el tiempo formateado como string.
     * Ejemplos: "123 ms", "1.2 s"
     *
     * @return string con el tiempo formateado
     * @throws IllegalStateException si se llama antes de invocar detener()
     */
    public String getTiempoFormateado() {
        long ms = Math.round(getTiempoMs());
        if (ms < 1000) {
            return String.format(Locale.ROOT, "%d ms", ms);
        }
        return String.format(Locale.ROOT, "%.1f s", ms / 1000.0);
    }

    private double calcularMs() {
        return (tiempoFin - tiempoInicio) / 1_000_000.0;
    }
}