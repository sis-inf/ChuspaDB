package edu.sisinf.estante;

import edu.sisinf.estante.config.ConfiguracionApp;
import edu.sisinf.estante.controller.*;
import edu.sisinf.estante.core.ErrorConexion;
import edu.sisinf.estante.dao.*;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.Esquema;
import edu.sisinf.estante.modelo.ResultadoQuery;
import edu.sisinf.estante.modelo.TipoMotor;
import edu.sisinf.estante.servicio.EjecutorQuery;
import edu.sisinf.estante.servicio.ExploradorEsquemas;
import edu.sisinf.estante.servicio.ExportadorCSV;
import edu.sisinf.estante.util.SqlValidator;

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
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private Connection conexionActiva;

    private PanelArbolConexionesController arbolController;
    private PanelEditorSQLController editorController;
    private PanelResultadoQueryController resultadoController;
    private BarraEstadoController barraEstadoController;

    private IRepositorioConexiones repositorio;
    private EjecutorQuery ejecutorQuery;
    private ExploradorEsquemas exploradorEsquemas;
    private ExportadorCSV exportadorCSV;

    private Map<TipoMotor, IConexionDAO> daos;

    @Override
    public void start(Stage stage) throws Exception {

        ConfiguracionApp.asegurarDirectorioHome();

        daos = new HashMap<>();

        daos.put(
                TipoMotor.SQLITE,
                new ConexionDAOSQLite()
        );

        daos.put(
                TipoMotor.MYSQL,
                new ConexionDAOMySQL()
            daos.put(
        TipoMotor.POSTGRESQL,
        new ConexionDAOPostgreSQL()
    );
        );

        repositorio = new RepositorioConexionesJSON(
                ConfiguracionApp.archivoConexiones()
        );

        ejecutorQuery = new EjecutorQuery();
        exploradorEsquemas = new ExploradorEsquemas();
        exportadorCSV = new ExportadorCSV();

        FXMLLoader principalLoader = new FXMLLoader(
                getClass().getResource(
                        "/fxml/VentanaPrincipal.fxml"
                )
        );

        Parent root = principalLoader.load();

        VentanaPrincipalController principalController =
                principalLoader.getController();

        cargarPanelArbol(principalController);

        cargarPanelEditor(principalController);

        cargarPanelResultado(principalController);

        cargarBarraEstado(principalController);

        conectarHandlers(stage, principalController);

        arbolController.cargarConexiones(
                repositorio.listar()
        );

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/estante.css")
                        .toExternalForm()
        );

        stage.setTitle("Estante");

        stage.setScene(scene);

        stage.show();
    }

    private void cargarPanelArbol(
            VentanaPrincipalController principal
    ) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/fxml/PanelArbolConexiones.fxml"
                )
        );

        Parent root = loader.load();

        arbolController = loader.getController();

        principal
                .getContenedorArbol()
                .getChildren()
                .add(root);
    }

    private void cargarPanelEditor(
            VentanaPrincipalController principal
    ) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/fxml/PanelEditorSQL.fxml"
                )
        );

        Parent root = loader.load();

        editorController = loader.getController();

        principal
                .getContenedorEditor()
                .getChildren()
                .add(root);
    }

    private void cargarPanelResultado(
            VentanaPrincipalController principal
    ) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/fxml/PanelResultadoQuery.fxml"
                )
        );

        Parent root = loader.load();

        resultadoController = loader.getController();

        principal
                .getContenedorResultado()
                .getChildren()
                .add(root);
    }

    private void cargarBarraEstado(
            VentanaPrincipalController principal
    ) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/fxml/BarraEstado.fxml"
                )
        );

        Parent root = loader.load();

        barraEstadoController = loader.getController();

        principal
                .getContenedorBarraEstado()
                .getChildren()
                .add(root);
    }

    private void conectarHandlers(
            Stage stage,
            VentanaPrincipalController principal
    ) {

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

        resultadoController
                .setOnExportar(this::exportarCSV);
    }

    private void refrescarConexiones() {

        arbolController.cargarConexiones(
                repositorio.listar()
        );
    }

    private void abrirDialogoNuevaConexion(Stage owner) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/DialogoNuevaConexion.fxml"
                    )
            );

            Parent root = loader.load();

            DialogoNuevaConexionController controller =
                    loader.getController();

            Stage dialog = new Stage();

            dialog.initOwner(owner);

            dialog.initModality(Modality.APPLICATION_MODAL);

            dialog.setScene(new Scene(root));
            controller.getBotonProbar().setOnAction(event -> {

    try {

        if (!controller.validarFormulario()) {
            return;
        }

        Conexion conexion =
                controller.construirConexion();

        IConexionDAO dao =
                daos.get(conexion.getTipoMotor());

        long inicio =
                System.currentTimeMillis();

        boolean ok =
                dao.probar(conexion);

        long tiempo =
                System.currentTimeMillis() - inicio;

        controller
                .getEtiquetaEstado()
                .setText(
                        (ok ? "✅ " : "❌ ")
                                + (ok ? "Conexión exitosa" : "Conexión fallida")
                                + " ("
                                + tiempo
                                + " ms)"
                );

    } catch (Exception e) {

        controller
                .getEtiquetaEstado()
                .setText(
                        "❌ " + obtenerMensajeError(e)
                );
    }
});

            controller.getBotonGuardar().setOnAction(event -> {

                try {

                    if (!controller.validarFormulario()) {
                        return;
                    }

                    Conexion conexion =
                            controller.construirConexion();

                    repositorio.guardar(conexion);

                    refrescarConexiones();

                    controller.setConexionResultado(conexion);

                    dialog.close();

                } catch (Exception e) {

                    controller
                            .getEtiquetaEstado()
                            .setText(obtenerMensajeError(e));
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

        try {

            if (conexionActiva != null) {
                conexionActiva.close();
            }

            IConexionDAO dao =
                    daos.get(conexion.getTipoMotor());

            conexionActiva = dao.abrir(conexion);

            List<Esquema> esquemas =
                    exploradorEsquemas
                            .listarEsquemas(conexionActiva);

            TreeItem<Object> nodo =
                    arbolController.getNodoSeleccionado();

            nodo.getChildren().clear();

            for (Esquema esquema : esquemas){
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

        try {

            if (conexionActiva == null) {

                mostrarError(
                        "Sin conexión",
                        "No existe conexión activa",
                        "Abra una conexión primero"
                );

                return;
            }

            if (SqlValidator.esDestructiva(sql)) {

                boolean confirmar =
                        DialogoConfirmacionDML.confirmar(sql);

                if (!confirmar) {
                    return;
                }
            }

            ResultadoQuery resultado =
                    ejecutorQuery.ejecutar(
                            conexionActiva,
                            sql
                    );

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

        } catch (Exception e) {

            mostrarError(
                    "Error SQL",
                    "No se pudo ejecutar la query",
                    obtenerMensajeError(e)
            );
        }
    }

    private void exportarCSV(ResultadoQuery resultado) {

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
                return;
            }

            exportadorCSV.exportar(
                    resultado,
                    archivo
            );

        } catch (Exception e) {

            mostrarError(
                    "Error exportando",
                    "No se pudo exportar CSV",
                    obtenerMensajeError(e)
            );
        }
    }

    private String obtenerMensajeError(Exception e) {

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
        launch(args);
    }
}
