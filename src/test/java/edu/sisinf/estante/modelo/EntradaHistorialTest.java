package edu.sisinf.chuspadb.modelo;

import edu.sisinf.chuspadb.modelo.EntradaHistorial;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntradaHistorialTest {

    @Test
    void debeExponerTodosLosValoresDelRegistro() {
        EntradaHistorial entrada = new EntradaHistorial(
                1000L, "SELECT 1", "demo", 25L, true);

        assertEquals(1000L, entrada.timestamp());
        assertEquals("SELECT 1", entrada.query());
        assertEquals("demo", entrada.baseDatos());
        assertEquals(25L, entrada.duracionMs());
        assertTrue(entrada.exitosa());
    }

    @Test
    void dosInstanciasConLosMismosValoresDebenSerIguales() {
        EntradaHistorial primera = new EntradaHistorial(
                1000L, "SELECT 1", "demo", 25L, true);
        EntradaHistorial segunda = new EntradaHistorial(
                1000L, "SELECT 1", "demo", 25L, true);

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void debeDistinguirInstanciasConDiferenteResultado() {
        EntradaHistorial primera = new EntradaHistorial(
                1000L, "SELECT 1", "demo", 25L, true);
        EntradaHistorial segunda = new EntradaHistorial(
                1000L, "SELECT 1", "demo", 25L, false);

        assertNotEquals(primera, segunda);
    }

    @Test
    void toStringDebeContenerQueryYBaseDeDatos() {
        EntradaHistorial entrada = new EntradaHistorial(
                1000L, "SELECT 1", "demo", 25L, true);

        assertTrue(entrada.toString().contains("SELECT 1"));
        assertTrue(entrada.toString().contains("demo"));
    }

    @Test
    void debeAceptarQueryNulaSinLanzarExcepcion() {
        EntradaHistorial entrada = assertDoesNotThrow(
                () -> new EntradaHistorial(
                        1000L, null, "demo", 25L, true));

        assertNull(entrada.query());
    }
}
