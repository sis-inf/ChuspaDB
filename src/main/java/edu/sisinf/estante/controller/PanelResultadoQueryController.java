package edu.sisinf.estante.controller;

import edu.sisinf.estante.core.ErrorPersistencia;
import edu.sisinf.estante.modelo.ResultadoQuery;
import edu.sisinf.estante.servicio.ExportadorExcel;
import edu.sisinf.estante.servicio.ExportadorJSON;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller del panel de resultado de queries.
 */
public class PanelResultadoQueryController {

    private static final int TAMANO_PAGINA_INICIAL = 50;

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
            if (onExportar != null && resultadoActual != null) {
                onExportar.accept(resultadoActual);
            }
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

        try {
            File archivo = elegirArchivo("JSON", "*.json");

            if (archivo == null) {
                return;
            }

            exportadorJSON.exportar(resultadoActual, archivo);

        } catch (Exception e) {
            mostrarError("No se pudo exportar JSON", obtenerMensaje(e));
        }
    }

    private void exportarExcel() {

        try {
            File archivo = elegirArchivo("Excel", "*.xlsx");

            if (archivo == null) {
                return;
            }

            exportadorExcel.exportar(resultadoActual, archivo);

        } catch (Exception e) {
            mostrarError("No se pudo exportar Excel", obtenerMensaje(e));
        }
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

    private String obtenerMensaje(Exception e) {
        return e instanceof ErrorPersistencia && e.getCause() != null
                ? e.getCause().getMessage()
                : e.getMessage();
    }

    private void mostrarError(String encabezado, String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error exportando");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // ------------------------------------------------------------------
    // Mostrar resultado
    // ------------------------------------------------------------------

    public void mostrar(ResultadoQuery resultado) {
        this.resultadoActual = resultado;

        tablaResultado.getColumns().clear();
        tablaResultado.getItems().clear();

        todasLasFilas = new ArrayList<>();
        paginaActual = 0;
        habilitarPaginacion(false);

        switch (resultado.getTipo()) {

            case LECTURA:
                List<String> columnas = resultado.getColumnas();

                for (int i = 0; i < columnas.size(); i++) {
                    final int indice = i;

                    TableColumn<List<Object>, Object> columna =
                            new TableColumn<>(columnas.get(i));

                    columna.setCellValueFactory(data ->
                            new ReadOnlyObjectWrapper<>(
                                    data.getValue().get(indice)
                            ));

                    tablaResultado.getColumns().add(columna);
                }

                todasLasFilas = resultado.getFilas();
                mostrarPagina();

                labelEstado.setText(
                        resultado.getFilas().size()
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
                labelEstado.setText(
                        "Error: " + resultado.getMensaje()
                );

                botonExportar.setDisable(true);
                break;
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