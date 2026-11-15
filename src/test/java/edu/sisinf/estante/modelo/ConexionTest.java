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

    @Test
    void constructorCompletoAsignaValoresCorrectamente() {
        Conexion c = new Conexion("Test", "host", 5432, "db", "user", "pass");
        assertEquals("Test", c.getNombre());
        assertEquals("host", c.getHost());
        assertEquals(5432, c.getPuerto());
        assertEquals("db", c.getBasedatos());
        assertEquals("user", c.getUsuario());
        assertEquals("pass", c.getPassword());
    }

    @Test
    void gettersAndSettersFuncionan() {
        Conexion c = new Conexion();
        c.setNombre("DB");
        c.setHost("127.0.0.1");
        c.setPuerto(8080);
        c.setBasedatos("prodb");
        c.setUsuario("admin");
        c.setPassword("secret");
        
        assertEquals("DB", c.getNombre());
        assertEquals("127.0.0.1", c.getHost());
        assertEquals(8080, c.getPuerto());
        assertEquals("prodb", c.getBasedatos());
        assertEquals("admin", c.getUsuario());
        assertEquals("secret", c.getPassword());
    }

    @Test
    void testToString() {
        Conexion c = new Conexion("Test", "host", 3306, "db", "user", "pass");
        String result = c.toString();
        assertTrue(result.contains("Test"));
        assertTrue(result.contains("host"));
        assertTrue(result.contains("3306"));
    }

