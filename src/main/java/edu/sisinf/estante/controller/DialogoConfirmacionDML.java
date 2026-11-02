package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.util.SqlValidator;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * Diálogo de confirmación para queries DML y DDL.
 * Muestra el SQL que se va a ejecutar y pide confirmación
 * explícita al usuario antes de proceder.
 */
public class DialogoConfirmacionDML {

    /**
     * Muestra un diálogo de confirmación antes de ejecutar una query.
     *
     * @param sql La consulta SQL que se desea ejecutar.
     * @return {@code true} si el usuario presiona OK,
     *         {@code false} si cancela o cierra el diálogo.
     */
    public static boolean confirmar(String sql) {

        // 1. Clasificar la query
        SqlValidator.TipoQuery tipo = SqlValidator.tipo(sql);

        // 2. Determinar si es destructiva
        boolean esDestructiva = SqlValidator.esDestructiva(sql);

        // 3. Construir el Alert
        Alert.AlertType alertType = esDestructiva
                ? Alert.AlertType.WARNING
                : Alert.AlertType.CONFIRMATION;

        Alert alert = new Alert(alertType);
        alert.setTitle("Confirmar ejecución");

        // Header distinto según si es destructiva o no
        if (esDestructiva) {
            alert.setHeaderText(
                "Esta consulta (" + tipo.name() + ") puede ser destructiva. ¿Continuar?"
            );
        } else {
            alert.setHeaderText(
                "Confirmar ejecución de " + tipo.name()
            );
        }

        // 4. Construir el contenido con VBox
        Label label = new Label("Vas a ejecutar la siguiente consulta:");

        TextArea textArea = new TextArea(sql);
        textArea.setEditable(false);    // Impide que el usuario modifique la consulta SQL en el cuadro de dialogo
        textArea.setWrapText(true);    // Garantiza que las sentencias largas sean completamente visibles
        textArea.setPrefRowCount(6);

        VBox vbox = new VBox(8, label, textArea);
        alert.getDialogPane().setContent(vbox);

        // 5. Mostrar modalmente y retornar resultado
        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}