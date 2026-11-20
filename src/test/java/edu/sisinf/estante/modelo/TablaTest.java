package edu.sisinf.estante.modelo;

import edu.sisinf.chuspadb.modelo.ColumnaInfo;
import edu.sisinf.chuspadb.modelo.Tabla;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TablaTest {

    @Test
    void testConstructorVacioYSetters() {
        Tabla tabla = new Tabla();
        tabla.setNombre("Usuarios");
        tabla.setEsquema("public");

        assertEquals("Usuarios", tabla.getNombre());
        assertEquals("public", tabla.getEsquema());
    }

    @Test
    void testConstructorCompleto() {
        List<ColumnaInfo> columnas = new ArrayList<>();
        columnas.add(new ColumnaInfo("id", "INTEGER", false, null, null, true));

        Tabla tabla = new Tabla("Facturas", "ventas", columnas);

        assertEquals("Facturas", tabla.getNombre());
        assertEquals("ventas", tabla.getEsquema());
        assertNotNull(tabla.getColumnas());
        assertEquals(1, tabla.getColumnas().size());
    }

    @Test
    void testValoresNulos() {
        Tabla tabla = new Tabla();

        assertNull(tabla.getNombre());
        assertNull(tabla.getEsquema());
        assertNull(tabla.getColumnas());
    }

    @Test
    void testListaVaciaLimite() {
        Tabla tabla = new Tabla("Vacia", "esquema", new ArrayList<>());

        assertNotNull(tabla.getColumnas());
        assertEquals(0, tabla.getColumnas().size());
    }

    @Test
    void testSetColumnasReemplazaLista() {
        Tabla tabla = new Tabla();
        List<ColumnaInfo> columnas = new ArrayList<>();
        columnas.add(new ColumnaInfo("precio", "REAL", true, null, null, false));
        columnas.add(new ColumnaInfo("stock", "INTEGER", true, null, null, false));

        tabla.setColumnas(columnas);

        assertEquals(2, tabla.getColumnas().size());
        assertEquals("precio", tabla.getColumnas().get(0).nombre());
    }
}
