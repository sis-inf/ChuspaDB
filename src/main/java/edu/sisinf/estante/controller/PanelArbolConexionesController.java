package edu.sisinf.estante.controller;

import edu.sisinf.estante.modelo.Conexion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller del panel izquierdo de Estante.
 */
public class PanelArbolConexionesController {

    @FXML private Button btnNuevaConexion;
    @FXML private Button btnRefrescar;
    @FXML private Button btnEliminar;
    @FXML private TextField txtBusqueda;
    @FXML private TreeView<Object> arbol;

    private Consumer<Conexion> onConexionDoubleClick = null;
    private Consumer<String> onTablaClick = null;

    private final List<Conexion> conexionesOriginales = new ArrayList<>();

    @FXML
    public void initialize() {
        TreeItem<Object> raiz = new TreeItem<>("Conexiones");
        raiz.setExpanded(true);
        arbol.setRoot(raiz);

        arbol.setOnMouseClicked(event -> {
            TreeItem<Object> seleccionado =
                    arbol.getSelectionModel().getSelectedItem();

            if (seleccionado != null
                    && seleccionado.getValue() instanceof String valor
                    && arbol.getTreeItemLevel(seleccionado) == 3) {
                if (onTablaClick != null) {
                    onTablaClick.accept(valor);
                }
            }

            if (event.getClickCount() == 2) {
                if (seleccionado != null
                        && seleccionado.getValue() instanceof Conexion conexion) {
                    if (onConexionDoubleClick != null) {
                        onConexionDoubleClick.accept(conexion);
                    }
                }
            }
        });

        txtBusqueda.textProperty().addListener(
            (javafx.beans.value.ObservableValue<? extends String> obs,
             String viejo, String nuevo) -> filtrarArbol(nuevo)
        );
    }

    public void cargarConexiones(List<Conexion> lista) {
        if (lista == null) return;
        conexionesOriginales.clear();
        conexionesOriginales.addAll(lista);
        filtrarArbol(txtBusqueda.getText());
    }

    private void filtrarArbol(String filtro) {
        TreeItem<Object> raiz = arbol.getRoot();
        raiz.getChildren().clear();

        if (filtro == null || filtro.isBlank()) {
            for (Conexion conexion : conexionesOriginales) {
                TreeItem<Object> item = new TreeItem<>(conexion);
                item.getChildren().add(new TreeItem<>("(cargando...)"));
                raiz.getChildren().add(item);
            }
        } else {
            String termino = filtro.toLowerCase().trim();
            for (Conexion conexion : conexionesOriginales) {
                String nombre = conexion != null ? conexion.toString().toLowerCase() : "";
                if (nombre.contains(termino)) {
                    TreeItem<Object> item = new TreeItem<>(conexion);
                    item.getChildren().add(new TreeItem<>("(cargando...)"));
                    raiz.getChildren().add(item);
                }
            }
        }
    }

    public void setOnConexionDoubleClick(Consumer<Conexion> callback) {
        this.onConexionDoubleClick = callback;
    }

    // Nivel 3 = tabla (raiz 0 > conexion 1 > esquema 2 > tabla 3). Sin consumidor:
    // App.java no llama a setOnTablaClick y aun no crea nodos de tabla.
    public void setOnTablaClick(Consumer<String> callback) {
        this.onTablaClick = callback;
    }

    public TreeItem<Object> getNodoSeleccionado() {
        return arbol.getSelectionModel().getSelectedItem();
    }

    public Button getBotonNuevaConexion() { return btnNuevaConexion; }
    public Button getBotonRefrescar()     { return btnRefrescar; }
    public Button getBotonEliminar()      { return btnEliminar; }
}