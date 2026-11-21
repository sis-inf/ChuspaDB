package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.config.DBConfig;
import edu.sisinf.chuspadb.core.ConnectionProvider;
import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.Map;

/**
 * Controller del diálogo modal para crear una nueva conexión.
 *
 * Gestiona la interfaz y construye el objeto Conexion.
 * Las pruebas de conexión se ejecutan en segundo plano
 * para evitar bloquear el hilo de JavaFX.
 */
public class DialogoNuevaConexionController {

    @FXML private TextField campoNombre;
    @FXML private ComboBox<TipoMotor> comboMotor;
    @FXML private TextField campoHost;
    @FXML private TextField campoPuerto;
    @FXML private TextField campoBaseDatos;
    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoPassword;
    @FXML private CheckBox checkBoxSSL;
    @FXML private Label etiquetaEstado;
    @FXML private Button botonProbar;
    @FXML private Button botonGuardar;
    @FXML private Button botonCancelar;

    private Conexion conexionResultado;
    private Map<TipoMotor, IConexionDAO> daos;

    @FXML
    public void initialize() {
        comboMotor.getItems().addAll(TipoMotor.values());
        comboMotor.setValue(TipoMotor.SQLITE);
        actualizarCamposSegunMotor(TipoMotor.SQLITE);

        comboMotor.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        actualizarCamposSegunMotor(newValue)
        );

