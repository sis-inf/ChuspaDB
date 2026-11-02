package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.modelo.EntradaHistorial;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller del panel de historial de queries de ChuspaDB.
 *
 * <p>Muestra las últimas queries ejecutadas en la sesión en un ListView.
 * Expone hooks para que la integración reaccione al doble clic sobre
 * una entrada y al botón de limpiar historial.</p>
 *
 * <p>Archivos relacionados:</p>
 * <ul>
 *   <li>Vista: {@code src/main/resources/fxml/PanelHistorial.fxml}</li>
 * </ul>
 */
public class PanelHistorialController {

    private static final int MAX_QUERY_CHARS = 60;

    @FXML private ListView<EntradaHistorial> listaHistorial;
    @FXML private Button btnLimpiar;

    private Consumer<String> onQuerySeleccionada = null;

    /**
     * Inicializa el ListView con celda personalizada y registra
     * el manejador de doble clic.
     */
    @FXML
    public void initialize() {
        listaHistorial.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EntradaHistorial entrada, boolean empty) {
                super.updateItem(entrada, empty);
                if (empty || entrada == null) {
                    setText(null);
                } else {
                    Locale locale = Locale.getDefault();
                    DateTimeFormatter formatter =
                        DateTimeFormatter.ofLocalizedDateTime(
                            FormatStyle.SHORT
                        ).withLocale(locale);

                    String hora = LocalDateTime
                        .ofInstant(
                            Instant.ofEpochMilli(entrada.timestamp()),
                            ZoneId.systemDefault()
                        )
                        .format(formatter);

                    String query = entrada.query().length() > MAX_QUERY_CHARS
                            ? entrada.query().substring(0, MAX_QUERY_CHARS) + "..."
                            : entrada.query();
                    setText("[" + hora + "] " + query);
                }
            }
        });

        listaHistorial.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                EntradaHistorial seleccionada =
                        listaHistorial.getSelectionModel().getSelectedItem();
                if (seleccionada != null && onQuerySeleccionada != null) {
                    onQuerySeleccionada.accept(seleccionada.query());
                }
            }
        });
    }

    /**
     * Carga la lista de entradas del historial en el ListView.
     *
     * @param entradas lista de entradas a mostrar
     */
    public void cargarHistorial(List<EntradaHistorial> entradas) {
        listaHistorial.getItems().setAll(entradas);
    }

    /**
     * Vacía visualmente el ListView del historial.
     */
    @FXML
    public void limpiarHistorial() {
        listaHistorial.getItems().clear();
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
     * Devuelve el botón "Limpiar historial".
     *
     * @return botón de limpiar
     */
    public Button getBotonLimpiar() {
        return btnLimpiar;
    }
}
