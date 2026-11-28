package edu.sisinf.chuspadb.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.core.ErrorQuery;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de {@link BarraEstadoController}.
 *
 * <p>Los {@code Label} se inyectan por reflexión (los campos {@code @FXML} son privados). Se arranca
 * el toolkit de JavaFX una sola vez; como el controlador actualiza la UI con
 * {@code Platform.runLater}, las aserciones esperan a que el hilo de JavaFX procese lo encolado.
 */
@ExtendWith(MockitoExtension.class)
class BarraEstadoControllerTest {

    private static final String SIN_CONEXION = "Sin conexión";
    private static final String MENSAJE_PROGRESO_POR_DEFECTO =
            "Procesando operación de base de datos...";

    private BarraEstadoController controller;
    private Label labelConexion;
    private Label labelTiempo;
    private Label labelFilas;
    private Label labelMensaje;

    @Mock
    private Runnable operacion;

    @BeforeAll
    static void iniciarToolkitJavaFX() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
            listo.countDown();
        } catch (RuntimeException sinEntornoGrafico) {
            assumeTrue(false, "JavaFX no se pudo iniciar: " + sinEntornoGrafico);
        }
        assumeTrue(listo.await(10, TimeUnit.SECONDS), "El toolkit de JavaFX no arrancó a tiempo");
    }

    /** Espera a que el hilo de JavaFX procese lo encolado con runLater y devuelve el texto. */
    private static String texto(Label label) {
        CountDownLatch procesado = new CountDownLatch(1);
        Platform.runLater(procesado::countDown);
        try {
            if (!procesado.await(5, TimeUnit.SECONDS)) {
                fail("El hilo de JavaFX no procesó las actualizaciones a tiempo.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido mientras se esperaba al hilo de JavaFX.");
        }
        return label.getText();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new BarraEstadoController();
        labelConexion = new Label(SIN_CONEXION);
        labelTiempo = new Label("");
        labelFilas = new Label("");
        labelMensaje = new Label("");
        inyectar(controller, "labelConexion", labelConexion);
        inyectar(controller, "labelTiempo", labelTiempo);
        inyectar(controller, "labelFilas", labelFilas);
        inyectar(controller, "labelMensaje", labelMensaje);
    }

    private static void inyectar(Object destino, String campo, Object valor) throws Exception {
        Field field = BarraEstadoController.class.getDeclaredField(campo);
        field.setAccessible(true);
        field.set(destino, valor);
    }

    // ── setConexion ─────────────────────────────────────────────

    @Nested
    @DisplayName("setConexion")
    class SetConexion {

        @Test
        void muestraElNombreDeLaConexion() {
            controller.setConexion("PostgreSQL local");

            assertEquals("PostgreSQL local", texto(labelConexion));
        }

        @Test
        void recortaEspaciosAlInicioYAlFinal() {
            controller.setConexion("   SQLite  prueba  ");

            assertEquals("SQLite  prueba", texto(labelConexion));
        }

        @ParameterizedTest(name = "[{index}] texto = \"{0}\"")
        @ValueSource(strings = {"", " ", "     ", "\t", "\n"})
        void textoEnBlancoMuestraSinConexion(String texto) {
            controller.setConexion("MySQL");

            controller.setConexion(texto);

            assertEquals(SIN_CONEXION, texto(labelConexion));
        }

        @Test
        void textoNuloMuestraSinConexion() {
            controller.setConexion("MySQL");

            controller.setConexion(null);

            assertEquals(SIN_CONEXION, texto(labelConexion));
        }

        @Test
        void noAfectaLosDemasLabels() {
            controller.setConexion("MySQL");

            assertEquals("", texto(labelTiempo));
            assertEquals("", texto(labelFilas));
            assertEquals("", texto(labelMensaje));
        }

        @Test
        void sinLabelInicializadoNoLanzaExcepcion() {
            BarraEstadoController sinLabels = new BarraEstadoController();

            assertDoesNotThrow(() -> sinLabels.setConexion("MySQL"));
        }
    }

    // ── setTiempo / setFilas ────────────────────────────────────

    @Nested
    @DisplayName("setTiempo y setFilas")
    class TiempoYFilas {

        @ParameterizedTest(name = "[{index}] {0} ms -> \"{1}\"")
        @CsvSource({
            "0, '0 ms'",
            "1, '1 ms'",
            "150, '150 ms'",
            "9223372036854775807, '9223372036854775807 ms'"
        })
        void setTiempoUsaElFormatoXMs(long ms, String esperado) {
            controller.setTiempo(ms);

            assertEquals(esperado, texto(labelTiempo));
        }

        @ParameterizedTest(name = "[{index}] {0} ms se normaliza a 0 ms")
        @ValueSource(longs = {-1, -500, Long.MIN_VALUE})
        void setTiempoNegativoSeNormalizaACero(long ms) {
            controller.setTiempo(ms);

            assertEquals("0 ms", texto(labelTiempo));
        }

        @ParameterizedTest(name = "[{index}] {0} filas -> \"{1}\"")
        @CsvSource({
            "0, '0 filas'",
            "1, '1 filas'",
            "42, '42 filas'",
            "2147483647, '2147483647 filas'"
        })
        void setFilasUsaElFormatoXFilas(int filas, String esperado) {
            controller.setFilas(filas);

            assertEquals(esperado, texto(labelFilas));
        }

        @ParameterizedTest(name = "[{index}] {0} filas se normaliza a 0 filas")
        @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
        void setFilasNegativoSeNormalizaACero(int filas) {
            controller.setFilas(filas);

            assertEquals("0 filas", texto(labelFilas));
        }

        @Test
        void sinLabelsInicializadosNoLanzanExcepcion() {
            BarraEstadoController sinLabels = new BarraEstadoController();

            assertDoesNotThrow(() -> sinLabels.setTiempo(10));
            assertDoesNotThrow(() -> sinLabels.setFilas(10));
        }
    }

    // ── actualizarEstadoQuery ───────────────────────────────────

    @Nested
    @DisplayName("actualizarEstadoQuery")
    class ActualizarEstadoQuery {

        @Test
        void actualizaFilasYTiempoConSusFormatos() {
            controller.actualizarEstadoQuery(25, 340);

            assertEquals("Filas: 25", texto(labelFilas));
            assertEquals("Tiempo: 340 ms", texto(labelTiempo));
        }

        @Test
        void aceptaCeroFilasYCeroMilisegundos() {
            controller.actualizarEstadoQuery(0, 0);

            assertEquals("Filas: 0", texto(labelFilas));
            assertEquals("Tiempo: 0 ms", texto(labelTiempo));
        }

        @ParameterizedTest(name = "[{index}] filas={0}, tiempo={1} -> {2} / {3}")
        @CsvSource({
            "-1, 100, 'Filas: 0', 'Tiempo: 100 ms'",
            "10, -1, 'Filas: 10', 'Tiempo: 0 ms'",
            "-5, -5, 'Filas: 0', 'Tiempo: 0 ms'"
        })
        void valoresNegativosSeNormalizanACero(
                int filas, long tiempoMs, String textoFilas, String textoTiempo) {
            controller.actualizarEstadoQuery(filas, tiempoMs);

            assertEquals(textoFilas, texto(labelFilas));
            assertEquals(textoTiempo, texto(labelTiempo));
        }

        @Test
        void noModificaConexionNiMensaje() {
            controller.setConexion("MySQL");
            controller.setMensaje("Listo");

            controller.actualizarEstadoQuery(3, 12);

            assertEquals("MySQL", texto(labelConexion));
            assertEquals("Listo", texto(labelMensaje));
        }

        @Test
        void conUnSoloLabelInicializadoActualizaElQueExiste() throws Exception {
            BarraEstadoController parcial = new BarraEstadoController();
            Label soloFilas = new Label("");
            inyectar(parcial, "labelFilas", soloFilas);

            assertDoesNotThrow(() -> parcial.actualizarEstadoQuery(7, 9));

            assertEquals("Filas: 7", texto(soloFilas));
        }
    }

    // ── setMensaje ──────────────────────────────────────────────

    @Nested
    @DisplayName("setMensaje")
    class SetMensaje {

        @Test
        void muestraElMensajeTalCual() {
            controller.setMensaje("Consulta ejecutada correctamente");

            assertEquals("Consulta ejecutada correctamente", texto(labelMensaje));
        }

        @Test
        void mensajeNuloMuestraTextoVacio() {
            controller.setMensaje("algo");

            controller.setMensaje(null);

            assertEquals("", texto(labelMensaje));
        }

        @Test
        void mensajeVacioLimpiaElLabel() {
            controller.setMensaje("algo");

            controller.setMensaje("");

            assertEquals("", texto(labelMensaje));
        }

        @ParameterizedTest(name = "[{index}] \"{0}\"")
        @ValueSource(strings = {
            "No se pudo conectar al servidor",
            "Timeout al abrir la conexion",
            "Driver JDBC no encontrado",
            "Error de sintaxis SQL cerca de FROM",
            "La consulta devolvió un error",
            "Todo en orden"
        })
        void cualquierTextoSeMuestraSinLanzarExcepcion(String mensaje) {
            assertDoesNotThrow(() -> controller.setMensaje(mensaje));

            assertEquals(mensaje, texto(labelMensaje));
        }

        @Test
        void conservaMensajesLargosYCaracteresEspeciales() {
            String largo = "ñ-áéíóú-¿?-" + "x".repeat(5_000);

            controller.setMensaje(largo);

            assertEquals(largo, texto(labelMensaje));
        }

        @Test
        void sinLabelInicializadoNoLanzaExcepcion() {
            BarraEstadoController sinLabels = new BarraEstadoController();

            assertDoesNotThrow(() -> sinLabels.setMensaje("hola"));
        }
    }

    // ── mostrarError ────────────────────────────────────────────

    @Nested
    @DisplayName("mostrarError")
    class MostrarError {

        @Test
        void errorNuloMuestraMensajeDesconocido() {
            controller.mostrarError(null);

            assertEquals(
                    "Error desconocido en la operación de base de datos.", texto(labelMensaje));
        }

        @Test
        void errorQueryMuestraPrefijoDeConsultaSql() {
            controller.mostrarError(new ErrorQuery("tabla inexistente"));

            assertEquals("Error en consulta SQL: tabla inexistente", texto(labelMensaje));
        }

        @Test
        void errorConexionMuestraPrefijoDeConexion() {
            controller.mostrarError(new ErrorConexion("servidor no responde"));

            assertEquals("Error de conexión: servidor no responde", texto(labelMensaje));
        }

        @Test
        void errorQueryEnvueltoEnOtraExcepcionSeDetectaPorCausaRaiz() {
            Throwable envoltorio =
                    new RuntimeException("envoltorio", new ErrorQuery("columna inválida"));

            controller.mostrarError(envoltorio);

            assertEquals("Error en consulta SQL: envoltorio", texto(labelMensaje));
        }

        @Test
        void errorConexionEnvueltoEnOtraExcepcionSeDetectaPorCausaRaiz() {
            Throwable envoltorio =
                    new IllegalStateException("fallo al abrir", new ErrorConexion("sin red"));

            controller.mostrarError(envoltorio);

            assertEquals("Error de conexión: fallo al abrir", texto(labelMensaje));
        }

        @Test
        void sqlExceptionMuestraSqlStateYMensaje() {
            controller.mostrarError(new SQLException("no such table: libros", "42S02"));

            assertEquals("Error SQL [42S02]: no such table: libros", texto(labelMensaje));
        }

        @Test
        void sqlExceptionSinSqlStateMuestraNa() {
            controller.mostrarError(new SQLException("fallo genérico"));

            assertEquals("Error SQL [N/A]: fallo genérico", texto(labelMensaje));
        }

        @Test
        void sqlExceptionComoCausaRaizDeOtraExcepcion() {
            Throwable error =
                    new RuntimeException("envoltorio", new SQLException("deadlock", "40001"));

            controller.mostrarError(error);

            assertEquals("Error SQL [40001]: deadlock", texto(labelMensaje));
        }

        @Test
        void excepcionInesperadaMuestraPrefijoGenerico() {
            controller.mostrarError(new IllegalArgumentException("parámetro inválido"));

            assertEquals("Error: parámetro inválido", texto(labelMensaje));
        }

        @Test
        void excepcionSinMensajeUsaElMensajeDeLaCausaRaiz() {
            Throwable error =
                    new RuntimeException((String) null, new IllegalStateException("mensaje de la causa"));

            controller.mostrarError(error);

            assertEquals("Error: mensaje de la causa", texto(labelMensaje));
        }

        @Test
        void excepcionSinMensajeNiCausaUsaElNombreDeLaClase() {
            controller.mostrarError(new IllegalArgumentException());

            assertEquals("Error: IllegalArgumentException", texto(labelMensaje));
        }

        @Test
        void sinLabelMensajeInicializadoNoLanzaExcepcion() {
            BarraEstadoController sinLabels = new BarraEstadoController();

            assertDoesNotThrow(() -> sinLabels.mostrarError(new ErrorConexion("caída")));
        }

        @Test
        void unNuevoErrorReemplazaAlAnterior() {
            controller.mostrarError(new ErrorQuery("primero"));
            controller.mostrarError(new ErrorConexion("segundo"));

            assertEquals("Error de conexión: segundo", texto(labelMensaje));
        }
    }

    // ── limpiar ─────────────────────────────────────────────────

    @Nested
    @DisplayName("limpiar")
    class Limpiar {

        @Test
        void vaciaTiempoFilasYMensajeSinTocarLaConexion() {
            controller.setConexion("PostgreSQL local");
            controller.actualizarEstadoQuery(5, 20);
            controller.setMensaje("Listo");

            controller.limpiar();

            assertEquals("", texto(labelTiempo));
            assertEquals("", texto(labelFilas));
            assertEquals("", texto(labelMensaje));
            assertEquals("PostgreSQL local", texto(labelConexion));
        }

        @Test
        void esIdempotente() {
            controller.limpiar();
            controller.limpiar();

            assertEquals("", texto(labelTiempo));
            assertEquals("", texto(labelFilas));
            assertEquals("", texto(labelMensaje));
        }

        @Test
        void sinLabelsInicializadosNoLanzaExcepcion() {
            BarraEstadoController sinLabels = new BarraEstadoController();

            assertDoesNotThrow(sinLabels::limpiar);
        }
    }

    // ── ejecutarSinBloquearUI (asincronía y fallas) ─────────────

    @Nested
    @DisplayName("ejecutarSinBloquearUI")
    class EjecutarSinBloquearUI {

        private static final long TIMEOUT_SEGUNDOS = 5;

        @Test
        void operacionNulaLanzaNullPointerException() {
            NullPointerException ex =
                    assertThrows(
                            NullPointerException.class,
                            () -> controller.ejecutarSinBloquearUI("Cargando", null));

            assertEquals("La operación a ejecutar no puede ser nula.", ex.getMessage());
        }

        @Test
        void ejecutaLaOperacionYCompletaElFuture() throws Exception {
            CompletableFuture<Void> futuro = controller.ejecutarSinBloquearUI("Cargando", operacion);

            futuro.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

            verify(operacion).run();
            assertTrue(futuro.isDone());
            assertFalse(futuro.isCompletedExceptionally());
        }

        @Test
        void muestraElMensajeDeProgresoIndicado() throws Exception {
            controller
                    .ejecutarSinBloquearUI("Cargando tablas...", operacion)
                    .get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

            assertEquals("Cargando tablas...", texto(labelMensaje));
        }

        @ParameterizedTest(name = "[{index}] mensaje = \"{0}\"")
        @ValueSource(strings = {"", "   "})
        void mensajeEnBlancoUsaElMensajeDeProgresoPorDefecto(String mensaje) throws Exception {
            controller
                    .ejecutarSinBloquearUI(mensaje, operacion)
                    .get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

            assertEquals(MENSAJE_PROGRESO_POR_DEFECTO, texto(labelMensaje));
        }

        @Test
        void mensajeNuloUsaElMensajeDeProgresoPorDefecto() throws Exception {
            controller
                    .ejecutarSinBloquearUI(null, operacion)
                    .get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

            assertEquals(MENSAJE_PROGRESO_POR_DEFECTO, texto(labelMensaje));
        }

        @Test
        void laOperacionSeEjecutaEnUnHiloDistintoAlDelLlamador() throws Exception {
            AtomicReference<String> hilo = new AtomicReference<>();

            controller
                    .ejecutarSinBloquearUI(
                            "Hilo", () -> hilo.set(Thread.currentThread().getName()))
                    .get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

            assertEquals("chuspadb-barra-estado-worker", hilo.get());
        }

        @Test
        void unaFallaDeConexionSeMuestraYSePropagaEnElFuture() {
            ErrorConexion falla = new ErrorConexion("conexión perdida");
            doThrow(falla).when(operacion).run();

            CompletableFuture<Void> futuro = controller.ejecutarSinBloquearUI("Conectando", operacion);

            ExecutionException ex =
                    assertThrows(
                            ExecutionException.class,
                            () -> futuro.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS));
            assertSame(falla, ex.getCause());
            assertTrue(futuro.isCompletedExceptionally());
            assertEquals("Error de conexión: conexión perdida", texto(labelMensaje));
        }

        @Test
        void unaFallaDeConsultaSeMuestraYSePropagaEnElFuture() {
            ErrorQuery falla = new ErrorQuery("sintaxis inválida");
            doThrow(falla).when(operacion).run();

            CompletableFuture<Void> futuro = controller.ejecutarSinBloquearUI("Ejecutando", operacion);

            ExecutionException ex =
                    assertThrows(
                            ExecutionException.class,
                            () -> futuro.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS));
            assertSame(falla, ex.getCause());
            assertEquals("Error en consulta SQL: sintaxis inválida", texto(labelMensaje));
        }

        @Test
        void unaExcepcionInesperadaSeMuestraConPrefijoGenerico() {
            doThrow(new IllegalStateException("estado inválido")).when(operacion).run();

            CompletableFuture<Void> futuro = controller.ejecutarSinBloquearUI("Trabajando", operacion);

            assertThrows(
                    ExecutionException.class, () -> futuro.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS));
            assertEquals("Error: estado inválido", texto(labelMensaje));
        }

        @Test
        void despuesDeUnaFallaElControladorSigueAceptandoOperaciones() throws Exception {
            doThrow(new ErrorConexion("caída")).when(operacion).run();
            CompletableFuture<Void> fallida = controller.ejecutarSinBloquearUI("Falla", operacion);
            assertThrows(
                    ExecutionException.class, () -> fallida.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS));

            AtomicReference<Boolean> ejecutada = new AtomicReference<>(false);
            controller
                    .ejecutarSinBloquearUI("Reintento", () -> ejecutada.set(true))
                    .get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

            assertEquals(Boolean.TRUE, ejecutada.get());
        }
    }
}
