package edu.sisinf.chuspadb.servicio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Ejecuta consultas SQL en un hilo de fondo (background) utilizando JavaFX Task,
 * manteniendo la interfaz de usuario (UI) responsiva durante consultas largas.
 * Permite cancelar la consulta en ejecución mediante {@link #cancelarConsultaActiva()}.
 */
public class EjecutorQueryAsync {

    /**
     * Error entregado a {@code onError} cuando el usuario cancela la consulta.
     * Permite a la UI distinguir una cancelación de un fallo real.
     */
    public static class ConsultaCancelada extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ConsultaCancelada() {
            super("Consulta cancelada por el usuario");
        }

        public ConsultaCancelada(Throwable causa) {
            super("Consulta cancelada por el usuario", causa);
        }
    }

    private final EjecutorQuery ejecutorQuery;

    /**
     * Statement de la consulta en curso. Es volatile porque lo escribe el hilo
     * de fondo y lo lee el hilo de la UI al cancelar.
     */
    private volatile Statement statementActivo;

    /** Indica que el usuario pidió cancelar la consulta en curso. */
    private volatile boolean cancelacionSolicitada;

    public EjecutorQueryAsync() {
        this.ejecutorQuery = new EjecutorQuery();
    }

    /**
     * Ejecuta una consulta SQL en un hilo de fondo.
     * Invoca {@code onSuccess} o {@code onError} en el hilo de la interfaz de usuario de JavaFX.
     * Si la consulta se cancela con {@link #cancelarConsultaActiva()}, {@code onError}
     * recibe una {@link ConsultaCancelada}.
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
                cancelacionSolicitada = false;
                ResultadoQuery resultado;

                // El try-with-resources cierra la conexión al terminar, incluso si hay
                // excepción o cancelación.
                try (Connection connection = dao.abrir(conexion)) {
                    try {
                        resultado = ejecutorQuery.ejecutar(envolver(connection), sql);
                    } finally {
                        liberarStatement();
                    }
                } catch (Exception e) {
                    // Al cancelar, el driver lanza una SQLException (a veces envuelta
                    // por EjecutorQuery). Se distingue por la bandera, no por el mensaje,
                    // porque cada driver redacta distinto el error.
                    if (cancelacionSolicitada) {
                        throw new ConsultaCancelada(e);
                    }
                    throw e;
                }

                // Si se canceló pero el driver igual devolvió un resultado, se descarta.
                if (cancelacionSolicitada) {
                    throw new ConsultaCancelada();
                }
                return resultado;
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

    /**
     * Cancela la consulta que se está ejecutando, invocando {@code cancel()} sobre
     * su {@link Statement}. La conexión y el statement se liberan en el hilo de
     * fondo cuando la ejecución termina.
     *
     * @return {@code true} si había un statement activo y se pudo solicitar la cancelación.
     */
    public boolean cancelarConsultaActiva() {
        cancelacionSolicitada = true;

        Statement statement = statementActivo;
        if (statement == null) {
            return false;
        }

        try {
            statement.cancel();
            return true;
        } catch (SQLException e) {
            // El statement pudo terminar o cerrarse justo antes de cancelar;
            // no es un error para el usuario.
            return false;
        }
    }

    /**
     * Envuelve la conexión para registrar el Statement que cree EjecutorQuery,
     * ya que ese objeto no es accesible desde esta clase.
     */
    private Connection envolver(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[] { Connection.class },
                (proxy, metodo, args) -> {
                    try {
                        Object resultado = metodo.invoke(real, args);
                        String nombre = metodo.getName();

                        if (resultado instanceof Statement
                                && (nombre.equals("createStatement") || nombre.startsWith("prepare"))) {
                            statementActivo = (Statement) resultado;
                        }
                        return resultado;
                    } catch (InvocationTargetException e) {
                        // Se relanza la excepción original (por ejemplo SQLException)
                        throw e.getCause();
                    }
                });
    }

    /** Cierra el statement activo (si lo hay) y limpia la referencia. */
    private void liberarStatement() {
        Statement statement = statementActivo;
        statementActivo = null;

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignorada) {
                // Cierre en el mejor esfuerzo: la conexión se cierra enseguida de todos modos.
            }
        }
    }
}
