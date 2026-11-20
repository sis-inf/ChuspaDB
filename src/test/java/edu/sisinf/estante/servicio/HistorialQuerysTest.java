package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.modelo.EntradaHistorial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase HistorialQuerys.
 *
 * Verifica el comportamiento del límite de capacidad,
 * la recuperación del último elemento, el vaciado del historial
 * y el comportamiento con historial vacío.
 */
class HistorialQuerysTest {

    private HistorialQuerys historial;

    @BeforeEach
    void setUp() {
        historial = new HistorialQuerys();
    }

    @Test
    @DisplayName("Después de 51 entradas el historial contiene exactamente 50")
    void despuesDe51EntradasContiene50() {
        for (int i = 1; i <= 51; i++) {
            historial.agregar("SELECT " + i + " FROM tabla", "test", 0L);
        }

        assertEquals(50, historial.obtenerTodos().size());
    }

    @Test
    @DisplayName("obtenerUltimo retorna la última query agregada")
    void obtenerUltimoRetornaLaUltimaAgregada() {
        historial.agregar("SELECT * FROM usuarios", "test", 0L);
        historial.agregar("SELECT * FROM libros", "test", 0L);
        historial.agregar("DELETE FROM prestamos WHERE id = 1", "test", 0L);

        assertEquals("DELETE FROM prestamos WHERE id = 1", historial.obtenerUltimo().query());
    }

    @Test
    @DisplayName("limpiar vacía el historial por completo")
    void limpiarVaciaElHistorial() {
        historial.agregar("SELECT * FROM usuarios", "test", 0L);
        historial.agregar("INSERT INTO libros VALUES (1, 'titulo')", "test", 0L);

        historial.limpiar();

        assertTrue(historial.obtenerTodos().isEmpty());
    }

    @Test
    @DisplayName("obtenerUltimo con historial vacío retorna null")
    void obtenerUltimoConHistorialVacioRetornaNull() {
        assertNull(historial.obtenerUltimo());
    }

