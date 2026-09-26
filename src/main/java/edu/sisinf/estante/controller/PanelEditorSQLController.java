package edu.sisinf.estante.controller;

import edu.sisinf.estante.modelo.ResultadoQuery;
import edu.sisinf.estante.servicio.EjecutorQueryAsync;
import edu.sisinf.estante.util.SqlValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller del panel editor SQL con resaltado de sintaxis y ejecución asíncrona.
 * Usa RichTextFX CodeArea para resaltado SQL y EjecutorQueryAsync para no bloquear la UI.
 */
public class PanelEditorSQLController {

    private static final String[] KEYWORDS = {
        "SELECT", "FROM", "WHERE", "JOIN", "ON", "GROUP BY", "ORDER BY",
        "HAVING", "INSERT", "UPDATE", "DELETE", "INTO", "VALUES", "SET",
        "CREATE", "DROP", "ALTER", "TABLE", "INDEX", "VIEW", "AND", "OR",
        "NOT", "NULL", "AS", "DISTINCT", "LIMIT", "OFFSET", "INNER",
        "LEFT", "RIGHT", "OUTER", "FULL", "CROSS", "UNION", "ALL"
    };

    private static final Pattern PATRON = Pattern.compile(
        "(?<KEYWORD>\\b(" + String.join("|", KEYWORDS) + ")\\b)"
        + "|(?<STRING>'[^']*')"
        + "|(?<COMMENT>--[^\n]*)",
        Pattern.CASE_INSENSITIVE
    );

    @FXML private Button btnEjecutar;
    @FXML private Button btnLimpiar;
    @FXML private Label labelConexionActiva;
    @FXML private CodeArea areaSQL;
    @FXML private ProgressIndicator spinnerEjecucion;

    private Consumer<String> onEjecutar;
    private Consumer<ResultadoQuery> onResultado;
    private Consumer<Throwable> onError;

    /**
     * Inicializa el controller conectando los botones y el resaltado de sintaxis.
     */
    @FXML
    public void initialize() {
        btnLimpiar.setOnAction(e -> areaSQL.clear());
        btnEjecutar.setOnAction(e -> disparar());

        spinnerEjecucion.setVisible(false);

        areaSQL.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                event.consume();
                disparar();
            }
        });

        areaSQL.textProperty().addListener((obs, oldText, newText) ->
            areaSQL.setStyleSpans(0, calcularResaltado(newText))
        );
        areaSQL.getStylesheets().add(
            getClass().getResource("/css/estante.css").toExternalForm()
        );
    }

    private StyleSpans<Collection<String>> calcularResaltado(String texto) {
        Matcher matcher = PATRON.matcher(texto);
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
        int ultimo = 0;

        while (matcher.find()) {
            String estilo =
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("STRING")  != null ? "string"  :
                "comment";

            builder.add(Collections.emptyList(), matcher.start() - ultimo);
            builder.add(Collections.singleton(estilo), matcher.end() - matcher.start());
            ultimo = matcher.end();
        }

        builder.add(Collections.emptyList(), texto.length() - ultimo);
        return builder.create();
    }

    /**
     * Registra el callback que se invoca al ejecutar una query (modo sincrónico legacy).
     */
    public void setOnEjecutar(Consumer<String> callback) {
        this.onEjecutar = callback;
    }

    /**
     * Registra el callback invocado cuando la query termina exitosamente (modo async).
     */
    public void setOnResultado(Consumer<ResultadoQuery> callback) {
        this.onResultado = callback;
    }

    /**
     * Registra el callback invocado cuando la query falla (modo async).
     */
    public void setOnError(Consumer<Throwable> callback) {
        this.onError = callback;
    }

    /**
     * Actualiza el label de conexión activa.
     */
    public void setConexionActiva(String descripcion) {
        labelConexionActiva.setText(descripcion != null ? descripcion : "Sin conexión");
    }

    /**
     * Devuelve el texto actual del editor SQL.
     */
    public String getTextoSQL() {
        return areaSQL.getText();
    }

    /**
     * Dispara la ejecución mostrando el spinner y deshabilitando el botón.
     */
    private void disparar() {
        String texto = areaSQL.getText();
        if (texto.isBlank()) return;

        if (SqlValidator.esDestructiva(texto)) {
            boolean confirmar = DialogoConfirmacionDML.confirmar(texto);
            if (!confirmar) return;
        }

        // Mostrar spinner y deshabilitar botón
        spinnerEjecucion.setVisible(true);
        btnEjecutar.setDisable(true);

        if (onResultado != null) {
            // Modo asíncrono — el caller conectó setOnResultado
            if (onEjecutar != null) {
                onEjecutar.accept(texto);
            }
        } else if (onEjecutar != null) {
            // Modo legado sincrónico
            onEjecutar.accept(texto);
            ocultarSpinner();
        }
    }

    /**
     * Oculta el spinner y rehabilita el botón Ejecutar.
     * Debe llamarse desde el hilo de la UI al finalizar la ejecución.
     */
    public void ocultarSpinner() {
        spinnerEjecucion.setVisible(false);
        btnEjecutar.setDisable(false);
    }
}
