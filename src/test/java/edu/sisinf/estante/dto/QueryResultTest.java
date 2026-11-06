package edu.sisinf.chuspadb.dto;

import org.junit.jupiter.api.Test;
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
}
