package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.core.ErrorQuery;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller de la barra de estado al pie de la ventana principal.
 * Muestra información contextual de la sesión: conexión activa,
 * tiempo de la última query, filas afectadas y un mensaje libre.
 * Es un componente puramente pasivo: solo muestra lo que se le indica.
 */
public class BarraEstadoController {
    private static final Logger logger = LoggerFactory.getLogger(BarraEstadoController.class);
    private static final ExecutorService WORKER_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread hilo = new Thread(runnable, "chuspadb-barra-estado-worker");
        hilo.setDaemon(true);
        return hilo;
    });

    @FXML private Label labelConexion;
    @FXML private Label labelTiempo;
    @FXML private Label labelFilas;
    @FXML private Label labelMensaje;

    /**
     * Actualiza el label de conexión activa.
     *
     * @param texto Nombre o descripción de la conexión activa.
     *              Si es {@code null} o esta en blanco, muestra "Sin conexión".
     */
    public void setConexion(String texto) {
        ejecutarEnHiloUI(() -> {
            if (labelConexion == null) {
                logger.warn("labelConexion no está inicializado en BarraEstadoController.");
                return;
            }
            String valor = (texto != null && !texto.isBlank()) ? texto.trim() : "Sin conexión";
            labelConexion.setText(valor);
        });
    }

    /**
     * Actualiza el label de tiempo con el formato "X ms".
     *
     * @param ms Tiempo en milisegundos de la última query ejecutada.
     */
    public void setTiempo(long ms) {
        if (ms < 0) {
            logger.warn("Se recibió un tiempo de ejecución negativo ({} ms); se normaliza a 0 ms.", ms);
            ms = 0;
        }
        final long tiempoSeguro = ms;
        ejecutarEnHiloUI(() -> {
            if (labelTiempo == null) {
                logger.warn("labelTiempo no está inicializado en BarraEstadoController.");
                return;
            }
            labelTiempo.setText(tiempoSeguro + " ms");
        });
    }

    /**
     * Actualiza el label de filas con el formato "X filas".
     *
     * @param filas Número de filas devueltas o afectadas.
     */
    public void setFilas(int filas) {
        if (filas < 0) {
            logger.warn("Se recibió un conteo de filas negativo ({}); se normaliza a 0 filas.", filas);
            filas = 0;
        }
        final int filasSeguras = filas;
        ejecutarEnHiloUI(() -> {
            if (labelFilas == null) {
                logger.warn("labelFilas no está inicializado en BarraEstadoController.");
                return;
            }
            labelFilas.setText(filasSeguras + " filas");
        });
    }

    /**
     * Actualiza las etiquetas de filas y tiempo tras ejecutar una query.
     * Muestra "Filas: N" y "Tiempo: X ms".
     *
     * @param filas    Número de filas devueltas o afectadas.
     * @param tiempoMs Tiempo de ejecución en milisegundos.
     */
    public void actualizarEstadoQuery(int filas, long tiempoMs) {
        if (filas < 0 || tiempoMs < 0) {
            logger.warn("Valores negativos recibidos en actualizarEstadoQuery (filas={}, tiempoMs={}); normalizando a 0.",
                    filas, tiempoMs);
        }
        final int filasSeguras = Math.max(0, filas);
        final long tiempoSeguro = Math.max(0, tiempoMs);

        ejecutarEnHiloUI(() -> {
            if (labelFilas != null) {
                labelFilas.setText("Filas: " + filasSeguras);
            }
            if (labelTiempo != null) {
                labelTiempo.setText("Tiempo: " + tiempoSeguro + " ms");
            }
        });
    }

    /**
     * Actualiza el label de mensaje libre y registra trazas si el texto corresponde a un fallo SQL o de conexión.
     *
     * @param mensaje Mensaje a mostrar. Si es {@code null}, muestra texto vacío.
     */
    public void setMensaje(String mensaje) {
        final String textoSeguro = (mensaje != null) ? mensaje : "";
        clasificarYRegistrarMensaje(textoSeguro);
        ejecutarEnHiloUI(() -> {
            if (labelMensaje == null) {
                logger.warn("labelMensaje no está inicializado en BarraEstadoController.");
                return;
            }
            labelMensaje.setText(textoSeguro);
        });
    }
    /**
     * Procesa y muestra en la barra de estado una excepción originada en la capa de datos
     * ({@link ErrorQuery}, {@link ErrorConexion} o {@link SQLException}), extrayendo su causa raíz.
     *
     * @param error Excepción capturada durante una operación de base de datos.
     */
    public void mostrarError(Throwable error) {
        if (error == null) {
            setMensaje("Error desconocido en la operación de base de datos.");
            return;
        }

        Throwable causaRaiz = obtenerCausaRaiz(error);
        String detalle;

        if (error instanceof ErrorQuery || causaRaiz instanceof ErrorQuery) {
            logger.error("ErrorQuery recibido en BarraEstadoController: {}", error.getMessage(), error);
            detalle = "Error en consulta SQL: " + extraerMensajeLimpio(error, causaRaiz);
        } else if (error instanceof ErrorConexion || causaRaiz instanceof ErrorConexion) {
            logger.error("ErrorConexion recibido en BarraEstadoController: {}", error.getMessage(), error);
            detalle = "Error de conexión: " + extraerMensajeLimpio(error, causaRaiz);
        } else if (causaRaiz instanceof SQLException sqlEx) {
            logger.error("SQLException capturada (SQLState={}, ErrorCode={}): {}",
                    sqlEx.getSQLState(), sqlEx.getErrorCode(), sqlEx.getMessage(), sqlEx);
            detalle = "Error SQL [" + (sqlEx.getSQLState() != null ? sqlEx.getSQLState() : "N/A") + "]: "
                    + sqlEx.getMessage();
        } else {
            logger.error("Error inesperado reportado a BarraEstadoController.", error);
            detalle = "Error: " + extraerMensajeLimpio(error, causaRaiz);
        }

        actualizarEtiquetaErrorDirecto(detalle);
    }
    public CompletableFuture<Void> ejecutarSinBloquearUI(String mensajeProgreso, Runnable operacion) {
        Objects.requireNonNull(operacion, "La operación a ejecutar no puede ser nula.");
        String avisoInicial = (mensajeProgreso != null && !mensajeProgreso.isBlank())
                ? mensajeProgreso
                : "Procesando operación de base de datos...";
        setMensaje(avisoInicial);

        return CompletableFuture.runAsync(() -> {
            try {
                operacion.run();
            } catch (Exception e) {
                mostrarError(e);
                throw e;
            }
        }, WORKER_EXECUTOR);
    }
    
    /**
     * Limpia los labels de tiempo, filas y mensaje.
     * El label de conexión no se modifica.
     */
    public void limpiar() {
        ejecutarEnHiloUI(() -> {
            if (labelTiempo != null) labelTiempo.setText("");
            if (labelFilas != null) labelFilas.setText("");
            if (labelMensaje != null) labelMensaje.setText("");
        });
    }
    private void ejecutarEnHiloUI(Runnable accion) {
        try {
            if (Platform.isFxApplicationThread()) {
                accion.run();
            } else {
                Platform.runLater(accion);
            }
        } catch (IllegalStateException e) {
            logger.debug("Toolkit JavaFX no inicializado; ejecutando actualización directa en el hilo actual.");
            accion.run();
        }
    }
    private void clasificarYRegistrarMensaje(String texto) {
        if (texto.isEmpty()) {
            return;
        }
        String lower = texto.toLowerCase();
        if (lower.contains("conexión") || lower.contains("conexion") || lower.contains("conectar")
                || lower.contains("timeout") || lower.contains("driver")) {
            logger.error("Fallo de conexión reportado a BarraEstadoController: {}", texto);
        } else if (lower.contains("sql") || lower.contains("query") || lower.contains("consulta")
                || lower.contains("syntax") || lower.contains("error")) {
            logger.error("Fallo de ejecución SQL reportado a BarraEstadoController: {}", texto);
        } else {
            logger.debug("Mensaje de estado en BarraEstadoController: {}", texto);
        }
    }

    private void actualizarEtiquetaErrorDirecto(String detalle) {
        ejecutarEnHiloUI(() -> {
            if (labelMensaje == null) {
                logger.warn("labelMensaje no está inicializado en BarraEstadoController.");
                return;
            }
            labelMensaje.setText(detalle);
        });
    }

    private Throwable obtenerCausaRaiz(Throwable error) {
        Throwable actual = error;
        while (actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }
        return actual;
    }

    private String extraerMensajeLimpio(Throwable error, Throwable causaRaiz) {
        if (error.getMessage() != null && !error.getMessage().isBlank()) {
            return error.getMessage();
        }
        if (causaRaiz != null && causaRaiz.getMessage() != null && !causaRaiz.getMessage().isBlank()) {
            return causaRaiz.getMessage();
        }
        return error.getClass().getSimpleName();
    }
}
