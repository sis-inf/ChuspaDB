package edu.sisinf.estante.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColumnaInfoTest {

    @Test
    void componentesRetornanValoresDelConstructor() {
        ColumnaInfo columna = new ColumnaInfo(
                "id",
                "INT",
                false,
                11,
                "0",
                true
        );

        assertEquals("id", columna.nombre());
        assertEquals("INT", columna.tipoSQL());
        assertFalse(columna.nullable());
        assertEquals(11, columna.tamano());
        assertEquals("0", columna.valorDefault());
        assertTrue(columna.esPrimaryKey());
    }

    @Test
    void igualdadEstructuralEntreInstanciasEquivalentes() {
        ColumnaInfo columna1 = new ColumnaInfo("nombre", "VARCHAR", true, 255, null, false);
        ColumnaInfo columna2 = new ColumnaInfo("nombre", "VARCHAR", true, 255, null, false);

        assertEquals(columna1, columna2);
        assertEquals(columna1.hashCode(), columna2.hashCode());
    }

    @Test
    void aceptaTamanoYValorDefaultNulos() {
        ColumnaInfo columna = new ColumnaInfo(
                "descripcion",
                "TEXT",
                true,
                null,
                null,
                false
        );

        assertNull(columna.tamano());
        assertNull(columna.valorDefault());
    }
}
