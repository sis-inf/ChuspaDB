package edu.sisinf.estante.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class EsquemaTest {

    @Test
    void constructorSinArgumentosDejaNombreYTablasEnNull() {
        Esquema esquema = new Esquema();

        assertNull(esquema.getNombre());
        assertNull(esquema.getTablas());
    }

    @Test
    void constructorConNombreYTablasVaciasDevuelveEsosValores() {
        Esquema esquema = new Esquema("public", List.of());

        assertEquals("public", esquema.getNombre());
        assertTrue(esquema.getTablas().isEmpty());
    }

    @Test
    void settersReemplazanLosValoresPrevios() {
        List<Tabla> tablasIniciales = new ArrayList<>();
        Esquema esquema = new Esquema("public", tablasIniciales);
        List<Tabla> tablasNuevas = new ArrayList<>();

        esquema.setNombre("ventas");
        esquema.setTablas(tablasNuevas);

        assertEquals("ventas", esquema.getNombre());
        assertSame(tablasNuevas, esquema.getTablas());
        assertNotSame(tablasIniciales, esquema.getTablas());
    }

    @Test
    void getTablasDevuelveLaMismaListaRecibidaEnElConstructor() {
        List<Tabla> tablas = new ArrayList<>();
        Esquema esquema = new Esquema("public", tablas);

        assertSame(tablas, esquema.getTablas());
    }
}