package edu.sisinf.estante.modelo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TablaTest {

    @Test
    void testConstructorYGetters() {
        Tabla tabla = new Tabla("Usuarios");
        assertEquals("Usuarios", tabla.getNombre());
        assertNotNull(tabla.getColumnas());
    }

    @Test
    void testSetters() {
        Tabla tabla = new Tabla("Temporal");
        tabla.setNombre("Clientes");
        assertEquals("Clientes", tabla.getNombre());
    }

    @Test
    void testAgregarColumna() {
        Tabla tabla = new Tabla("Facturas");
        tabla.agregarColumna("id", "INTEGER");
        
        assertEquals(1, tabla.getColumnas().size());
    }

    @Test
    void testBuscarColumnaPorNombre() {
        Tabla tabla = new Tabla("Productos");
        tabla.agregarColumna("precio", "REAL");
        
        assertNotNull(tabla.buscarColumna("precio"));
        assertNull(tabla.buscarColumna("stock"));
    }
}
