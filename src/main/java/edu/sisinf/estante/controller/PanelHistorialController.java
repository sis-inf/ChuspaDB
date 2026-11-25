package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.core.ErrorChuspaDB;
import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.core.ErrorQuery;
import edu.sisinf.chuspadb.modelo.EntradaHistorial;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

/**
 * Controller del panel de historial de queries de ChuspaDB.
 *
 * <p>Muestra las últimas queries ejecutadas en la sesión en un ListView.
 * Expone hooks para que la integración reaccione al doble clic sobre
 * una entrada, al botón de limpiar historial y a los errores de carga.</p>
 *
 * <p>La carga del historial desde una fuente que puede acceder a la base de
 * datos se hace con {@link #cargarHistorialAsync(Callable)}, que ejecuta la
 * operación en un hilo de fondo para no bloquear el hilo de JavaFX, captura
 * de forma específica los errores SQL y de dominio, y mantiene el estado de
 * la pantalla (carga en curso, botón de limpiar, mensaje de error).</p>
 *
 * <p>Archivos relacionados:</p>
 * <ul>
 *   <li>Vista: {@code src/main/resources/fxml/PanelHistorial.fxml}</li>
 * </ul>
 */
public class PanelHistorialController {

    private static final Logger logger =
            LoggerFactory.getLogger(PanelHistorialController.class);

    private static final int MAX_QUERY_CHARS = 60;
    private static final int MAX_PROFUNDIDAD_CAUSAS = 10;
    private static final String TEXTO_SIN_HISTORIAL = "Sin historial";
    private static final String TEXTO_CARGANDO = "⏳ Cargando historial...";

