package edu.sisinf.estante.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
