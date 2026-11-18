package edu.sisinf.chuspadb;

import edu.sisinf.chuspadb.config.ConfiguracionApp;
import edu.sisinf.chuspadb.controller.*;
import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.dao.*;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.Esquema;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import edu.sisinf.chuspadb.modelo.TipoMotor;
import edu.sisinf.chuspadb.servicio.EjecutorQuery;
import edu.sisinf.chuspadb.servicio.EjecutorQueryAsync;
import edu.sisinf.chuspadb.servicio.ExploradorEsquemas;
import edu.sisinf.chuspadb.servicio.ExportadorCSV;
import edu.sisinf.chuspadb.util.SqlValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private Connection conexionActiva;
    private Conexion conexionInfoActiva;
    private IConexionDAO daoActivo;

    private PanelArbolConexionesController arbolController;
    private PanelEditorSQLController editorController;
    private PanelResultadoQueryController resultadoController;
    private BarraEstadoController barraEstadoController;

    private IRepositorioConexiones repositorio;
    private EjecutorQueryAsync ejecutorQueryAsync;
    private ExploradorEsquemas exploradorEsquemas;
    private ExportadorCSV exportadorCSV;

    private Map<TipoMotor, IConexionDAO> daos;

    @Override
    public void start(Stage stage) throws Exception {
        if (stage == null) {
            logger.error("El escenario principal (Stage) recibido en start() es nulo.");
            throw new IllegalArgumentException("El escenario principal (Stage) no puede ser nulo.");
        }

        logger.debug("Iniciando configuración y componentes principales de ChuspaDB.");
        ConfiguracionApp.asegurarDirectorioHome();

        daos = new HashMap<>();

        daos.put(
                TipoMotor.SQLITE,
                new ConexionDAOSQLite()
        );

        daos.put(
                TipoMotor.MYSQL,
                new ConexionDAOMySQL()
        );

        daos.put(
                TipoMotor.POSTGRESQL,
                new ConexionDAOPostgreSQL()
        );

        repositorio = new RepositorioConexionesJSON(
                ConfiguracionApp.archivoConexiones()
        );

        ejecutorQueryAsync = new EjecutorQueryAsync();
        exploradorEsquemas = new ExploradorEsquemas();
        exportadorCSV = new ExportadorCSV();

        URL fxmlPrincipal = Objects.requireNonNull(
                getClass().getResource("/fxml/VentanaPrincipal.fxml"),
                "No se encontró el recurso /fxml/VentanaPrincipal.fxml"
        );
        FXMLLoader principalLoader = new FXMLLoader(fxmlPrincipal);

        Parent root = principalLoader.load();

        VentanaPrincipalController principalController =
            Objects.requireNonNull(
                principalLoader.getController(),"No se pudo inicializar VentanaPrincipalController"
            );

        cargarPanelArbol(principalController);

        cargarPanelEditor(principalController);

        cargarPanelResultado(principalController);

        cargarBarraEstado(principalController);

        conectarHandlers(stage, principalController);

        arbolController.cargarConexiones(
                repositorio.listar()
        );

        Scene scene = new Scene(root);
        URL cssUrl = Objects.requireNonNull(
                getClass().getResource("/css/chuspadb.css"),
                "No se encontró la hoja de estilos /css/chuspadb.css"
        );
        scene.getStylesheets().add(cssUrl.toExternalForm());
        
        stage.setTitle("ChuspaDB");

        stage.setScene(scene);

        stage.show();
        logger.debug("Interfaz principal de ChuspaDB iniciada correctamente.");
    }

    private void cargarPanelArbol(
            VentanaPrincipalController principal
    ) throws Exception {
        Objects.requireNonNull(principal, "VentanaPrincipalController no puede ser nulo.");
        URL recurso = Objects.requireNonNull(
                getClass().getResource("/fxml/PanelArbolConexiones.fxml"),
                "No se encontró /fxml/PanelArbolConexiones.fxml"
        );
        
        FXMLLoader loader = new FXMLLoader(recurso);

        Parent root = loader.load();

        arbolController = loader.getController();

        principal
                .getContenedorArbol()
                .getChildren()
                .add(root);
        logger.debug("Panel de árbol de conexiones cargado.");
    }

    private void cargarPanelEditor(
            VentanaPrincipalController principal
    ) throws Exception {
        Objects.requireNonNull(principal, "VentanaPrincipalController no puede ser nulo.");
        URL recurso = Objects.requireNonNull(
                getClass().getResource("/fxml/PanelEditorSQL.fxml"),
                "No se encontró /fxml/PanelEditorSQL.fxml"
        );
        
        FXMLLoader loader = new FXMLLoader(recurso);

        Parent root = loader.load();

        editorController = loader.getController();

        principal
                .getContenedorEditor()
                .getChildren()
                .add(root);
        logger.debug("Panel de editor SQL cargado.");
    }

    private void cargarPanelResultado(
            VentanaPrincipalController principal
    ) throws Exception {
        Objects.requireNonNull(principal, "VentanaPrincipalController no puede ser nulo.");
        URL recurso = Objects.requireNonNull(
                getClass().getResource("/fxml/PanelResultadoQuery.fxml"),
                "No se encontró /fxml/PanelResultadoQuery.fxml"
        );
        
        FXMLLoader loader = new FXMLLoader(recurso);

        Parent root = loader.load();

        resultadoController = loader.getController();

        principal
                .getContenedorResultado()
                .getChildren()
                .add(root);
        logger.debug("Panel de resultados de query cargado.");
    }

    private void cargarBarraEstado(
            VentanaPrincipalController principal
    ) throws Exception {
        Objects.requireNonNull(principal, "VentanaPrincipalController no puede ser nulo.");
        URL recurso = Objects.requireNonNull(
                getClass().getResource("/fxml/BarraEstado.fxml"),
                "No se encontró /fxml/BarraEstado.fxml"
        );

        FXMLLoader loader = new FXMLLoader(recurso);

        Parent root = loader.load();

        barraEstadoController = loader.getController();

        principal
                .getContenedorBarraEstado()
                .getChildren()
                .add(root);
        logger.debug("Barra de estado cargada.");
    }

    private void conectarHandlers(
            Stage stage,
            VentanaPrincipalController principal
    ) {
        Objects.requireNonNull(stage, "Stage no puede ser nulo al conectar handlers.");
        Objects.requireNonNull(principal, "VentanaPrincipalController no puede ser nulo al conectar handlers.");

        principal
                .getMenuNuevaConexion()
                .setOnAction(e -> abrirDialogoNuevaConexion(stage));

        arbolController
                .getBotonNuevaConexion()
                .setOnAction(e -> abrirDialogoNuevaConexion(stage));

        arbolController
                .getBotonRefrescar()
                .setOnAction(e -> refrescarConexiones());

        arbolController
                .setOnConexionDoubleClick(this::abrirConexion);

        editorController
                .setOnEjecutar(this::ejecutarSQL);

        editorController
                .setOnResultado(this::manejarResultadoQuery);

        editorController
                .setOnError(this::manejarErrorQuery);

        resultadoController
                .setOnExportar(this::exportarCSV);
        logger.debug("Manejadores de eventos conectados correctamente.");
    }

    private void refrescarConexiones() {
        logger.debug("Refrescando lista de conexiones guardadas.");
        arbolController.cargarConexiones(
                repositorio.listar()
        );
    }

    private void abrirDialogoNuevaConexion(Stage owner) {
        if (owner == null) {
            logger.warn("Se intentó abrir el diálogo de nueva conexión con Stage propietario nulo.");
            throw new IllegalArgumentException("La ventana propietaria (owner) no puede ser nula.");
        }

        URL recurso = Objects.requireNonNull(
                getClass().getResource("/fxml/DialogoNuevaConexion.fxml"),
                "No se encontró /fxml/DialogoNuevaConexion.fxml"
        );
        
        try {
            FXMLLoader loader = new FXMLLoader(recurso);

            Parent root = loader.load();

            DialogoNuevaConexionController controller =
                    loader.getController();

            Stage dialog = new Stage();

            dialog.initOwner(owner);

            dialog.initModality(Modality.APPLICATION_MODAL);

            dialog.setScene(new Scene(root));

            controller.setDaos(daos);

            controller.setOnGuardar(() -> {

                try {

                    if (!controller.validarFormulario()) {
                        return;
                    }

                    Conexion conexion =
                            controller.construirConexion();

                    repositorio.guardar(conexion);

                    refrescarConexiones();

                    controller.setConexionResultado(conexion);

                    logger.debug("Nueva conexión '{}' guardada correctamente.", conexion.getNombre());
                    dialog.close();

                } catch (Exception e) {

                    controller.mostrarEstado(obtenerMensajeError(e));
                }
            });
            
            dialog.showAndWait();

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudo abrir el diálogo",
                    obtenerMensajeError(e)
            );
        }
    }

    private void abrirConexion(Conexion conexion) {
        if (conexion == null) {
            logger.warn("Se intentó abrir una conexión nula.");
            throw new IllegalArgumentException("La conexión seleccionada no puede ser nula.");
        }

        try {

            if (conexionActiva != null && !conexionActiva.isClosed()) {
                logger.debug("Cerrando conexión activa previa antes de abrir '{}'.", conexion.getNombre());
                conexionActiva.close();
            }

            IConexionDAO dao =
                    daos.get(conexion.getTipoMotor());
            if (dao == null) {
                throw new ErrorConexion("No existe un DAO soportado para el motor: " + conexion.getTipoMotor());
            }

            logger.debug("Intentando abrir conexión '{}' con motor {}.", conexion.getNombre(), conexion.getTipoMotor());
            conexionActiva = dao.abrir(conexion);

            conexionInfoActiva = conexion;
            daoActivo = dao;

            List<Esquema> esquemas =
                    exploradorEsquemas
                            .listarEsquemas(conexionActiva);

            TreeItem<Object> nodo =
                    arbolController.getNodoSeleccionado();
            if (nodo == null) {
                throw new IllegalStateException("No hay un nodo seleccionado en el árbol de conexiones.");
            }

            nodo.getChildren().clear();

            for (Esquema esquema : esquemas) {
                nodo.getChildren().add(
                        new TreeItem<>(esquema.getNombre())
                );
            }

            nodo.setExpanded(true);

            editorController.setConexionActiva(
                    conexion.getNombre()
            );

            barraEstadoController.setConexion(
                    conexion.getNombre()
            );
            logger.debug("Conexión '{}' establecida con {} esquemas cargados.", conexion.getNombre(), esquemas.size());

        } catch (IllegalStateException e) {

            throw e;

        } catch (ErrorConexion e) {

            mostrarError(
                    "Error de conexión",
                    "No se pudo abrir la conexión",
                    obtenerMensajeError(e)
            );

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "Error inesperado",
                    obtenerMensajeError(e)
            );
        }
    }

    private void ejecutarSQL(String sql) {
        if (sql == null || sql.isBlank()) {
            logger.warn("Se intentó ejecutar una consulta SQL nula o vacía.");
            editorController.ocultarSpinner();
            mostrarError(
                    "Consulta inválida",
                    "La sentencia SQL está vacía",
                    "Ingrese una sentencia SQL válida antes de ejecutar."
            );
            return;
        }

        if (conexionActiva == null || conexionInfoActiva == null || daoActivo == null) {
            logger.warn("Intento de ejecutar SQL sin una conexión activa válida.");
            mostrarError(
                    "Sin conexión",
                    "No existe conexión activa",
                    "Abra una conexión primero"
            );

            editorController.ocultarSpinner();

            return;
        }

        if (SqlValidator.esDestructiva(sql)) {
            logger.debug("Consulta SQL destructiva detectada; solicitando confirmación.");
            boolean confirmar =
                    DialogoConfirmacionDML.confirmar(sql);

            if (!confirmar) {
                logger.debug("Ejecución de consulta destructiva cancelada por el usuario.");
                editorController.ocultarSpinner();
                return;
            }
        }
        
        logger.debug("Enviando consulta SQL a ejecución asíncrona.");
        ejecutorQueryAsync.ejecutar(
                sql,
                conexionInfoActiva,
                daoActivo,
                this::manejarResultadoQuery,
                this::manejarErrorQuery
        );
    }

    private void manejarResultadoQuery(ResultadoQuery resultado) {
        if (resultado == null) {
            logger.warn("Se recibió un ResultadoQuery nulo.");
            editorController.ocultarSpinner();
            throw new IllegalArgumentException("El resultado de la consulta no puede ser nulo.");
        }

        resultadoController.mostrar(resultado);

        barraEstadoController.setTiempo(
                resultado.getTiempoMs()
        );

        switch (resultado.getTipo()) {

            case LECTURA ->
                    barraEstadoController.setFilas(
                            resultado.getFilas().size()
                    );

            case ESCRITURA ->
                    barraEstadoController.setFilas(
                            resultado.getFilasAfectadas()
                    );

            case ERROR ->
                    barraEstadoController.setMensaje(
                            resultado.getMensaje()
                    );
        }

        editorController.ocultarSpinner();
        logger.debug("Resultado de query procesado (tipo: {}, tiempo: {} ms).",
                resultado.getTipo(), resultado.getTiempoMs());
    }

    private void manejarErrorQuery(Throwable error) {
        Throwable causa = (error != null)
                ? error
                : new RuntimeException("Error desconocido durante la ejecución de la consulta.");

        ResultadoQuery resultado =
                ResultadoQuery.deError(
                        obtenerMensajeError(causa),
                        0
                );

        manejarResultadoQuery(resultado);
    }

    private void exportarCSV(ResultadoQuery resultado) {
        if (resultado == null) {
            logger.warn("Se intentó exportar a CSV un ResultadoQuery nulo.");
            mostrarError(
                    "Error exportando",
                    "No hay resultados para exportar",
                    "Ejecute una consulta válida antes de exportar a CSV."
            );
            return;
        }

        try {

            FileChooser chooser =
                    new FileChooser();

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "CSV",
                            "*.csv"
                    )
            );

            File archivo =
                    chooser.showSaveDialog(null);

            if (archivo == null) {
                logger.debug("Exportación a CSV cancelada por el usuario.");
                return;
            }

            exportadorCSV.exportar(
                    resultado,
                    archivo
            );
            logger.debug("Resultados exportados correctamente a '{}'.", archivo.getAbsolutePath());

        } catch (Exception e) {

            mostrarError(
                    "Error exportando",
                    "No se pudo exportar CSV",
                    obtenerMensajeError(e)
            );
        }
    }

    private String obtenerMensajeError(Throwable e) {
        if (e == null) {
            logger.error("Error en la interfaz de usuario (excepción nula).");
            return "Ocurrió un error inesperado. Consulte los logs para más detalles.";
        }

        logger.error("Error en la interfaz de usuario", e);

        String mensaje = e.getMessage();
        return (mensaje == null || mensaje.isBlank())
                ? "Ocurrió un error inesperado. Consulte los logs para más detalles."
                : mensaje;
    }

    private void mostrarError(
            String titulo,
            String header,
            String mensaje
    ) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(titulo);

        alert.setHeaderText(header);

        alert.setContentText(mensaje);

        alert.showAndWait();
    }

    public static void main(String[] args) {
        if (args == null) {
            logger.error("El arreglo de argumentos (args) recibido en main() es nulo.");
            throw new IllegalArgumentException("Los argumentos de inicio (args) no pueden ser nulos.");
        }
        logger.debug("Iniciando aplicación ChuspaDB con {} argumentos.", args.length);
        launch(args);
    }
}
