package edu.sisinf.chuspadb.servicio;

import java.sql.Connection;
import java.util.function.Consumer;

import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Ejecuta consultas SQL en un hilo de fondo (background) utilizando JavaFX Task,
 * manteniendo la interfaz de usuario (UI) responsiva durante consultas largas.
 */
public class EjecutorQueryAsync {

    private final EjecutorQuery ejecutorQuery;

    public EjecutorQueryAsync() {
        this.ejecutorQuery = new EjecutorQuery();
    }

    /**
     * Ejecuta una consulta SQL en un hilo de fondo.
     * Invoca {@code onSuccess} o {@code onError} en el hilo de la interfaz de usuario de JavaFX.
     *
     * @param sql       Sentencia SQL a ejecutar.
     * @param conexion  Datos de la conexión utilizados para abrir la conexión JDBC.
     * @param dao       DAO utilizado para abrir la conexión JDBC.
     * @param onSuccess Callback invocado con el resultado de la consulta en el hilo de la UI.
     * @param onError   Callback invocado con la excepción en el hilo de la UI si la consulta falla.
     */
    public void ejecutar(
            String sql,
            Conexion conexion,
            IConexionDAO dao,
            Consumer<ResultadoQuery> onSuccess,
            Consumer<Throwable> onError) {

        Task<ResultadoQuery> task = new Task<>() {
            @Override
            protected ResultadoQuery call() throws Exception {
                // El bloque try-with-resources asegura que la conexión se cierre
                // automáticamente al finalizar el bloque, incluso si ocurre una excepción.
                try (Connection connection = dao.abrir(conexion)) {
                    return ejecutorQuery.ejecutar(connection, sql);
                }
            }
        };

        // Evento que se ejecuta automáticamente cuando la tarea en background tiene éxito
        task.setOnSucceeded(event -> {
            ResultadoQuery result = task.getValue();
            if (onSuccess != null) {
                // Platform.runLater garantiza explícitamente el retorno seguro al hilo de la UI
                Platform.runLater(() -> onSuccess.accept(result));
            }
        });

        // Evento que se ejecuta automáticamente si la tarea en background lanza un error
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            if (onError != null) {
                Platform.runLater(() -> onError.accept(error));
            }
        });

        // Configuración e inicio del hilo de fondo
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true); // Evita que el hilo bloquee el cierre de la aplicación
        backgroundThread.start();
    }
}
