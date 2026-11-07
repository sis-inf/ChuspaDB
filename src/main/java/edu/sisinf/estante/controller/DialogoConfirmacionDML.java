package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.util.SqlValidator;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
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
     * Si la sentencia es {@code UPDATE} o {@code DELETE} sin clausula
     * {@code WHERE}, muestra una advertencia en rojo y exige que el usuario
     * presione el botón de confirmación explícita (Cancelar queda por defecto).
     *
     * @param sql La consulta SQL que se desea ejecutar.
     * @return {@code true} si el usuario presiona OK (o la confirmación
     *         explícita en el caso sin {@code WHERE}),
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

        // UPDATE o DELETE sin WHERE: advertencia en rojo y confirmación explícita
        ButtonType confirmarSinWhere = new ButtonType(
                "Ejecutar sin WHERE", ButtonBar.ButtonData.OK_DONE);
        if (esDmlSinWhere(sql, tipo)) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setHeaderText(
                "PELIGRO: " + tipo.name() + " sin WHERE afectará TODAS las filas de la tabla."
            );

            Label advertencia = new Label(
                "Esta sentencia no tiene clausula WHERE y puede provocar pérdida masiva de datos.\n"
                + "Presiona 'Ejecutar sin WHERE' solo si estás seguro."
            );
            advertencia.setWrapText(true);
            advertencia.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            vbox.getChildren().add(0, advertencia);

            alert.getButtonTypes().setAll(confirmarSinWhere, ButtonType.CANCEL);
            Button botonConfirmar = (Button) alert.getDialogPane().lookupButton(confirmarSinWhere);
            botonConfirmar.setDefaultButton(false);
            botonConfirmar.setStyle("-fx-base: #c62828; -fx-text-fill: white;");
            Button botonCancelar = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
            botonCancelar.setDefaultButton(true);
        }

        // 5. Mostrar modalmente y retornar resultado
        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK || response == confirmarSinWhere)
                .isPresent();
    }

    /**
     * Indica si la sentencia es {@code UPDATE} o {@code DELETE} y no contiene
     * la clausula {@code WHERE}.
     *
     * @param sql  La consulta SQL a revisar.
     * @param tipo El tipo de query ya clasificado.
     * @return {@code true} si es un UPDATE/DELETE sin WHERE.
     */
    static boolean esDmlSinWhere(String sql, SqlValidator.TipoQuery tipo) {
        if (tipo != SqlValidator.TipoQuery.UPDATE && tipo != SqlValidator.TipoQuery.DELETE) {
            return false;
        }
        return sql == null || !sql.matches("(?is).*\\bWHERE\\b.*");
    }
}