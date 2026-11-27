package edu.sisinf.estante.modelo;

import edu.sisinf.chuspadb.modelo.ImportacionResultado;
import java.util.List;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotNull(resultado.errores());
        assertTrue(resultado.errores().isEmpty());
    }

    @Test
    void manejaValoresLimiteCeroYMaximos() {
        ImportacionResultado vacio = new ImportacionResultado(0, 0, Collections.emptyList());
        assertEquals(0, vacio.filasInsertadas());
        assertEquals(0, vacio.filasFallidas());
        assertTrue(vacio.errores().isEmpty());

        ImportacionResultado maximo = new ImportacionResultado(Integer.MAX_VALUE, 0, List.of());
        assertEquals(Integer.MAX_VALUE, maximo.filasInsertadas());
        assertEquals(0, maximo.filasFallidas());
    }

    @Test
    void manejaListaDeErroresNulaYComparacionConNulo() {
        ImportacionResultado conErroresNulos = new ImportacionResultado(2, 1, null);

        assertEquals(2, conErroresNulos.filasInsertadas());
        assertEquals(1, conErroresNulos.filasFallidas());
        assertNull(conErroresNulos.errores());
        assertNotNull(conErroresNulos.toString());
        assertNotEquals(null, conErroresNulos);
    }

    @Test
    void instanciasConValoresDistintosNoSonIguales() {
        ImportacionResultado base = new ImportacionResultado(3, 1, List.of("error A"));
        ImportacionResultado distintasInsertadas = new ImportacionResultado(4, 1, List.of("error A"));
        ImportacionResultado distintasFallidas = new ImportacionResultado(3, 2, List.of("error A"));
        ImportacionResultado distintosErrores = new ImportacionResultado(3, 1, List.of("error B"));

        assertNotEquals(base, distintasInsertadas);
        assertNotEquals(base, distintasFallidas);
        assertNotEquals(base, distintosErrores);
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
