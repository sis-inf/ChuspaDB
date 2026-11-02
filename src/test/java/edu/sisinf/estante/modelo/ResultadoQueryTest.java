package edu.sisinf.chuspadb.modelo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoQueryTest {

    @Test
    void deLecturaDevuelveTipoLecturaConColumnasFilasYTiempo() {
        List<String> columnas = List.of("id");
        List<List<Object>> filas = List.of(List.of(1), List.of(2));

        ResultadoQuery resultado = ResultadoQuery.deLectura(columnas, filas, 12);

        assertEquals(ResultadoQuery.Tipo.LECTURA, resultado.getTipo());
        assertEquals(columnas, resultado.getColumnas());
        assertEquals(filas, resultado.getFilas());
        assertEquals(12, resultado.getTiempoMs());
    }

    @Test
    void deEscrituraDevuelveTipoEscrituraConFilasAfectadasYTiempo() {
        ResultadoQuery resultado = ResultadoQuery.deEscritura(5, 8);

        assertEquals(ResultadoQuery.Tipo.ESCRITURA, resultado.getTipo());
        assertEquals(5, resultado.getFilasAfectadas());
        assertEquals(8, resultado.getTiempoMs());
    }

    @Test
    void deErrorDevuelveTipoErrorConMensaje() {
        ResultadoQuery resultado = ResultadoQuery.deError("fallo", 3);

        assertEquals(ResultadoQuery.Tipo.ERROR, resultado.getTipo());
        assertEquals("fallo", resultado.getMensaje());
    }

    @Test
    void constructorConColumnasCreaLecturaVaciaYCopiaLasColumnas() {
        List<String> columnas = new ArrayList<>(List.of("a", "b"));

        ResultadoQuery resultado = new ResultadoQuery(columnas);
        columnas.add("c");

        assertEquals(ResultadoQuery.Tipo.LECTURA, resultado.getTipo());
        assertTrue(resultado.estaVacio());
        assertEquals(List.of("a", "b"), resultado.getColumnas());
        assertNotSame(columnas, resultado.getColumnas());
    }

    @Test
    void agregarFilaIncrementaTotalFilasYDejaDeEstarVacio() {
        ResultadoQuery resultado = new ResultadoQuery(List.of("a", "b"));

        resultado.agregarFila(List.of(1, 2));

        assertEquals(1, resultado.getTotalFilas());
        assertFalse(resultado.estaVacio());
    }

    @Test
    void agregarFilaConTamanoDistintoONullLanzaIllegalArgumentException() {
        ResultadoQuery resultado = new ResultadoQuery(List.of("a", "b"));

        assertThrows(IllegalArgumentException.class, () -> resultado.agregarFila(List.of(1)));
        assertThrows(IllegalArgumentException.class, () -> resultado.agregarFila(Arrays.asList(1, 2, 3)));
        assertThrows(IllegalArgumentException.class, () -> resultado.agregarFila(null));
    }
}
