package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.core.ErrorQuery;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
/**
 * Controller principal de la ventana base de ChuspaDB.
 */
public class VentanaPrincipalController {
    private static final Logger logger = LoggerFactory.getLogger(VentanaPrincipalController.class);

    private static final ExecutorService UI_BACKGROUND_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread hilo = new Thread(runnable, "chuspadb-ventana-principal-worker");
        hilo.setDaemon(true);
        return hilo;
    });

    @FXML
    private MenuItem menuNuevaConexion;

    @FXML
    private MenuItem menuSalir;

    @FXML
    private MenuItem menuAcercaDe;

    @FXML
    private StackPane contenedorArbol;

    @FXML
    private StackPane contenedorEditor;

    @FXML
    private StackPane contenedorResultado;

    @FXML
    private StackPane contenedorBarraEstado;

    @FXML
    public void initialize() {
        if (menuSalir != null) {
            menuSalir.setOnAction(event -> {
                logger.debug("Solicitando cierre de aplicación desde VentanaPrincipalController.");
                Platform.exit();
            });
        }

        if (menuAcercaDe != null) {
            menuAcercaDe.setOnAction(event -> {
                try {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Acerca de");
                    alert.setHeaderText("ChuspaDB");
                    alert.setContentText("Versión: 0.1.0\nLicencia: MIT");
                    alert.showAndWait();
                } catch (Exception e) {
                    logger.error("Error al mostrar diálogo 'Acerca de' en VentanaPrincipalController.", e);
                    manejarErrorBaseDatos(e);
                }
            });
        }
    }
    public void ejecutarOperacionAsincrona(
            Runnable operacion,
            Runnable alCompletar,
            Consumer<String> alFallar) {
        if (operacion == null) {
            logger.warn("Se omitió ejecutarOperacionAsincrona porque la operación es nula.");
            return;
        }

        CompletableFuture.runAsync(operacion, UI_BACKGROUND_EXECUTOR)
                .whenComplete((ok, ex) -> {
                    if (ex != null) {
                        Throwable causa = (ex.getCause() != null) ? ex.getCause() : ex;
                        String mensajeError = manejarErrorBaseDatos(causa);
                        if (alFallar != null) {
                            ejecutarEnHiloUI(() -> alFallar.accept(mensajeError));
                        }
                    } else if (alCompletar != null) {
                        ejecutarEnHiloUI(alCompletar);
                    }
                });
    }
    public String manejarErrorBaseDatos(Throwable error) {
        if (error == null) {
            logger.warn("manejarErrorBaseDatos recibió una excepción nula.");
            return "Error desconocido de base de datos.";
        }

        Throwable raiz = obtenerCausaRaiz(error);
        String titulo;
        String mensaje;

        if (error instanceof ErrorQuery || raiz instanceof ErrorQuery) {
            logger.error("ErrorQuery capturado en VentanaPrincipalController: {}", error.getMessage(), error);
            titulo = "Error en consulta SQL";
            mensaje = extraerMensaje(error, raiz);
        } else if (error instanceof ErrorConexion || raiz instanceof ErrorConexion) {
            logger.error("ErrorConexion capturado en VentanaPrincipalController: {}", error.getMessage(), error);
            titulo = "Error de conexión a base de datos";
            mensaje = extraerMensaje(error, raiz);
        } else if (error instanceof ErrorPersistencia || raiz instanceof ErrorPersistencia) {
            logger.error("ErrorPersistencia capturado en VentanaPrincipalController: {}", error.getMessage(), error);
            titulo = "Error de persistencia";
            mensaje = extraerMensaje(error, raiz);
        } else if (raiz instanceof SQLException sqlEx) {
            logger.error("SQLException capturada (SQLState={}, ErrorCode={}): {}",
                    sqlEx.getSQLState(), sqlEx.getErrorCode(), sqlEx.getMessage(), sqlEx);
            titulo = "Error SQL [" + (sqlEx.getSQLState() != null ? sqlEx.getSQLState() : "N/A") + "]";
            mensaje = extraerMensaje(error, raiz);
        } else {
            logger.error("Error inesperado en VentanaPrincipalController.", error);
            titulo = "Error en operación";
            mensaje = extraerMensaje(error, raiz);
        }

        final String tituloFinal = titulo;
        final String mensajeFinal = mensaje;
        ejecutarEnHiloUI(() -> mostrarAlertaError(tituloFinal, mensajeFinal));
        return tituloFinal + ": " + mensajeFinal;
    }

    private Throwable obtenerCausaRaiz(Throwable error) {
        Throwable actual = error;
        while (actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }
        return actual;
    }

    private String extraerMensaje(Throwable original, Throwable raiz) {
        if (raiz != null && raiz.getMessage() != null && !raiz.getMessage().isBlank()) {
            return raiz.getMessage();
        }
        if (original != null && original.getMessage() != null && !original.getMessage().isBlank()) {
            return original.getMessage();
        }
        return (raiz != null) ? raiz.getClass().getSimpleName() : "Error inesperado.";
    }

    private void mostrarAlertaError(String encabezado, String detalle) {
        try {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error - ChuspaDB");
            alerta.setHeaderText(encabezado);
            alerta.setContentText(detalle);
            alerta.showAndWait();
        } catch (IllegalStateException e) {
            logger.debug("Toolkit JavaFX no inicializado; omitiendo diálogo modal: {} - {}", encabezado, detalle);
        }
    }

    private void ejecutarEnHiloUI(Runnable accion) {
        try {
            if (Platform.isFxApplicationThread()) {
                accion.run();
            } else {
                Platform.runLater(accion);
            }
        } catch (IllegalStateException e) {
            logger.debug("Toolkit JavaFX no inicializado; ejecutando actualización directa.");
            accion.run();
        }
    }

    public MenuItem getMenuNuevaConexion() {
        return menuNuevaConexion;
    }

    public MenuItem getMenuSalir() {
        return menuSalir;
    }

    public MenuItem getMenuAcercaDe() {
        return menuAcercaDe;
    }

    public StackPane getContenedorArbol() {
        return contenedorArbol;
    }

    public StackPane getContenedorEditor() {
        return contenedorEditor;
    }

    public StackPane getContenedorResultado() {
        return contenedorResultado;
    }

    public StackPane getContenedorBarraEstado() {
        return contenedorBarraEstado;
    }
}
