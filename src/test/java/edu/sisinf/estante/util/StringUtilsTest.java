package edu.sisinf.chuspadb.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    @DisplayName("Truncar texto corto sin cambios")
    void testTruncarTextoCorto() {
        String resultado = StringUtils.truncar("Hola", 10);
        assertEquals("Hola", resultado);
    }

    @Test
    @DisplayName("Truncar texto largo con puntos suspensivos")
    void testTruncarTextoLargo() {
        String resultado = StringUtils.truncar("Hola mundo", 4);
        assertEquals("H...", resultado);
    }

    @Test
    @DisplayName("Enmascarar valor normal")
    void testEnmascararNormal() {
        String resultado = StringUtils.enmascarar("123456789");
        assertEquals("****", resultado);
    }

    @Test
    @DisplayName("Enmascarar valor null")
    void testEnmascararNull() {
        String resultado = StringUtils.enmascarar(null);
        assertNull(resultado);
    }

    @Test
    @DisplayName("Enmascarar valor vacío")
    void testEnmascararVacio() {
        String resultado = StringUtils.enmascarar("");
        assertEquals("", resultado);
    }

    @Test
    @DisplayName("Capitalizar varias palabras")
    void testCapitalizarVariasPalabras() {
        String resultado = StringUtils.capitalizar("hola mundo");
        assertEquals("Hola Mundo", resultado);
    }

    @Test
    @DisplayName("esVacioONulo con null")
    void testEsVacioONuloNull() {
        assertTrue(StringUtils.esVacioONulo(null));
    }

    @Test
    @DisplayName("esVacioONulo con vacío")
    void testEsVacioONuloVacio() {
        assertTrue(StringUtils.esVacioONulo(""));
    }

    @Test
    @DisplayName("esVacioONulo con texto normal")
    void testEsVacioONuloTexto() {
        assertFalse(StringUtils.esVacioONulo("Hola"));
    }

    @Test
    void truncaEnElLimiteDePuntosSuspensivos() {
        assertEquals("abc", StringUtils.truncar("abcdef", 3));
        assertEquals("a...", StringUtils.truncar("abcdef", 4));
        assertEquals("abcd", StringUtils.truncar("abcd", 4));
    }

    @Test
    void conservaTextoSiElLimiteNoEsPositivo() {
        assertEquals("abc", StringUtils.truncar("abc", 0));
        assertEquals("abc", StringUtils.truncar("abc", -1));
    }

    @Test
    void trataTextoNuloComoCadenaVaciaAlTruncarYEscapar() {
        assertEquals("", StringUtils.truncar(null, 4));
        assertEquals("", StringUtils.escaparLike(null));
    }

    @Test
    void escapaAmbosComodinesLikeYConservaElTextoOrdinario() {
        assertEquals("a\\%\\_\\%z", StringUtils.escaparLike("a%_%z"));
        assertEquals("texto", StringUtils.escaparLike("texto"));
    }

    @Test
    void capitalizaTextoConSeparadoresMixtos() {
        assertEquals("Hola Mundo", StringUtils.capitalizar(" \tHOLA \n mUndo  "));
    }
}
