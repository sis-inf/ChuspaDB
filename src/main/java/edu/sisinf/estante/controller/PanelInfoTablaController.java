package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.core.ErrorQuery;
import edu.sisinf.chuspadb.modelo.ColumnaInfo;
import edu.sisinf.chuspadb.servicio.ExploradorEsquemas;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.beans.property.SimpleStringProperty;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

/**
 * Controller del panel de información de tabla.
 *
 * Muestra las columnas de la tabla seleccionada en el árbol de conexiones
 * en un TableView con columnas Nombre, Tipo, Nullable y Default.
 */
public class PanelInfoTablaController {
    private static final Logger logger = LoggerFactory.getLogger(PanelInfoTablaController.class);
    private static final ExecutorService INFO_TABLA_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread hilo = new Thread(runnable, "chuspadb-info-tabla-worker");
        hilo.setDaemon(true);
        return hilo;
    });

    @FXML private Label labelNombreTabla;
    @FXML private TableView<ColumnaInfo> tablaColumnas;
    @FXML private TableColumn<ColumnaInfo, String> colNombre;
    @FXML private TableColumn<ColumnaInfo, String> colTipo;
    @FXML private TableColumn<ColumnaInfo, String> colNullable;
    @FXML private TableColumn<ColumnaInfo, String> colDefault;

    private final ExploradorEsquemas explorador = new ExploradorEsquemas();
    private final AtomicLong secuenciaCarga = new AtomicLong(0);
    /**
     * Inicializa el TableView con las columnas correspondientes.
     */
    @FXML
    public void initialize() {
        if (colNombre != null) {
            colNombre.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().nombre()));
        }
        if (colTipo != null) {
            colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().tipoSQL()));
        }
        if (colNullable != null) {
            colNullable.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nullable() ? "Sí" : "No"));
        }
        if (colDefault != null) {
            colDefault.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().valorDefault() != null
                    ? data.getValue().valorDefault() : ""));
        }
    }
    
    /**
     * Carga la información de columnas de la tabla indicada.
     *
     * @param tabla     nombre de la tabla
     * @param esquema   nombre del esquema
     * @param conexion  conexión JDBC activa
     */
    public void mostrarTabla(String tabla, String esquema, Connection conexion) {
        final long idSolicitud = secuenciaCarga.incrementAndGet();
        if (tabla == null || tabla.isBlank()) {
            logger.warn("mostrarTabla invocada con nombre de tabla nulo o vacío.");
            ejecutarEnHiloUI(() -> {
                if (labelNombreTabla != null) {
                    labelNombreTabla.setText("Error: Nombre de tabla inválido.");
                }
                if (tablaColumnas != null) {
                    tablaColumnas.getItems().clear();
                }
            });
            return;
        }

        if (conexion == null) {
            logger.warn("mostrarTabla invocada con conexión nula para la tabla '{}'.", tabla);
            ejecutarEnHiloUI(() -> {
                if (labelNombreTabla != null) {
                    labelNombreTabla.setText("Error de conexión BD: no hay una conexión activa para '" + tabla + "'.");
                }
                if (tablaColumnas != null) {
                    tablaColumnas.getItems().clear();
                }
            });
            return;
        }

        ejecutarEnHiloUI(() -> {
            if (labelNombreTabla != null) {
                labelNombreTabla.setText("Cargando tabla: " + tabla + "...");
            }
            if (tablaColumnas != null) {
                tablaColumnas.getItems().clear();
            }
        });

        CompletableFuture.supplyAsync(() -> {
            try {
                if (conexion.isClosed()) {
                    throw new ErrorConexion("La conexión a la base de datos se encuentra cerrada.");
                }
                List<ColumnaInfo> columnas = explorador.getColumnas(conexion, esquema, tabla);
                return columnas != null ? columnas : Collections.<ColumnaInfo>emptyList();
            } catch (ErrorConexion | ErrorQuery e) {
                throw new RuntimeException(e);
            } catch (SQLException sqlEx) {
                throw new RuntimeException(sqlEx);
            }
        }, INFO_TABLA_EXECUTOR).whenComplete((columnas, ex) -> {
            if (idSolicitud != secuenciaCarga.get()) {
                logger.debug("Descartando carga obsoleta de metadatos para la tabla '{}'.", tabla);
                return;
            }

            if (ex != null) {
                Throwable causa = extraerCausaReal(ex);
                String mensajeError = clasificarYRegistrarErrorSql(tabla, causa);
                ejecutarEnHiloUI(() -> {
                    if (labelNombreTabla != null) {
                        labelNombreTabla.setText(mensajeError);
                    }
                    if (tablaColumnas != null) {
                        tablaColumnas.getItems().clear();
                    }
                });
                return;
            }

            ejecutarEnHiloUI(() -> {
                if (labelNombreTabla != null) {
                    labelNombreTabla.setText("Tabla: " + tabla);
                }
                if (tablaColumnas != null) {
                    tablaColumnas.getItems().setAll(columnas);
                }
            });
        });
    }
    /**
     * Limpia el panel cuando no hay tabla seleccionada.
     */
    public void limpiar() {
        secuenciaCarga.incrementAndGet();
        ejecutarEnHiloUI(() -> {
            if (labelNombreTabla != null) {
                labelNombreTabla.setText("Selecciona una tabla");
            }
            if (tablaColumnas != null) {
                tablaColumnas.getItems().clear();
            }
        });
    }
    private String clasificarYRegistrarErrorSql(String tabla, Throwable error) {
        if (error instanceof ErrorQuery) {
            logger.error("ErrorQuery al consultar columnas de '{}': {}", tabla, error.getMessage(), error);
            return "Error en consulta SQL (" + tabla + "): " + obtenerMensaje(error);
        }
        if (error instanceof ErrorConexion) {
            logger.error("ErrorConexion al consultar columnas de '{}': {}", tabla, error.getMessage(), error);
            return "Error de conexión BD (" + tabla + "): " + obtenerMensaje(error);
        }
        if (error instanceof SQLException sqlEx) {
            String state = (sqlEx.getSQLState() != null && !sqlEx.getSQLState().isBlank())
                    ? sqlEx.getSQLState()
                    : "N/A";
            logger.error("SQLException al obtener columnas de '{}' [SQLState={}]: {}",
                    tabla, state, sqlEx.getMessage(), sqlEx);
            return "Error SQL [" + state + "] en '" + tabla + "': " + obtenerMensaje(sqlEx);
        }
        logger.error("Error inesperado al obtener columnas de '{}'", tabla, error);
        return "Error al cargar tabla '" + tabla + "': " + obtenerMensaje(error);
    }
    private Throwable extraerCausaReal(Throwable error) {
        Throwable actual = error;
        while (actual != null && actual.getCause() != null && actual.getCause() != actual
                && !(actual instanceof ErrorQuery)
                && !(actual instanceof ErrorConexion)
                && !(actual instanceof SQLException)) {
            actual = actual.getCause();
        }
        return actual != null ? actual : error;
    }

    private String obtenerMensaje(Throwable e) {
        if (e == null) {
            return "Error desconocido.";
        }
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        return e.getClass().getSimpleName();
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
}
