package edu.sisinf.chuspadb.dto;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para verificar que QueryResult
 * almacena y retorna datos correctamente.
 */
class QueryResultTest {

    @Test
    void getRows_debeRetornarDosFilas() {
        // Arrange
        List<String> columnas = List.of("id", "nombre");
        List<List<Object>> filas = List.of(
            List.of(1, "Ana"),
            List.of(2, "Luis")
        );

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertEquals(2, resultado.getRows().size());
    }

    @Test
    void getColumns_debeRetornarNombreCorrecto() {
        // Arrange
        List<String> columnas = List.of("id", "nombre", "email");
        List<List<Object>> filas = List.of(
            List.of(1, "Ana", "ana@email.com")
        );

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertEquals("nombre", resultado.getColumns().get(1));
    }

    @Test
    void isEmpty_debeRetornarFalse_cuandoHayFilas() {
        // Arrange
        List<String> columnas = List.of("id");
        List<List<Object>> filas = List.of(List.of(1));

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertFalse(resultado.isEmpty());
    }

    @Test
    void isEmpty_debeRetornarTrue_cuandoNoHayFilas() {
        // Arrange
        List<String> columnas = List.of("id");
        List<List<Object>> filas = List.of();

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void of_debeLanzarExcepcion_cuandoColumnasEsNulo() {
        // Arrange
        List<List<Object>> filas = List.of();

        // Act
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> QueryResult.of(null, filas)
        );

        // Assert
        assertEquals("columns no puede ser nulo", ex.getMessage());
    }

    @Test
    void of_debeLanzarExcepcion_cuandoFilasEsNulo() {
        // Arrange
        List<String> columnas = List.of("id");

        // Act
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> QueryResult.of(columnas, null)
        );

        // Assert
        assertEquals("rows no puede ser nulo", ex.getMessage());
    }

    @Test
    void of_debeValidarColumnasPrimero_cuandoAmbosSonNulos() {
        // Act
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> QueryResult.of(null, null)
        );

        // Assert
        assertEquals("columns no puede ser nulo", ex.getMessage());
    }

    @Test
    void of_debeAceptarColumnasYFilasVacias() {
        // Arrange
        List<String> columnas = List.of();
        List<List<Object>> filas = List.of();

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getColumns().isEmpty());
        assertTrue(resultado.isEmpty());
    }

    @Test
    void getRows_debePermitirValoresNulosEnLasFilas() {
        // Arrange
        List<String> columnas = List.of("id", "nombre");
        List<Object> fila = Arrays.asList(1, null);
        List<List<Object>> filas = List.of(fila);

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertNotNull(resultado.getRows());
        assertEquals(1, resultado.getRows().get(0).get(0));
        assertNull(resultado.getRows().get(0).get(1));
        assertFalse(resultado.isEmpty());
    }

    @Test
    void getColumns_debeRetornarLaMismaListaRecibida() {
        // Arrange
        List<String> columnas = List.of("id", "nombre");
        List<List<Object>> filas = List.of();

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertSame(columnas, resultado.getColumns());
        assertSame(filas, resultado.getRows());
    }

    @Test
    void getRows_debeSoportarUnaCantidadGrandeDeFilas() {
        // Arrange
        List<String> columnas = List.of("id");
        List<List<Object>> filas = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            filas.add(List.of(i));
        }

        // Act
        QueryResult resultado = QueryResult.of(columnas, filas);

        // Assert
        assertEquals(10000, resultado.getRows().size());
        assertEquals(9999, resultado.getRows().get(9999).get(0));
        assertFalse(resultado.isEmpty());
    }
}
