package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.core.ErrorQuery;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import edu.sisinf.chuspadb.servicio.ExportadorExcel;
import edu.sisinf.chuspadb.servicio.ExportadorJSON;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Controller del panel de resultado de queries.
 */
public class PanelResultadoQueryController {

    private static final Logger logger = LoggerFactory.getLogger(PanelResultadoQueryController.class);
    private static final int TAMANO_PAGINA_INICIAL = 50;

    private static final ExecutorService EXPORT_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread hilo = new Thread(runnable, "chuspadb-resultado-export-worker");
        hilo.setDaemon(true);
        return hilo;
    });

    @FXML
    private TableView<List<Object>> tablaResultado;

    @FXML
    private Label labelEstado;

    @FXML
    private Button botonExportar;

    private final ExportadorJSON exportadorJSON = new ExportadorJSON();
    private final ExportadorExcel exportadorExcel = new ExportadorExcel();

    private ResultadoQuery resultadoActual;

    private Consumer<ResultadoQuery> onExportar;

    // --- Paginación (controles creados por código, sin tocar el FXML) ---
    private final ComboBox<Integer> comboTamanoPagina = new ComboBox<>();
    private final Button botonAnterior = new Button("Anterior");
    private final Button botonSiguiente = new Button("Siguiente");
    private final Label labelPagina = new Label();

    private List<List<Object>> todasLasFilas = new ArrayList<>();
    private int tamanoPagina = TAMANO_PAGINA_INICIAL;
    private int paginaActual = 0;

    @FXML
    public void initialize() {

        ContextMenu menuExportar = new ContextMenu();

        MenuItem itemCsv = new MenuItem("Exportar a CSV...");
        itemCsv.setOnAction(event -> {
            if (onExportar == null || resultadoActual == null) {
                logger.warn("Exportación CSV omitida: callback o resultadoActual no disponibles.");
                return;
            }
            ejecutarExportacionAsincrona("Exportando resultado a CSV...", () -> onExportar.accept(resultadoActual));
        });

        MenuItem itemJson = new MenuItem("Exportar a JSON...");
        itemJson.setOnAction(event -> exportarJSON());

        MenuItem itemExcel = new MenuItem("Exportar a Excel...");
        itemExcel.setOnAction(event -> exportarExcel());

        menuExportar.getItems().addAll(itemCsv, itemJson, itemExcel);

        botonExportar.setOnAction(event ->
                menuExportar.show(
                        botonExportar,
                        botonExportar.getScene().getWindow().getX(),
                        botonExportar.getScene().getWindow().getY()
                )
        );

        botonExportar.setContextMenu(menuExportar);

        inicializarPaginacion();
    }

    // ------------------------------------------------------------------
    // Paginación
    // ------------------------------------------------------------------

    private void inicializarPaginacion() {

        comboTamanoPagina.getItems().setAll(50, 100, 500);
        comboTamanoPagina.setValue(TAMANO_PAGINA_INICIAL);

        comboTamanoPagina.valueProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                tamanoPagina = nuevo;
                paginaActual = 0;
                mostrarPagina();
            }
        });

        botonAnterior.setOnAction(event -> irAPagina(paginaActual - 1));
        botonSiguiente.setOnAction(event -> irAPagina(paginaActual + 1));

        Label etiquetaTamano = new Label("Filas por página:");
        List<Node> controles = List.of(
                etiquetaTamano, comboTamanoPagina,
                botonAnterior, labelPagina, botonSiguiente
        );

        // Se agregan junto al botón exportar, dentro de su mismo contenedor.
        Node padre = botonExportar.getParent();
        if (padre instanceof Pane) {
            ((Pane) padre).getChildren().addAll(controles);
        } else if (padre instanceof ToolBar) {
            ((ToolBar) padre).getItems().addAll(controles);
        }

        habilitarPaginacion(false);
    }

    private void habilitarPaginacion(boolean habilitada) {
        comboTamanoPagina.setDisable(!habilitada);
        botonAnterior.setDisable(true);
        botonSiguiente.setDisable(true);
        if (!habilitada) {
            labelPagina.setText("");
        }
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil(todasLasFilas.size() / (double) tamanoPagina));
    }

    private void irAPagina(int pagina) {
        if (pagina < 0 || pagina >= totalPaginas()) {
            return;
        }
        paginaActual = pagina;
        mostrarPagina();
    }

    private void mostrarPagina() {

        int desde = paginaActual * tamanoPagina;
        int hasta = Math.min(desde + tamanoPagina, todasLasFilas.size());

        // Solo se muestra el bloque actual; los datos siguen en memoria
        // y la consulta no se vuelve a ejecutar.
        tablaResultado.setItems(
                FXCollections.observableArrayList(todasLasFilas.subList(desde, hasta))
        );

        labelPagina.setText("Página " + (paginaActual + 1) + " de " + totalPaginas());

        comboTamanoPagina.setDisable(false);
        botonAnterior.setDisable(paginaActual == 0);
        botonSiguiente.setDisable(paginaActual >= totalPaginas() - 1);
    }

    // ------------------------------------------------------------------
    // Exportación
    // ------------------------------------------------------------------

    private void exportarJSON() {
        if (resultadoActual == null) {
            logger.warn("Intento de exportar a JSON sin un resultado activo.");
            return;
        }

        File archivo = elegirArchivo("JSON", "*.json");
        if (archivo == null) {
            return;
        }

        final ResultadoQuery snapshot = resultadoActual;
        ejecutarExportacionAsincrona("Exportando resultado a JSON...", () -> {
            try {
                exportadorJSON.exportar(snapshot, archivo);
            } catch (ErrorPersistencia e) {
                logger.error("ErrorPersistencia al exportar JSON en '{}': {}", archivo.getAbsolutePath(), e.getMessage(), e);
                ejecutarEnHiloUI(() -> mostrarError("No se pudo exportar JSON", obtenerMensaje(e)));
            } catch (Exception e) {
                logger.error("Error inesperado al exportar JSON en '{}'", archivo.getAbsolutePath(), e);
                ejecutarEnHiloUI(() -> mostrarError("No se pudo exportar JSON", obtenerMensaje(e)));
            }
        });
    }

    private void exportarExcel() {
        if (resultadoActual == null) {
            logger.warn("Intento de exportar a Excel sin un resultado activo.");
            return;
        }

        File archivo = elegirArchivo("Excel", "*.xlsx");
        if (archivo == null) {
            return;
        }

        final ResultadoQuery snapshot = resultadoActual;
        ejecutarExportacionAsincrona("Exportando resultado a Excel...", () -> {
            try {
                exportadorExcel.exportar(snapshot, archivo);
            } catch (ErrorPersistencia e) {
                logger.error("ErrorPersistencia al exportar Excel en '{}': {}", archivo.getAbsolutePath(), e.getMessage(), e);
                ejecutarEnHiloUI(() -> mostrarError("No se pudo exportar Excel", obtenerMensaje(e)));
            } catch (Exception e) {
                logger.error("Error inesperado al exportar Excel en '{}'", archivo.getAbsolutePath(), e);
                ejecutarEnHiloUI(() -> mostrarError("No se pudo exportar Excel", obtenerMensaje(e)));
            }
        });
    }
    private void ejecutarExportacionAsincrona(String mensajeProgreso, Runnable tareaExportacion) {
        final String estadoPrevio = labelEstado != null ? labelEstado.getText() : "";
        if (botonExportar != null) {
            botonExportar.setDisable(true);
        }
        if (labelEstado != null) {
            labelEstado.setText(mensajeProgreso);
        }

        CompletableFuture.runAsync(tareaExportacion, EXPORT_EXECUTOR)
                .whenComplete((ok, ex) -> ejecutarEnHiloUI(() -> {
                    if (ex != null) {
                        logger.error("Fallo en tarea de exportación en segundo plano.", ex);
                        mostrarError("Error durante la exportación", obtenerMensaje(ex));
                    }
                    if (botonExportar != null) {
                        botonExportar.setDisable(resultadoActual == null);
                    }
                    if (labelEstado != null && mensajeProgreso.equals(labelEstado.getText())) {
                        labelEstado.setText(estadoPrevio);
                    }
                }));
    }

    private File elegirArchivo(String descripcion, String extension) {

        FileChooser chooser = new FileChooser();

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(descripcion, extension)
        );

        Window ventana = botonExportar.getScene() != null
                ? botonExportar.getScene().getWindow()
                : null;

        return chooser.showSaveDialog(ventana);
    }

    private String obtenerMensaje(Throwable e) {
        if (e == null) {
            return "Error desconocido.";
        }
        Throwable actual = e;
        while (actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }
        if (actual instanceof SQLException sqlEx) {
            return "Error SQL [" + (sqlEx.getSQLState() != null ? sqlEx.getSQLState() : "N/A") + "]: "
                    + sqlEx.getMessage();
        }
        if (e instanceof ErrorPersistencia && e.getCause() != null && e.getCause().getMessage() != null) {
            return e.getCause().getMessage();
        }
        return (actual.getMessage() != null && !actual.getMessage().isBlank())
                ? actual.getMessage()
                : actual.getClass().getSimpleName();
    }

    private void mostrarError(String encabezado, String mensaje) {
        try {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error exportando");
            alerta.setHeaderText(encabezado);
            alerta.setContentText(mensaje);
            alerta.showAndWait();
        } catch (IllegalStateException e) {
            logger.debug("Toolkit JavaFX no inicializado; omitiendo diálogo modal de alerta: {} - {}",
                    encabezado, mensaje);
        }
    }

    // ------------------------------------------------------------------
    // Mostrar resultado
    // ------------------------------------------------------------------

    public void mostrar(ResultadoQuery resultado) {
        ejecutarEnHiloUI(() -> {
            if (resultado == null || resultado.getTipo() == null) {
                logger.warn("Se recibió un ResultadoQuery nulo o sin tipo definido en PanelResultadoQueryController.");
                this.resultadoActual = null;
                limpiarTablaYPaginacion();
                if (labelEstado != null) {
                    labelEstado.setText("Error: Resultado de consulta inválido o vacío.");
                }
                if (botonExportar != null) {
                    botonExportar.setDisable(true);
                }
                return;
            }

            this.resultadoActual = resultado;
            limpiarTablaYPaginacion();

            switch (resultado.getTipo()) {

                case LECTURA:
                    List<String> columnas = resultado.getColumnas() != null
                            ? resultado.getColumnas()
                            : Collections.emptyList();

                    for (int i = 0; i < columnas.size(); i++) {
                        final int indice = i;

                        TableColumn<List<Object>, Object> columna =
                                new TableColumn<>(columnas.get(i));

                        columna.setCellValueFactory(data -> {
                            List<Object> fila = data.getValue();
                            Object valor = (fila != null && indice < fila.size()) ? fila.get(indice) : null;
                            return new ReadOnlyObjectWrapper<>(valor);
                        });

                        tablaResultado.getColumns().add(columna);
                    }

                    todasLasFilas = resultado.getFilas() != null
                            ? resultado.getFilas()
                            : new ArrayList<>();
                    mostrarPagina();

                    labelEstado.setText(
                            todasLasFilas.size()
                                    + " filas (" +
                                    resultado.getTiempoMs() +
                                    " ms)"
                    );

                    botonExportar.setDisable(false);
                    break;

                case ESCRITURA:
                    labelEstado.setText(
                            resultado.getFilasAfectadas()
                                    + " filas afectadas (" +
                                    resultado.getTiempoMs() +
                                    " ms)"
                    );

                    botonExportar.setDisable(true);
                    break;

                case ERROR:
                    String detalleError = (resultado.getMensaje() != null && !resultado.getMensaje().isBlank())
                            ? resultado.getMensaje()
                            : "Fallo desconocido al ejecutar la consulta SQL.";
                    logger.error("Error de ejecución SQL recibido en PanelResultadoQueryController: {}", detalleError);
                    labelEstado.setText("Error SQL: " + detalleError);
                    botonExportar.setDisable(true);
                    break;
            }
        });
    }

    public void mostrarErrorSql(Throwable error) {
        if (error == null) {
            logger.warn("mostrarErrorSql recibió una excepción nula.");
            return;
        }

        String mensajeFormateado;
        if (error instanceof ErrorQuery) {
            logger.error("ErrorQuery capturado en PanelResultadoQueryController: {}", error.getMessage(), error);
            mensajeFormateado = "Error en consulta SQL: " + obtenerMensaje(error);
        } else if (error instanceof ErrorConexion) {
            logger.error("ErrorConexion capturado en PanelResultadoQueryController: {}", error.getMessage(), error);
            mensajeFormateado = "Error de conexión BD: " + obtenerMensaje(error);
        } else if (error instanceof SQLException sqlEx) {
            logger.error("SQLException capturada (SQLState={}, ErrorCode={}): {}",
                    sqlEx.getSQLState(), sqlEx.getErrorCode(), sqlEx.getMessage(), sqlEx);
            mensajeFormateado = "Error SQL [" + (sqlEx.getSQLState() != null ? sqlEx.getSQLState() : "N/A") + "]: "
                    + sqlEx.getMessage();
        } else {
            logger.error("Error inesperado de ejecución SQL en PanelResultadoQueryController.", error);
            mensajeFormateado = "Error SQL: " + obtenerMensaje(error);
        }

        final String textoFinal = mensajeFormateado;
        ejecutarEnHiloUI(() -> {
            this.resultadoActual = null;
            limpiarTablaYPaginacion();
            if (labelEstado != null) {
                labelEstado.setText(textoFinal);
            }
            if (botonExportar != null) {
                botonExportar.setDisable(true);
            }
        });
    }

    private void limpiarTablaYPaginacion() {
        if (tablaResultado != null) {
            tablaResultado.getColumns().clear();
            tablaResultado.getItems().clear();
        }
        todasLasFilas = new ArrayList<>();
        paginaActual = 0;
        habilitarPaginacion(false);
    }

    private void ejecutarEnHiloUI(Runnable accion) {
        try {
            if (Platform.isFxApplicationThread()) {
                accion.run();
            } else {
                Platform.runLater(accion);
            }
        } catch (IllegalStateException e) {
            logger.debug("Toolkit JavaFX no inicializado; ejecutando actualización directa.");
            accion.run();
        }
    }

    public void setOnExportar(Consumer<ResultadoQuery> callback) {
        this.onExportar = callback;
    }

    public TableView<List<Object>> getTablaResultado() {
        return tablaResultado;
    }

    public Label getLabelEstado() {
        return labelEstado;
    }

    public Button getBotonExportar() {
        return botonExportar;
    }
}
