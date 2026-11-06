package edu.sisinf.estante.modelo;

import edu.sisinf.chuspadb.modelo.ImportacionResultado;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportacionResultadoTest {

    @Test
    void exponeFilasInsertadasFallidasYErrores() {
        ImportacionResultado resultado = new ImportacionResultado(
                3, 1, List.of("fila 4: error")
        );

        assertEquals(3, resultado.filasInsertadas());
        assertEquals(1, resultado.filasFallidas());
        assertEquals(List.of("fila 4: error"), resultado.errores());
    }

    @Test
    void instanciasConLosMismosValoresSonIguales() {
        ImportacionResultado resultado1 = new ImportacionResultado(
                3, 1, List.of("fila 4: error")
        );
        ImportacionResultado resultado2 = new ImportacionResultado(
                3, 1, List.of("fila 4: error")
        );

        assertEquals(resultado1, resultado2);
        assertEquals(resultado1.hashCode(), resultado2.hashCode());
    }

    @Test
    void importacionSinErroresSeRepresentaCorrectamente() {
        ImportacionResultado resultado = new ImportacionResultado(
                5, 0, List.of()
        );

        assertEquals(5, resultado.filasInsertadas());
        assertEquals(0, resultado.filasFallidas());
        assertTrue(resultado.errores().isEmpty());
    }

    @Test
    void toStringContieneLosTresValores() {
        ImportacionResultado resultado = new ImportacionResultado(
                3, 1, List.of("fila 4: error")
        );

        String texto = resultado.toString();

        assertTrue(texto.contains("3"));
        assertTrue(texto.contains("1"));
        assertTrue(texto.contains("fila 4: error"));
    }
}