    private static final ExecutorService HILO_CARGA =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread hilo = new Thread(runnable, "chuspadb-historial-worker");
                hilo.setDaemon(true);
                return hilo;
            });

    @FXML private ListView<EntradaHistorial> listaHistorial;
    @FXML private Button btnLimpiar;

    private Consumer<String> onQuerySeleccionada = null;
    private Consumer<String> onError = null;
    private boolean cargando = false;

    /**
     * Inicializa el ListView con celda personalizada y registra
     * el manejador de doble clic.
     */
    @FXML
    public void initialize() {
        listaHistorial.setPlaceholder(new Label(TEXTO_SIN_HISTORIAL));

        listaHistorial.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EntradaHistorial entrada, boolean empty) {
                super.updateItem(entrada, empty);
                if (empty || entrada == null) {
                    setText(null);
                } else {
                    setText(formatearEntrada(entrada));
                }
            }
        });

        listaHistorial.setOnMouseClicked(event -> {
            if (event.getClickCount() != 2 || cargando) {
                return;
            }
            EntradaHistorial seleccionada =
                    listaHistorial.getSelectionModel().getSelectedItem();
            if (seleccionada == null
                    || seleccionada.query() == null
                    || onQuerySeleccionada == null) {
                return;
            }
            try {
                onQuerySeleccionada.accept(seleccionada.query());
            } catch (RuntimeException e) {
                logger.error("Falló el callback de query seleccionada.", e);
                notificarError(describirError(e), false);
            }
        });
    }

    /**
     * Carga la lista de entradas del historial en el ListView.
     *
     * <p>Si se invoca desde un hilo distinto al de JavaFX, la actualización
     * se delega a {@link Platform#runLater(Runnable)}. Una lista {@code null}
     * se trata como historial vacío y las entradas {@code null} se omiten.</p>
     *
     * @param entradas lista de entradas a mostrar
     */
    public void cargarHistorial(List<EntradaHistorial> entradas) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> cargarHistorial(entradas));
            return;
        }
        if (entradas == null) {
            logger.warn("Se recibió una lista de historial nula; se muestra vacío.");
            listaHistorial.getItems().clear();
        } else {
            List<EntradaHistorial> validas =
                    entradas.stream().filter(Objects::nonNull).toList();
            listaHistorial.getItems().setAll(validas);
        }
        listaHistorial.setPlaceholder(new Label(TEXTO_SIN_HISTORIAL));
    }

    /**
     * Carga el historial desde una fuente que puede acceder a la base de datos,
     * sin bloquear el hilo de JavaFX.
     *
     * <p>La fuente se ejecuta en un hilo de fondo. Mientras dura la carga se
     * deshabilita el botón de limpiar y se ignoran nuevas solicitudes. Si la
     * fuente falla, se captura el error (SQL o de dominio), se registra, se
     * muestra un mensaje claro en el panel y se avisa mediante el callback de
     * {@link #setOnError(Consumer)}.</p>
     *
     * @param fuente operación que devuelve las entradas del historial
     * @throws NullPointerException si la fuente es {@code null}
     */
    public void cargarHistorialAsync(Callable<List<EntradaHistorial>> fuente) {
        Objects.requireNonNull(fuente, "La fuente del historial no puede ser nula.");

        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> cargarHistorialAsync(fuente));
            return;
        }
        if (cargando) {
            logger.warn("Ya hay una carga de historial en curso; se ignora la solicitud.");
            return;
        }

        Task<List<EntradaHistorial>> tarea = new Task<>() {
            @Override
            protected List<EntradaHistorial> call() throws Exception {
                return fuente.call();
            }
        };

        tarea.setOnSucceeded(evento -> {
            finalizarCarga();
            cargarHistorial(tarea.getValue());
        });

        tarea.setOnFailed(evento -> {
            finalizarCarga();
            Throwable error = tarea.getException();
            logger.error("Falló la carga del historial.", error);
            notificarError(describirError(error), true);
        });

        tarea.setOnCancelled(evento -> {
            finalizarCarga();
            logger.info("La carga del historial fue cancelada.");
        });

        iniciarCarga();
        try {
            HILO_CARGA.execute(tarea);
        } catch (RejectedExecutionException e) {
            finalizarCarga();
            logger.error("No se pudo programar la carga del historial.", e);
            notificarError("No se pudo iniciar la carga del historial.", true);
        }
    }

    /**
     * Vacía visualmente el ListView del historial.
     */
    @FXML
    public void limpiarHistorial() {
        if (cargando) {
            logger.warn("No se limpia el historial mientras hay una carga en curso.");
            return;
        }
        listaHistorial.getItems().clear();
        listaHistorial.setPlaceholder(new Label(TEXTO_SIN_HISTORIAL));
    }

    /**
     * Registra el callback invocado cuando el usuario hace doble clic
     * sobre una entrada del historial.
     *
     * @param callback Consumer que recibe la query seleccionada
     */
    public void setOnQuerySeleccionada(Consumer<String> callback) {
        this.onQuerySeleccionada = callback;
    }

    /**
     * Registra el callback invocado cuando falla la carga del historial o la
     * acción de doble clic. Recibe un mensaje legible para el usuario, por
     * ejemplo para mostrarlo en la barra de estado.
     *
     * @param callback Consumer que recibe el mensaje de error
     */
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }

    /**
     * Indica si hay una carga de historial en curso.
     *
     * @return {@code true} si se está cargando el historial
     */
    public boolean isCargando() {
        return cargando;
    }

    /**
     * Devuelve el botón "Limpiar historial".
     *
     * @return botón de limpiar
     */
    public Button getBotonLimpiar() {
        return btnLimpiar;
    }

    private void iniciarCarga() {
        cargando = true;
        if (btnLimpiar != null) {
            btnLimpiar.setDisable(true);
        }
        listaHistorial.setPlaceholder(new Label(TEXTO_CARGANDO));
    }

    private void finalizarCarga() {
        cargando = false;
        if (btnLimpiar != null) {
            btnLimpiar.setDisable(false);
        }
    }

    private void notificarError(String mensaje, boolean vaciarLista) {
        if (vaciarLista) {
            listaHistorial.getItems().clear();
        }
        listaHistorial.setPlaceholder(new Label("❌ " + mensaje));
        if (onError != null) {
            try {
                onError.accept(mensaje);
            } catch (RuntimeException e) {
                logger.warn("Falló el callback de error del historial.", e);
            }
        }
    }

    private static String formatearEntrada(EntradaHistorial entrada) {
        String query = entrada.query() == null ? "(consulta vacía)" : entrada.query();
        if (query.length() > MAX_QUERY_CHARS) {
            query = query.substring(0, MAX_QUERY_CHARS) + "...";
        }

        String hora;
        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .withLocale(Locale.getDefault());
            hora = LocalDateTime
                    .ofInstant(
                            Instant.ofEpochMilli(entrada.timestamp()),
                            ZoneId.systemDefault()
                    )
                    .format(formatter);
        } catch (DateTimeException e) {
            logger.warn("Marca de tiempo inválida en el historial: {}",
                    entrada.timestamp());
            hora = "fecha inválida";
        }

        return "[" + hora + "] " + query;
    }

    /**
     * Traduce una excepción de la capa de datos a un mensaje legible,
     * distinguiendo errores de conexión, de consulta y SQL específicos.
     */
    static String describirError(Throwable error) {
        Throwable actual = error;
        int profundidad = 0;

        while (actual != null && profundidad < MAX_PROFUNDIDAD_CAUSAS) {
            if (actual instanceof ErrorConexion) {
                return "No se pudo conectar con la base de datos: "
                        + mensajeSeguro(actual);
            }
            if (actual instanceof ErrorQuery) {
                return "Falló la consulta del historial: " + mensajeSeguro(actual);
            }
            if (actual instanceof SQLException sql) {
                return describirSql(sql);
            }
            actual = actual.getCause();
            profundidad++;
        }

        if (error instanceof ErrorChuspaDB) {
            return "Error de la aplicación: " + mensajeSeguro(error);
        }
        return "Error inesperado al cargar el historial: " + mensajeSeguro(error);
    }

    private static String describirSql(SQLException sql) {
        String estado = sql.getSQLState();

        if (sql instanceof SQLTimeoutException
                || (estado != null && estado.startsWith("HYT"))) {
            return "Tiempo de espera agotado al consultar la base de datos.";
        }
        if (sql instanceof SQLInvalidAuthorizationSpecException
                || (estado != null && estado.startsWith("28"))) {
            return "Credenciales incorrectas o acceso no autorizado.";
        }
        if (sql instanceof SQLTransientConnectionException) {
            return "Fallo temporal de conexión. Intente nuevamente.";
        }
        if (sql instanceof SQLNonTransientConnectionException
                || (estado != null && estado.startsWith("08"))) {
            return "Se perdió o no se pudo establecer la conexión con el servidor.";
        }
        if (sql instanceof SQLSyntaxErrorException
                || (estado != null && estado.startsWith("42"))) {
            return "Error de sintaxis u objeto inexistente en la consulta: "
                    + mensajeSeguro(sql);
        }

        return "Error SQL: " + mensajeSeguro(sql)
                + (estado != null ? " [SQLState: " + estado + "]" : "")
                + (sql.getErrorCode() != 0
                        ? " [Código: " + sql.getErrorCode() + "]"
                        : "");
    }

    private static String mensajeSeguro(Throwable error) {
        if (error == null) {
            return "causa desconocida.";
        }
        String mensaje = error.getMessage();
        return (mensaje == null || mensaje.isBlank())
                ? error.getClass().getSimpleName()
                : mensaje;
    }
}