        botonCancelar.setOnAction(event -> cerrar());
    }

    private void actualizarCamposSegunMotor(TipoMotor motor) {
        boolean esSqlite = motor == TipoMotor.SQLITE;
        campoHost.setDisable(esSqlite);
        campoPuerto.setDisable(esSqlite);
        campoUsuario.setDisable(esSqlite);
        campoPassword.setDisable(esSqlite);
        checkBoxSSL.setDisable(esSqlite);
    }

    private void cerrar() {
        ((Stage) botonCancelar.getScene().getWindow()).close();
    }

    public boolean validarFormulario() {
        String error = obtenerErrorValidacion();

        if (error == null) {
            return true;
        }

        mostrarError(error);
        return false;
    }

    private String obtenerErrorValidacion() {
        TipoMotor motor = comboMotor.getValue();

        if (motor == null) {
            return "Seleccione un motor de base de datos.";
        }

        if (campoNombre.getText() == null || campoNombre.getText().isBlank()) {
            return "El nombre de la conexión es obligatorio.";
        }

        if (motor == TipoMotor.SQLITE) {
            if (campoBaseDatos.getText() == null
                    || campoBaseDatos.getText().isBlank()) {
                return "El archivo de SQLite es obligatorio.";
            }
            return null;
        }

        if (campoHost.getText() == null || campoHost.getText().isBlank()) {
            return "El host es obligatorio.";
        }

        if (campoUsuario.getText() == null || campoUsuario.getText().isBlank()) {
            return "El usuario es obligatorio.";
        }

        String puertoTexto = campoPuerto.getText();

        if (puertoTexto == null || puertoTexto.isBlank()) {
            return "El puerto es obligatorio.";
        }

        try {
            int puerto = Integer.parseInt(puertoTexto.strip());

            if (puerto < 1 || puerto > 65535) {
                return "El puerto debe estar entre 1 y 65535.";
            }
        } catch (NumberFormatException e) {
            return "El puerto debe ser un número válido.";
        }

        return null;
    }

    public Conexion construirConexion() {
        Conexion conexion = new Conexion();
        conexion.setNombre(campoNombre.getText());
        conexion.setTipoMotor(comboMotor.getValue());
        conexion.setHost(campoHost.getText());
        conexion.setBasedatos(campoBaseDatos.getText());
        conexion.setUsuario(campoUsuario.getText());
        conexion.setPassword(campoPassword.getText());
        conexion.setUsarSSL(checkBoxSSL.isSelected());

        String puertoTexto = campoPuerto.getText();

        if (puertoTexto != null && !puertoTexto.isBlank()) {
            try {
                conexion.setPuerto(Integer.parseInt(puertoTexto.strip()));
            } catch (NumberFormatException e) {
                conexion.setPuerto(null);
            }
        } else {
            conexion.setPuerto(null);
        }

        return conexion;
    }

    public Conexion getConexionResultado() {
        return conexionResultado;
    }

    public void setConexionResultado(Conexion conexion) {
        this.conexionResultado = conexion;
    }

    public void setOnGuardar(Runnable accion) {
        botonGuardar.setOnAction(event -> accion.run());
    }

    public void mostrarEstado(String mensaje) {
        etiquetaEstado.setText(mensaje);
    }

    public void setDaos(Map<TipoMotor, IConexionDAO> daos) {
        this.daos = daos;
    }

    @FXML
    private void handleProbarConexion() {
        if (!validarFormulario()) {
            return;
        }

        if (daos == null) {
            mostrarError("No se configuraron los controladores de conexión.");
            return;
        }

        Conexion conexion = construirConexion();
        IConexionDAO dao = daos.get(conexion.getTipoMotor());

        if (dao == null) {
            mostrarError("No existe un controlador para el motor seleccionado.");
            return;
        }

        DBConfig config;

        try {
            config = new DBConfig(
                    dao.construirUrl(conexion),
                    conexion.getUsuario(),
                    conexion.getPassword(),
                    driverDe(conexion.getTipoMotor())
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError("Configuración de conexión inválida: " + e.getMessage());
            return;
        }

        Task<Long> prueba = new Task<>() {
            @Override
            protected Long call() throws SQLException {
                long inicio = System.currentTimeMillis();

                try (Connection ignored = ConnectionProvider.abrir(config)) {
                    return System.currentTimeMillis() - inicio;
                }
            }
        };

        prueba.setOnSucceeded(event -> {
            botonProbar.setDisable(false);
            etiquetaEstado.setText(
                    "✅ Conexión exitosa (" + prueba.getValue() + " ms)"
            );
            etiquetaEstado.setStyle("-fx-text-fill: #27ae60;");
        });

        prueba.setOnFailed(event -> {
            botonProbar.setDisable(false);
            mostrarError(
                    "No se pudo conectar: "
                            + describirErrorConexion(prueba.getException())
            );
        });

        botonProbar.setDisable(true);
        etiquetaEstado.setText("⏳ Probando conexión...");
        etiquetaEstado.setStyle("");

        Thread hilo = new Thread(prueba, "probar-conexion");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarError(String mensaje) {
        etiquetaEstado.setText("❌ " + mensaje);
        etiquetaEstado.setStyle("-fx-text-fill: #e74c3c;");
    }

    private static String describirErrorConexion(Throwable error) {
        Throwable causa = error;

        while (causa != null && !(causa instanceof SQLException)) {
            causa = causa.getCause();
        }

        if (causa instanceof SQLException sql) {
            String estado = sql.getSQLState();

            if (sql instanceof SQLTimeoutException
                    || (estado != null && estado.startsWith("HYT"))) {
                return "Tiempo de espera agotado al conectar con la base de datos.";
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
                return "No se pudo establecer la conexión con el servidor.";
            }

            String mensaje = sql.getMessage() != null
                    ? sql.getMessage()
                    : "Error SQL sin descripción.";

            return "Error SQL: " + mensaje
                    + (estado != null ? " [SQLState: " + estado + "]" : "")
                    + (sql.getErrorCode() != 0
                    ? " [Código: " + sql.getErrorCode() + "]"
                    : "");
        }

        return error != null && error.getMessage() != null
                ? error.getMessage()
                : "Error desconocido al probar la conexión.";
    }

    private static String driverDe(TipoMotor motor) {
        return switch (motor) {
            case SQLITE -> "org.sqlite.JDBC";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case POSTGRESQL -> "org.postgresql.Driver";
        };
    }
}
