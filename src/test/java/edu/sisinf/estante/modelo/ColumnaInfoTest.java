package edu.sisinf.chuspadb.modelo;

import edu.sisinf.chuspadb.modelo.ColumnaInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
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

    @Test
    void aceptaNombreYTipoSQLNulos() {
        ColumnaInfo columna = assertDoesNotThrow(
                () -> new ColumnaInfo(null, null, true, null, null, false)
        );

        assertNotNull(columna);
        assertNull(columna.nombre());
        assertNull(columna.tipoSQL());
    }

    @Test
    void aceptaCadenasVacias() {
        ColumnaInfo columna = new ColumnaInfo("", "", false, 1, "", false);

        assertEquals("", columna.nombre());
        assertEquals("", columna.tipoSQL());
        assertEquals("", columna.valorDefault());
    }

    @Test
    void conservaEspaciosSinRecortar() {
        ColumnaInfo columna = new ColumnaInfo("  id  ", " INT ", false, 1, " 0 ", false);

        assertEquals("  id  ", columna.nombre());
        assertEquals(" INT ", columna.tipoSQL());
        assertEquals(" 0 ", columna.valorDefault());
    }

    @Test
    void desempaquetarTamanoNuloLanzaNullPointerException() {
        ColumnaInfo columna = new ColumnaInfo("x", "INT", true, null, null, false);

        assertThrows(NullPointerException.class, () -> {
            int tamano = columna.tamano();
            assertEquals(0, tamano);
        });
    }

    @Test
    void aceptaTamanoCero() {
        ColumnaInfo columna = new ColumnaInfo("x", "INT", true, 0, null, false);

        assertNotNull(columna.tamano());
        assertEquals(0, columna.tamano());
    }

    @Test
    void aceptaTamanoNegativoSinValidar() {
        ColumnaInfo columna = new ColumnaInfo("x", "INT", true, -1, null, false);

        assertEquals(-1, columna.tamano());
    }

    @Test
    void aceptaTamanoEnLosExtremosDeInteger() {
        ColumnaInfo maximo = new ColumnaInfo("x", "INT", true, Integer.MAX_VALUE, null, false);
        ColumnaInfo minimo = new ColumnaInfo("x", "INT", true, Integer.MIN_VALUE, null, false);

        assertEquals(Integer.MAX_VALUE, maximo.tamano());
        assertEquals(Integer.MIN_VALUE, minimo.tamano());
    }

    @Test
    void aceptaNombreMuyLargo() {
        String largo = "a".repeat(10_000);
        ColumnaInfo columna = new ColumnaInfo(largo, "TEXT", true, null, null, false);

        assertEquals(10_000, columna.nombre().length());
        assertEquals(largo, columna.nombre());
    }

    @Test
    void igualdadConTodosLosCamposNulos() {
        ColumnaInfo columna1 = new ColumnaInfo(null, null, false, null, null, false);
        ColumnaInfo columna2 = new ColumnaInfo(null, null, false, null, null, false);

        assertEquals(columna1, columna2);
        assertEquals(columna1.hashCode(), columna2.hashCode());
    }

    @Test
    void igualdadFalsaSiCambiaCualquierComponente() {
        ColumnaInfo base = new ColumnaInfo("id", "INT", false, 11, "0", true);

        assertNotEquals(base, new ColumnaInfo("otro", "INT", false, 11, "0", true));
        assertNotEquals(base, new ColumnaInfo("id", "BIGINT", false, 11, "0", true));
        assertNotEquals(base, new ColumnaInfo("id", "INT", true, 11, "0", true));
        assertNotEquals(base, new ColumnaInfo("id", "INT", false, 12, "0", true));
        assertNotEquals(base, new ColumnaInfo("id", "INT", false, null, "0", true));
        assertNotEquals(base, new ColumnaInfo("id", "INT", false, 11, "1", true));
        assertNotEquals(base, new ColumnaInfo("id", "INT", false, 11, null, true));
        assertNotEquals(base, new ColumnaInfo("id", "INT", false, 11, "0", false));
    }

    @Test
    void equalsConNullYOtroTipoEsFalso() {
        ColumnaInfo columna = new ColumnaInfo("id", "INT", false, 11, "0", true);

        assertFalse(columna.equals(null));
        assertNotEquals(columna, "id");
    }

    @Test
    void hashCodeEsConsistenteEnLlamadasRepetidas() {
        ColumnaInfo columna = new ColumnaInfo("id", "INT", false, 11, "0", true);
        int primero = columna.hashCode();

        assertEquals(primero, columna.hashCode());
        assertEquals(primero, columna.hashCode());
    }

    @Test
    void toStringContieneLosComponentes() {
        ColumnaInfo columna = new ColumnaInfo("id", "INT", false, 11, "0", true);
        String texto = columna.toString();

        assertNotNull(texto);
        assertTrue(texto.contains("nombre=id"));
        assertTrue(texto.contains("tipoSQL=INT"));
        assertTrue(texto.contains("nullable=false"));
        assertTrue(texto.contains("tamano=11"));
        assertTrue(texto.contains("valorDefault=0"));
        assertTrue(texto.contains("esPrimaryKey=true"));
    }

    @Test
    void esRecordConSeisComponentesEnOrden() {
        assertTrue(ColumnaInfo.class.isRecord());

        RecordComponent[] componentes = ColumnaInfo.class.getRecordComponents();
        assertNotNull(componentes);
        assertEquals(6, componentes.length);

        String[] nombres = Arrays.stream(componentes)
                .map(RecordComponent::getName)
                .toArray(String[]::new);

        assertArrayEquals(
                new String[] {"nombre", "tipoSQL", "nullable", "tamano", "valorDefault", "esPrimaryKey"},
                nombres
        );
        assertEquals(boolean.class, componentes[2].getType());
        assertEquals(Integer.class, componentes[3].getType());
    }

    @Test
    void camposSonPrivadosYFinales() {
        int contados = 0;

        for (Field campo : ColumnaInfo.class.getDeclaredFields()) {
            if (Modifier.isStatic(campo.getModifiers())) {
                continue;
            }
            contados++;
            assertTrue(Modifier.isPrivate(campo.getModifiers()));
            assertTrue(Modifier.isFinal(campo.getModifiers()));
        }

        assertEquals(6, contados);
    }

    @Test
    void valorDefaultTextoNullEsDistintoDeNulo() {
        ColumnaInfo conTexto = new ColumnaInfo("a", "INT", true, null, "null", false);
        ColumnaInfo conNulo = new ColumnaInfo("a", "INT", true, null, null, false);

        assertNotEquals(conTexto, conNulo);
        assertEquals("null", conTexto.valorDefault());
        assertNull(conNulo.valorDefault());
    }

    @Test
    void permiteNullableYPrimaryKeyJuntos() {
        ColumnaInfo columna = new ColumnaInfo("id", "INT", true, 11, null, true);

        assertTrue(columna.nullable());
        assertTrue(columna.esPrimaryKey());
    }
}