    @Test
    @DisplayName("agregar con query nula lanza IllegalArgumentException")
    void agregarConQueryNulaLanzaExcepcion() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> historial.agregar(null, "test", 0L));

        assertEquals("La sentencia SQL (query) no puede ser nula ni estar vacía.", error.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> historial.agregar(null, "test", 0L, false));
    }

    @Test
    @DisplayName("agregar con query en blanco lanza IllegalArgumentException")
    void agregarConQueryEnBlancoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> historial.agregar("   ", "test", 0L));
        assertThrows(IllegalArgumentException.class,
                () -> historial.agregar("", "test", 0L));
        assertTrue(historial.obtenerTodos().isEmpty());
    }

    @Test
    @DisplayName("agregar con base de datos nula lanza IllegalArgumentException")
    void agregarConBaseDatosNulaLanzaExcepcion() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> historial.agregar("SELECT 1", null, 0L));

        assertEquals("El nombre de la base de datos no puede ser nulo ni estar vacío.", error.getMessage());
        assertTrue(historial.obtenerTodos().isEmpty());
    }

    @Test
    @DisplayName("agregar con base de datos en blanco lanza IllegalArgumentException")
    void agregarConBaseDatosEnBlancoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> historial.agregar("SELECT 1", "   ", 0L));
        assertThrows(IllegalArgumentException.class,
                () -> historial.agregar("SELECT 1", "", 0L, true));
        assertTrue(historial.obtenerTodos().isEmpty());
    }

    @Test
    @DisplayName("agregar con duración negativa lanza excepción y no registra nada")
    void agregarConDuracionNegativaLanzaExcepcion() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> historial.agregar("SELECT 1", "test", -1L));

        assertEquals("La duración en milisegundos no puede ser negativa.", error.getMessage());
        assertTrue(historial.obtenerTodos().isEmpty());
        assertNull(historial.obtenerUltimo());
    }

    @Test
    @DisplayName("agregar con duración cero se acepta")
    void agregarConDuracionCeroSeAcepta() {
        historial.agregar("SELECT 1", "test", 0L);

        assertEquals(1, historial.obtenerTodos().size());
        assertEquals(0L, historial.obtenerUltimo().duracionMs());
    }

    @Test
    @DisplayName("agregar sin indicador marca la entrada como exitosa")
    void agregarSinIndicadorMarcaExitosa() {
        historial.agregar("SELECT 1", "test", 5L);

        assertTrue(historial.obtenerUltimo().exitosa());
    }

    @Test
    @DisplayName("agregar con exitosa en false registra la entrada como fallida")
    void agregarConExitosaFalsaRegistraFallo() {
        historial.agregar("SELECT 1", "test", 5L, false);

        assertFalse(historial.obtenerUltimo().exitosa());
    }

    @Test
    @DisplayName("agregar recorta los espacios de la query y la base de datos")
    void agregarRecortaEspacios() {
        historial.agregar("  SELECT 1  ", "  prod  ", 5L);

        EntradaHistorial ultima = historial.obtenerUltimo();
        assertEquals("SELECT 1", ultima.query());
        assertEquals("prod", ultima.baseDatos());
        assertEquals(5L, ultima.duracionMs());
    }

    @Test
    @DisplayName("agregar registra un timestamp dentro del momento de la llamada")
    void agregarRegistraTimestampActual() {
        long antes = System.currentTimeMillis();
        historial.agregar("SELECT 1", "test", 0L);
        long despues = System.currentTimeMillis();

        long timestamp = historial.obtenerUltimo().timestamp();
        assertTrue(timestamp >= antes && timestamp <= despues);
    }

    @Test
    @DisplayName("Con exactamente 50 entradas no se descarta ninguna")
    void conExactamente50EntradasNoSeDescartaNinguna() {
        for (int i = 1; i <= 50; i++) {
            historial.agregar("SELECT " + i + " FROM tabla", "test", 0L);
        }

        List<EntradaHistorial> todas = historial.obtenerTodos();
        assertEquals(50, todas.size());
        assertEquals("SELECT 1 FROM tabla", todas.get(0).query());
    }

    @Test
    @DisplayName("Con 51 entradas se descarta la más antigua")
    void con51EntradasSeDescartaLaMasAntigua() {
        for (int i = 1; i <= 51; i++) {
            historial.agregar("SELECT " + i + " FROM tabla", "test", 0L);
        }

        List<EntradaHistorial> todas = historial.obtenerTodos();
        assertEquals("SELECT 2 FROM tabla", todas.get(0).query());
        assertEquals("SELECT 51 FROM tabla", todas.get(49).query());
        assertEquals("SELECT 51 FROM tabla", historial.obtenerUltimo().query());
    }

    @Test
    @DisplayName("Con 100 entradas se conservan solo las últimas 50")
    void con100EntradasSeConservanLasUltimas50() {
        for (int i = 1; i <= 100; i++) {
            historial.agregar("SELECT " + i + " FROM tabla", "test", 0L);
        }

        List<EntradaHistorial> todas = historial.obtenerTodos();
        assertEquals(50, todas.size());
        assertEquals("SELECT 51 FROM tabla", todas.get(0).query());
        assertEquals("SELECT 100 FROM tabla", todas.get(49).query());
    }

    @Test
    @DisplayName("obtenerTodos con historial vacío retorna una lista vacía no nula")
    void obtenerTodosConHistorialVacioRetornaListaVacia() {
        List<EntradaHistorial> todas = historial.obtenerTodos();

        assertNotNull(todas);
        assertTrue(todas.isEmpty());
    }

    @Test
    @DisplayName("obtenerTodos retorna las entradas en orden cronológico")
    void obtenerTodosRetornaEnOrdenCronologico() {
        historial.agregar("SELECT 1", "test", 0L);
        historial.agregar("SELECT 2", "test", 0L);
        historial.agregar("SELECT 3", "test", 0L);

        List<EntradaHistorial> todas = historial.obtenerTodos();
        assertEquals(3, todas.size());
        assertEquals("SELECT 1", todas.get(0).query());
        assertEquals("SELECT 2", todas.get(1).query());
        assertEquals("SELECT 3", todas.get(2).query());
    }

    @Test
    @DisplayName("obtenerTodos retorna una copia: modificarla no afecta al historial")
    void obtenerTodosRetornaUnaCopia() {
        historial.agregar("SELECT 1", "test", 0L);

        List<EntradaHistorial> copia = historial.obtenerTodos();
        copia.clear();

        assertEquals(1, historial.obtenerTodos().size());
    }

    @Test
    @DisplayName("obtenerUltimo no elimina la entrada del historial")
    void obtenerUltimoNoEliminaLaEntrada() {
        historial.agregar("SELECT 1", "test", 0L);

        EntradaHistorial primera = historial.obtenerUltimo();
        EntradaHistorial segunda = historial.obtenerUltimo();

        assertEquals(primera, segunda);
        assertEquals(1, historial.obtenerTodos().size());
    }

    @Test
    @DisplayName("limpiar con historial vacío no lanza excepción")
    void limpiarConHistorialVacioNoLanzaExcepcion() {
        assertDoesNotThrow(() -> historial.limpiar());

        assertTrue(historial.obtenerTodos().isEmpty());
    }

    @Test
    @DisplayName("Después de limpiar se pueden agregar nuevas entradas")
    void despuesDeLimpiarSePuedeAgregarDeNuevo() {
        historial.agregar("SELECT 1", "test", 0L);
        historial.limpiar();

        assertNull(historial.obtenerUltimo());

        historial.agregar("SELECT 2", "test", 0L);

        assertEquals(1, historial.obtenerTodos().size());
        assertEquals("SELECT 2", historial.obtenerUltimo().query());
    }

    @Test
    @DisplayName("obtenerPorBaseDatos con nombre nulo lanza IllegalArgumentException")
    void obtenerPorBaseDatosConNombreNuloLanzaExcepcion() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> historial.obtenerPorBaseDatos(null));

        assertEquals("El nombre de la base de datos a filtrar no puede ser nulo ni estar vacío.",
                error.getMessage());
    }

    @Test
    @DisplayName("obtenerPorBaseDatos con nombre en blanco lanza IllegalArgumentException")
    void obtenerPorBaseDatosConNombreEnBlancoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> historial.obtenerPorBaseDatos("   "));
        assertThrows(IllegalArgumentException.class,
                () -> historial.obtenerPorBaseDatos(""));
    }

    @Test
    @DisplayName("obtenerPorBaseDatos ignora mayúsculas y minúsculas")
    void obtenerPorBaseDatosIgnoraMayusculas() {
        historial.agregar("SELECT a", "Produccion", 1L);
        historial.agregar("SELECT b", "desarrollo", 1L);
        historial.agregar("SELECT c", "PRODUCCION", 1L);

        List<EntradaHistorial> filtradas = historial.obtenerPorBaseDatos("produccion");

        assertEquals(2, filtradas.size());
        assertEquals("SELECT a", filtradas.get(0).query());
        assertEquals("SELECT c", filtradas.get(1).query());
    }

    @Test
    @DisplayName("obtenerPorBaseDatos recorta los espacios del nombre buscado")
    void obtenerPorBaseDatosRecortaEspacios() {
        historial.agregar("SELECT a", "Produccion", 1L);
        historial.agregar("SELECT b", "desarrollo", 1L);

        List<EntradaHistorial> filtradas = historial.obtenerPorBaseDatos("  desarrollo  ");

        assertEquals(1, filtradas.size());
        assertEquals("SELECT b", filtradas.get(0).query());
    }

    @Test
    @DisplayName("obtenerPorBaseDatos sin coincidencias retorna lista vacía")
    void obtenerPorBaseDatosSinCoincidenciasRetornaListaVacia() {
        assertTrue(historial.obtenerPorBaseDatos("inexistente").isEmpty());

        historial.agregar("SELECT a", "Produccion", 1L);

        assertTrue(historial.obtenerPorBaseDatos("inexistente").isEmpty());
    }

    @Test
    @DisplayName("obtenerSoloExitosas retorna solo las entradas exitosas")
    void obtenerSoloExitosasFiltraLasFallidas() {
        historial.agregar("SELECT 1", "test", 1L);
        historial.agregar("SELECT 2", "test", 1L, false);
        historial.agregar("SELECT 3", "test", 1L, true);

        List<EntradaHistorial> exitosas = historial.obtenerSoloExitosas();

        assertEquals(2, exitosas.size());
        assertEquals("SELECT 1", exitosas.get(0).query());
        assertEquals("SELECT 3", exitosas.get(1).query());
        assertTrue(exitosas.stream().allMatch(EntradaHistorial::exitosa));
    }

    @Test
    @DisplayName("obtenerSoloExitosas sin exitosas o sin entradas retorna lista vacía")
    void obtenerSoloExitosasSinExitosasRetornaListaVacia() {
        assertTrue(historial.obtenerSoloExitosas().isEmpty());

        historial.agregar("SELECT 1", "test", 1L, false);
        historial.agregar("SELECT 2", "test", 1L, false);

        assertTrue(historial.obtenerSoloExitosas().isEmpty());
    }
}
