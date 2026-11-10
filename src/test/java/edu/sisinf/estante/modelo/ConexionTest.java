package edu.sisinf.chuspadb.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConexionTest {

    @Test
    void copiarCreaCopiaConNombreDistinto() {
        Conexion original = new Conexion(
                "Original",
                "localhost",
                3306,
                "testdb",
                "root",
                "1234"
        );

        Conexion copia = original.copiar("Copia");

        assertEquals("Copia", copia.getNombre());
        assertEquals(original.getHost(), copia.getHost());
        assertEquals(original.getPuerto(), copia.getPuerto());
        assertEquals(original.getBasedatos(), copia.getBasedatos());
        assertEquals(original.getUsuario(), copia.getUsuario());
        assertNotSame(original, copia);
    }

    @Test
    void modificarCopiaNoAfectaOriginal() {
        Conexion original = new Conexion(
                "Original",
                "localhost",
                3306,
                "testdb",
                "root",
                "1234"
        );

        original.getEtiquetas().add("produccion");

        Conexion copia = original.copiar("Copia");

        copia.setHost("192.168.1.10");
        copia.getEtiquetas().add("desarrollo");

        assertEquals("localhost", original.getHost());
        assertEquals(1, original.getEtiquetas().size());
        assertFalse(original.getEtiquetas().contains("desarrollo"));
    }

    @Test
    void etiquetasFuncionanCorrectamente() {
        Conexion conexion = new Conexion();

        conexion.getEtiquetas().add("mysql");
        conexion.getEtiquetas().add("local");

        assertEquals(2, conexion.getEtiquetas().size());
        assertTrue(conexion.getEtiquetas().contains("mysql"));
        assertTrue(conexion.getEtiquetas().contains("local"));
    }

    @Test
    void copiarConNombreNullLanzaExcepcion() {
        Conexion conexion = new Conexion();

        assertThrows(
                IllegalArgumentException.class,
                () -> conexion.copiar(null)
        );
    }

    @Test
    void copiarConNombreVacioLanzaExcepcion() {
        Conexion conexion = new Conexion();

        assertThrows(
                IllegalArgumentException.class,
                () -> conexion.copiar("")
        );
    }
}
