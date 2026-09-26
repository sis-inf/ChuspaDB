package edu.sisinf.estante.controller;

import edu.sisinf.estante.modelo.ColumnaInfo;
import edu.sisinf.estante.servicio.ExploradorEsquemas;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.util.List;

/**
 * Controller del panel de información de tabla.
 *
 * Muestra las columnas de la tabla seleccionada en el árbol de conexiones
 * en un TableView con columnas Nombre, Tipo, Nullable y Default.
 */
public class PanelInfoTablaController {

    @FXML private Label labelNombreTabla;
    @FXML private TableView<ColumnaInfo> tablaColumnas;
    @FXML private TableColumn<ColumnaInfo, String> colNombre;
    @FXML private TableColumn<ColumnaInfo, String> colTipo;
    @FXML private TableColumn<ColumnaInfo, String> colNullable;
    @FXML private TableColumn<ColumnaInfo, String> colDefault;

    private final ExploradorEsquemas explorador = new ExploradorEsquemas();

    /**
     * Inicializa el TableView con las columnas correspondientes.
     */
    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().nombre()));

        colTipo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().tipoSQL()));

        colNullable.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().nullable() ? "Sí" : "No"));

        colDefault.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().valorDefault() != null
                                ? data.getValue().valorDefault()
                                : ""
                ));
    }

    /**
     * Carga la información de columnas de la tabla indicada.
     *
     * @param tabla     nombre de la tabla
     * @param conexion  conexión JDBC activa
     */    
public void mostrarTabla(String tabla, String esquema, Connection conexion) {
    labelNombreTabla.setText("Tabla: " + tabla);
    tablaColumnas.getItems().clear(); 
    List<ColumnaInfo> columnas = explorador.getColumnas(conexion, esquema, tabla);
    tablaColumnas.getItems().setAll(columnas);
}

    /**
     * Limpia el panel cuando no hay tabla seleccionada.
     */
    public void limpiar() {
        labelNombreTabla.setText("Selecciona una tabla");
        tablaColumnas.getItems().clear();
    }
}
