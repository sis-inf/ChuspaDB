package edu.sisinf.estante.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class FxmlCargaSmokeTest {

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        latch.await();
    }

    @ParameterizedTest(name = "Verificando carga de {0}")
    @ValueSource(strings = {
        "/fxml/BarraEstado.fxml",
        "/fxml/DialogoNuevaConexion.fxml",
        "/fxml/PanelArbolConexiones.fxml",
        "/fxml/PanelEditorSQL.fxml",
        "/fxml/PanelEstadisticas.fxml",
        "/fxml/PanelHistorial.fxml",
        "/fxml/PanelInfoTabla.fxml",
        "/fxml/PanelResultadoQuery.fxml",
        "/fxml/VentanaPrincipal.fxml"
    })
    void testCargaFxmlYController(String rutaFxml) {
        try (InputStream is = getClass().getResourceAsStream(rutaFxml)) {
            assertNotNull(is, "El archivo FXML no existe en la ruta: " + rutaFxml);

            FXMLLoader loader = new FXMLLoader();
            loader.load(is);

            Object controller = loader.getController();
            assertNotNull(controller, 
                "El FXML " + rutaFxml + " cargo pero NO tiene asignado un fx:controller en su XML");

        } catch (Exception e) {
            fail("Error al cargar el FXML " + rutaFxml + ": " + e.getMessage(), e);
        }
    }
}