package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración pequeñas de EjecutorQuery.
 * Usan SQLite en memoria para ser rápidas y deterministas.
 */
class EjecutorQueryTest {

    private Connection conexion;
    private EjecutorQuery ejecutor;

    @BeforeEach
    void setUp() throws SQLException {
        // Cada test obtiene una base en memoria nueva (conexión nueva = BD nueva en SQLite)
        conexion = DriverManager.getConnection("jdbc:sqlite::memory:");
        ejecutor = new EjecutorQuery(conexion);

        // Crear tabla con datos de prueba
        conexion.createStatement().executeUpdate(
            "CREATE TABLE usuarios (id INTEGER PRIMARY KEY, nombre TEXT, edad INTEGER)"
        );
        conexion.createStatement().executeUpdate("INSERT INTO usuarios VALUES (1, 'Ana', 30)");
        conexion.createStatement().executeUpdate("INSERT INTO usuarios VALUES (2, 'Luis', 25)");
        conexion.createStatement().executeUpdate("INSERT INTO usuarios VALUES (3, 'María', 35)");
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Cerrar la conexión elimina la base en memoria automáticamente
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }

    // -------------------------------------------------------------------------
    // SELECT
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_select_retornaTipoLectura() {
        // Un SELECT debe producir un ResultadoQuery de tipo LECTURA
        ResultadoQuery resultado = ejecutor.ejecutar("SELECT * FROM usuarios");
        assertEquals(ResultadoQuery.Tipo.LECTURA, resultado.getTipo());
    }

    @Test
    void ejecutar_select_contieneNombresDeColumnasCorrectos() {
        // El resultado de un SELECT debe incluir los nombres exactos de las columnas
        ResultadoQuery resultado = ejecutor.ejecutar("SELECT id, nombre, edad FROM usuarios");
        assertEquals(3, resultado.getColumnas().size());
        assertEquals("id",     resultado.getColumnas().get(0));
        assertEquals("nombre", resultado.getColumnas().get(1));
        assertEquals("edad",   resultado.getColumnas().get(2));
    }

    @Test
    void ejecutar_select_contieneNumeroCorrectoDeFilas() {
        // La tabla tiene 3 filas; el resultado debe reflejarlo
        ResultadoQuery resultado = ejecutor.ejecutar("SELECT * FROM usuarios");
        assertEquals(3, resultado.getFilas().size());
    }

    @Test
    void ejecutar_select_contieneValoresCorrectosEnCadaCelda() {
        // Los datos de la primera fila deben coincidir con lo insertado en setUp()
        ResultadoQuery resultado = ejecutor.ejecutar("SELECT id, nombre, edad FROM usuarios WHERE id = 1");
        assertEquals(1, resultado.getFilas().size());

        var fila = resultado.getFilas().get(0);
        assertEquals(1,     ((Number) fila.get(0)).intValue());
        assertEquals("Ana", fila.get(1));
        assertEquals(30,    ((Number) fila.get(2)).intValue());
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_insert_retornaTipoEscritura() {
        // Un INSERT debe producir un ResultadoQuery de tipo ESCRITURA
        ResultadoQuery resultado = ejecutor.ejecutar(
            "INSERT INTO usuarios VALUES (4, 'Carlos', 28)"
        );
        assertEquals(ResultadoQuery.Tipo.ESCRITURA, resultado.getTipo());
    }

    @Test
    void ejecutar_insert_filasAfectadasEsUno() {
        // Un INSERT de una fila debe reportar filasAfectadas == 1
        ResultadoQuery resultado = ejecutor.ejecutar(
            "INSERT INTO usuarios VALUES (4, 'Carlos', 28)"
        );
        assertEquals(1, resultado.getFilasAfectadas());
    }

    // -------------------------------------------------------------------------
    // UPDATE y DELETE
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_update_retornaFilasAfectadasCorrecto() {
        // UPDATE que modifica 2 filas debe reportar filasAfectadas == 2
        ResultadoQuery resultado = ejecutor.ejecutar(
            "UPDATE usuarios SET edad = 99 WHERE id IN (1, 2)"
        );
        assertEquals(2, resultado.getFilasAfectadas());
    }

    @Test
    void ejecutar_delete_retornaFilasAfectadasCorrecto() {
        // DELETE de una fila concreta debe reportar filasAfectadas == 1
        ResultadoQuery resultado = ejecutor.ejecutar(
            "DELETE FROM usuarios WHERE id = 3"
        );
        assertEquals(1, resultado.getFilasAfectadas());
    }

    // -------------------------------------------------------------------------
    // ERROR
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_sqlInvalido_retornaTipoError() {
        // SQL malformado debe producir un ResultadoQuery de tipo ERROR
        ResultadoQuery resultado = ejecutor.ejecutar("SELECCION INVALIDA ??");
        assertEquals(ResultadoQuery.Tipo.ERROR, resultado.getTipo());
    }

    @Test
    void ejecutar_sqlInvalido_nuncaPropagaSQLException() {
        // El ejecutor nunca debe propagar la excepción; el test pasa si no se lanza nada
        assertDoesNotThrow(() -> ejecutor.ejecutar("ESTO NO ES SQL VALIDO"));
    }

    @Test
    void ejecutar_sqlInvalido_mensajeErrorNoEsVacio() {
        // El mensaje de error debe contener información útil (no estar vacío)
        ResultadoQuery resultado = ejecutor.ejecutar("SELECT * FROM tabla_inexistente_xyz");
        assertNotNull(resultado.getMensaje());
        assertFalse(resultado.getMensaje().isBlank());
    }

    // -------------------------------------------------------------------------
    // tiempoMs
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_tiempoMsEsNoNegativo() {
        // El tiempo de ejecución siempre debe ser >= 0, independientemente del resultado
        ResultadoQuery lectura  = ejecutor.ejecutar("SELECT * FROM usuarios");
        ResultadoQuery escritura = ejecutor.ejecutar("INSERT INTO usuarios VALUES (10, 'X', 0)");
        ResultadoQuery error    = ejecutor.ejecutar("SQL INVALIDO");

        assertTrue(lectura.getTiempoMs()   >= 0);
        assertTrue(escritura.getTiempoMs() >= 0);
        assertTrue(error.getTiempoMs()     >= 0);
    }
}
