package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.modelo.Conexion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller del panel izquierdo de ChuspaDB.
 */
public class PanelArbolConexionesController {

    @FXML private Button btnNuevaConexion;
    @FXML private Button btnRefrescar;
    @FXML private Button btnEliminar;
    @FXML private TextField txtBusqueda;
    @FXML private TreeView<Object> arbol;

    private Consumer<Conexion> onConexionDoubleClick = null;
    private Consumer<String> onTablaClick = null;

    private final Map<TreeItem<Object>, List<TreeItem<Object>>> hijosOriginales =
            new IdentityHashMap<>();

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
        TreeItem<Object> raiz = arbol.getRoot();
        hijosOriginales.clear();
        raiz.getChildren().clear();
        for (Conexion conexion : lista) {
            TreeItem<Object> item = new TreeItem<>(conexion);
            item.getChildren().add(new TreeItem<>("(cargando...)"));
            raiz.getChildren().add(item);
        }
        filtrarArbol(txtBusqueda.getText());
    }

    private void filtrarArbol(String filtro) {
        TreeItem<Object> raiz = arbol.getRoot();
        restaurarHijos(raiz);

        if (filtro == null || filtro.isBlank()) {
            return;
        }
        filtrarHijos(raiz, filtro.toLowerCase().trim());
    }

    private boolean filtrarHijos(TreeItem<Object> item, String termino) {
        List<TreeItem<Object>> hijos = new ArrayList<>(item.getChildren());
        List<TreeItem<Object>> visibles = new ArrayList<>();
        for (TreeItem<Object> hijo : hijos) {
            if (coincide(hijo, termino)) {
                visibles.add(hijo);
            } else if (filtrarHijos(hijo, termino)) {
                hijo.setExpanded(true);
                visibles.add(hijo);
            }
        }
        if (visibles.size() < hijos.size()) {
            hijosOriginales.put(item, hijos);
            item.getChildren().setAll(visibles);
        }
        return !visibles.isEmpty();
    }

    private void restaurarHijos(TreeItem<Object> item) {
        List<TreeItem<Object>> hijos = hijosOriginales.remove(item);
        if (hijos != null && hijos.containsAll(item.getChildren())) {
            item.getChildren().setAll(hijos);
        }
        for (TreeItem<Object> hijo : item.getChildren()) {
            restaurarHijos(hijo);
        }
    }

    private boolean coincide(TreeItem<Object> item, String termino) {
        Object valor = item.getValue();
        return valor != null && valor.toString().toLowerCase().contains(termino);
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
