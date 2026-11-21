package edu.sisinf.chuspadb.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Controller de la barra de estado al pie de la ventana principal.
 * Muestra información contextual de la sesión: conexión activa,
 * tiempo de la última query, filas afectadas y un mensaje libre.
 * Es un componente puramente pasivo: solo muestra lo que se le indica.
 */
public class BarraEstadoController {
    private static final Logger logger = LoggerFactory.getLogger(BarraEstadoController.class);

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
     * Actualiza el label de mensaje libre.
     *
     * @param mensaje Mensaje a mostrar. Si es {@code null}, muestra texto vacío.
     */
    public void setMensaje(String mensaje) {
        ejecutarEnHiloUI(() -> {
            if (labelMensaje == null) {
                logger.warn("labelMensaje no está inicializado en BarraEstadoController.");
                return;
            }
            labelMensaje.setText(mensaje != null ? mensaje : "");
        });
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
        Runnable tareaSegura = () -> {
            try {
                accion.run();
            } catch (Exception e) {
                if (e.getCause() instanceof SQLException sqlEx) {
                    logger.error("Error SQL capturado al actualizar la barra de estado (SQLState={}): {}",
                            sqlEx.getSQLState(), sqlEx.getMessage(), sqlEx);
                } else {
                    logger.error("Error inesperado al actualizar la interfaz de BarraEstadoController.", e);
                }
            }
        };

        try {
            if (Platform.isFxApplicationThread()) {
                tareaSegura.run();
            } else {
                Platform.runLater(tareaSegura);
            }
        } catch (IllegalStateException e) {
            // Permite que pruebas unitarias sin toolkit JavaFX inicializado ejecuten sin bloquearse
            logger.debug("Toolkit JavaFX no inicializado; ejecutando actualización directa en el hilo actual.");
            tareaSegura.run();
        }
    }
}
