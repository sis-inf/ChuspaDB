package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorQuery;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
        ejecutor = new EjecutorQuery();

        // Crear tabla con datos de prueba
        ejecutarDirecto("CREATE TABLE usuarios (id INTEGER PRIMARY KEY, nombre TEXT, edad INTEGER)");
        ejecutarDirecto("INSERT INTO usuarios VALUES (1, 'Ana', 30)");
        ejecutarDirecto("INSERT INTO usuarios VALUES (2, 'Luis', 25)");
        ejecutarDirecto("INSERT INTO usuarios VALUES (3, 'María', 35)");
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
        ResultadoQuery resultado = ejecutor.ejecutar(conexion, "SELECT * FROM usuarios");
        assertEquals(ResultadoQuery.Tipo.LECTURA, resultado.getTipo());
    }

    @Test
    void ejecutar_select_contieneNombresDeColumnasCorrectos() {
        // El resultado de un SELECT debe incluir los nombres exactos de las columnas
        ResultadoQuery resultado = ejecutor.ejecutar(conexion, "SELECT id, nombre, edad FROM usuarios");
        assertEquals(List.of("id", "nombre", "edad"), resultado.getColumnas());
    }

    @Test
    void ejecutar_select_usaElAliasComoNombreDeColumna() {
        // El nombre de columna debe ser la etiqueta (alias) y no el nombre original
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "SELECT nombre AS persona FROM usuarios WHERE id = 2");
        assertEquals(List.of("persona"), resultado.getColumnas());
        assertEquals("Luis", resultado.getFilas().get(0).get(0));
    }

    @Test
    void ejecutar_select_contieneNumeroCorrectoDeFilas() {
        // La tabla tiene 3 filas; el resultado debe reflejarlo
        ResultadoQuery resultado = ejecutor.ejecutar(conexion, "SELECT * FROM usuarios");
        assertEquals(3, resultado.getFilas().size());
        assertEquals(3, resultado.getTotalFilas());
    }

    @Test
    void ejecutar_select_contieneValoresCorrectosEnCadaCelda() {
        // Los datos de la fila deben coincidir con lo insertado en setUp()
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "SELECT id, nombre, edad FROM usuarios WHERE id = 1");
        assertEquals(1, resultado.getFilas().size());

        List<Object> fila = resultado.getFilas().get(0);
        assertEquals(1,     ((Number) fila.get(0)).intValue());
        assertEquals("Ana", fila.get(1));
        assertEquals(30,    ((Number) fila.get(2)).intValue());
    }

    @Test
    void ejecutar_select_sinCoincidenciasDevuelveColumnasYNingunaFila() {
        // Sin filas que cumplan la condición, se conservan las columnas y la lista de filas queda vacía
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "SELECT id, nombre FROM usuarios WHERE edad > 100");
        assertEquals(List.of("id", "nombre"), resultado.getColumnas());
        assertTrue(resultado.estaVacio());
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_insert_retornaTipoEscritura() {
        // Un INSERT debe producir un ResultadoQuery de tipo ESCRITURA
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "INSERT INTO usuarios VALUES (4, 'Carlos', 28)");
        assertEquals(ResultadoQuery.Tipo.ESCRITURA, resultado.getTipo());
    }

    @Test
    void ejecutar_insert_filasAfectadasEsUnoYLaFilaSePersiste() throws SQLException {
        // Un INSERT de una fila debe reportar filasAfectadas == 1 y dejar 4 filas en la tabla
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "INSERT INTO usuarios VALUES (4, 'Carlos', 28)");
        assertEquals(1, resultado.getFilasAfectadas());
        assertEquals(4, contarFilas());
    }

    // -------------------------------------------------------------------------
    // UPDATE y DELETE
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_update_retornaNumeroExactoDeRegistrosModificados() throws SQLException {
        // UPDATE que modifica 2 de las 3 filas debe reportar filasAfectadas == 2
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "UPDATE usuarios SET edad = 99 WHERE id IN (1, 2)");
        assertEquals(ResultadoQuery.Tipo.ESCRITURA, resultado.getTipo());
        assertEquals(2, resultado.getFilasAfectadas());

        // Se verifica en la base que cambiaron esas dos filas y no la tercera
        assertEquals(99, edadDe(1));
        assertEquals(99, edadDe(2));
        assertEquals(35, edadDe(3));
    }

    @Test
    void ejecutar_update_sinCoincidenciasRetornaCeroFilasAfectadas() {
        // UPDATE que no encuentra filas debe reportar filasAfectadas == 0
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "UPDATE usuarios SET edad = 99 WHERE id = 999");
        assertEquals(0, resultado.getFilasAfectadas());
    }

    @Test
    void ejecutar_delete_retornaFilasAfectadasCorrecto() throws SQLException {
        // DELETE de una fila concreta debe reportar filasAfectadas == 1 y dejar 2 filas
        ResultadoQuery resultado = ejecutor.ejecutar(
            conexion, "DELETE FROM usuarios WHERE id = 3");
        assertEquals(1, resultado.getFilasAfectadas());
        assertEquals(2, contarFilas());
    }

    // -------------------------------------------------------------------------
    // ERROR
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_tablaInexistente_lanzaErrorQuery() {
        // Una consulta sobre una tabla que no existe debe lanzar ErrorQuery
        assertThrows(ErrorQuery.class,
            () -> ejecutor.ejecutar(conexion, "SELECT * FROM tabla_inexistente_xyz"));
    }

    @Test
    void ejecutar_errorQuery_conservaMensajeDelMotor() {
        // El mensaje de la excepción debe contener información útil (no estar vacío)
        ErrorQuery error = assertThrows(ErrorQuery.class,
            () -> ejecutor.ejecutar(conexion, "SELECT * FROM tabla_inexistente_xyz"));
        assertNotNull(error.getMessage());
        assertFalse(error.getMessage().isBlank());
    }

    // -------------------------------------------------------------------------
    // tiempoMs
    // -------------------------------------------------------------------------

    @Test
    void ejecutar_tiempoMsEsNoNegativo() {
        // El tiempo de ejecución siempre debe ser >= 0
        ResultadoQuery lectura   = ejecutor.ejecutar(conexion, "SELECT * FROM usuarios");
        ResultadoQuery escritura = ejecutor.ejecutar(conexion, "INSERT INTO usuarios VALUES (10, 'X', 0)");

        assertTrue(lectura.getTiempoMs()   >= 0);
        assertTrue(escritura.getTiempoMs() >= 0);
    }

    // -------------------------------------------------------------------------
    // Ayudantes: acceden a la base sin pasar por EjecutorQuery
    // -------------------------------------------------------------------------

    private void ejecutarDirecto(String sql) throws SQLException {
        try (Statement stmt = conexion.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private int contarFilas() throws SQLException {
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private int edadDe(int id) throws SQLException {
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT edad FROM usuarios WHERE id = " + id)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
