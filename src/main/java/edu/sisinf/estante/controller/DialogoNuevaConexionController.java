package edu.sisinf.estante.controller;

import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller del diálogo modal para crear una nueva conexión.
 *
 * <p>Este controller es deliberadamente "tonto": no contiene lógica de persistencia
 * ni de prueba de conexión. Solo gestiona la UI y construye el objeto {@link Conexion}
 * con los datos ingresados por el usuario.</p>
 *
 * <p>Los handlers de los botones "Probar" y "Guardar" se inyectan desde fuera
 * del controller para mantener bajo el acoplamiento con los servicios.</p>
 */
public class DialogoNuevaConexionController {

    // --------------------------------------------------------
    // FXML fields
    // --------------------------------------------------------

    @FXML private TextField           campoNombre;
    @FXML private ComboBox<TipoMotor> comboMotor;
    @FXML private TextField           campoHost;
    @FXML private TextField           campoPuerto;
    @FXML private TextField           campoBaseDatos;
    @FXML private TextField           campoUsuario;
    @FXML private PasswordField       campoPassword;
    @FXML private CheckBox            checkBoxSSL;
    @FXML private Label               etiquetaEstado;
    @FXML private Button              botonProbar;
    @FXML private Button              botonGuardar;
    @FXML private Button              botonCancelar;

    /** Resultado del diálogo. Es null si el usuario canceló. */
    private Conexion conexionResultado;

    // --------------------------------------------------------
    // Initialization
    // --------------------------------------------------------

    /**
     * Inicializa el diálogo:
     * <ul>
     * <li>Llena el ComboBox con los valores de {@link TipoMotor}.</li>
     * <li>Selecciona SQLITE por defecto y deshabilita los campos que no aplican.</li>
     * <li>Registra el listener del ComboBox para habilitar/deshabilitar campos.</li>
     * <li>Conecta el botón "Cancelar" para cerrar el diálogo.</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        comboMotor.getItems().addAll(TipoMotor.values());
        comboMotor.setValue(TipoMotor.SQLITE);
        actualizarCamposSegunMotor(TipoMotor.SQLITE);

        comboMotor.valueProperty().addListener(
                (observable, oldValue, newValue) -> actualizarCamposSegunMotor(newValue)
        );

        botonCancelar.setOnAction(event -> cerrar());
    }

    // --------------------------------------------------------
    // Private helpers
    // --------------------------------------------------------

    /**
     * Habilita o deshabilita los campos de red según el motor seleccionado.
     * SQLite no requiere host, puerto, usuario, password ni SSL.
     *
     * @param motor motor seleccionado en el ComboBox
     */
    private void actualizarCamposSegunMotor(TipoMotor motor) {
        boolean esSqlite = motor == TipoMotor.SQLITE;
        campoHost.setDisable(esSqlite);
        campoPuerto.setDisable(esSqlite);
        campoUsuario.setDisable(esSqlite);
        campoPassword.setDisable(esSqlite);
        checkBoxSSL.setDisable(esSqlite);
    }

    /**
     * Cierra el diálogo sin guardar.
     * El resultado queda como {@code null}.
     */
    private void cerrar() {
        ((Stage) botonCancelar.getScene().getWindow()).close();
    }

    // --------------------------------------------------------
    // Public API
    // --------------------------------------------------------

    /**
     * Construye un objeto {@link Conexion} con los valores actuales del formulario.
     *
     * <ul>
     * <li>Si el campo "Puerto" está vacío, el puerto del objeto será {@code null}.</li>
     * <li>Si el campo "Puerto" contiene un valor no numérico, el puerto será {@code null}.</li>
     * <li>Los demás campos se copian directamente desde los controles.</li>
     * </ul>
     *
     * @return objeto {@link Conexion} armado con los datos del formulario
     */
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

    /**
     * Devuelve la conexión resultado del diálogo.
     * Es {@code null} si el usuario canceló sin guardar.
     *
     * @return conexión guardada o {@code null}
     */
    public Conexion getConexionResultado() {
        return conexionResultado;
    }

    /**
     * Establece la conexión resultado del diálogo.
     * La integración llama a este método tras validar y persistir la conexión.
     *
     * @param conexion conexión a establecer como resultado
     */
    public void setConexionResultado(Conexion conexion) {
        this.conexionResultado = conexion;
    }

    /**
     * Devuelve el botón "Probar" para que la integración le conecte su handler.
     *
     * @return botón Probar
     */
    public Button getBotonProbar() {
        return botonProbar;
    }

    /**
     * Devuelve el botón "Guardar" para que la integración le conecte su handler.
     *
     * @return botón Guardar
     */
    public Button getBotonGuardar() {
        return botonGuardar;
    }

    /**
     * Devuelve la etiqueta de estado para que la integración muestre mensajes al usuario.
     *
     * @return etiqueta de estado
     */
    public Label getEtiquetaEstado() {
        return etiquetaEstado;
    }
    @FXML
private void handleProbarConexion() {
    try {
        // 1. Capturar el tiempo de inicio
        long startTime = System.currentTimeMillis();
        
        // 2. Extraer los datos ingresados en el formulario
        String nombre = campoNombre.getText();
        String motor = (comboMotor.getValue() != null) ? comboMotor.getValue().toString() : "";
        String host = campoHost.getText();
        String puerto = campoPuerto.getText();
        String baseDatos = campoBaseDatos.getText();
        String usuario = campoUsuario.getText();
        String password = campoPassword.getText();

        // 3. Construir el objeto Conexion (Ajusta los parámetros según tu constructor real)
        // Ejemplo: Conexion conexion = new Conexion(nombre, motor, host, puerto, baseDatos, usuario, password);
        
        // 4. Ejecutar la prueba con el ConexionTester del proyecto
        // boolean esExitosa = ConexionTester.probar(conexion);
        boolean esExitosa = true; // TODO: Descomenta lo de arriba y usa tu clase real aquí

        // 5. Calcular el tiempo total en milisegundos
        long tiempoMs = System.currentTimeMillis() - startTime;

        // 6. Mostrar el resultado con el ícono y los milisegundos solicitados
        if (esExitosa) {
            etiquetaEstado.setText("✅ Conexión exitosa (" + tiempoMs + " ms)");
            etiquetaEstado.setStyle("-fx-text-fill: #2ecc71;"); // Verde amigable
        } else {
            etiquetaEstado.setText("❌ Falló la conexión (" + tiempoMs + " ms)");
            etiquetaEstado.setStyle("-fx-text-fill: #e74c3c;"); // Rojo amigable
        }

    } catch (Exception e) {
        etiquetaEstado.setText("❌ Error al intentar probar: " + e.getMessage());
        etiquetaEstado.setStyle("-fx-text-fill: #e74c3c;");
    }
}
}