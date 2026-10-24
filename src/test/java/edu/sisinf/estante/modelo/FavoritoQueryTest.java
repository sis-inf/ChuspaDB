package edu.sisinf.estante.modelo;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FavoritoQueryTest {

    @Test
    void creacionExitosaConParametrosValidos() {
        LocalDateTime fecha = LocalDateTime.of(2024, 1, 1, 0, 0);

        FavoritoQuery favorito = new FavoritoQuery(
                "Favorito1",
                "SELECT 1",
                "MySQL",
                fecha
        );

        assertEquals("Favorito1", favorito.nombre());
        assertEquals("SELECT 1", favorito.sql());
        assertEquals("MySQL", favorito.motor());
        assertEquals(fecha, favorito.fechaCreacion());
    }

    @Test
    void fechaCreacionNulaSeInicializaConValorNoNulo() {
        FavoritoQuery favorito = new FavoritoQuery(
                "Favorito1",
                "SELECT 1",
                "MySQL",
                null
        );

        assertNotNull(favorito.fechaCreacion());
    }

    @Test
    void nombreNuloLanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FavoritoQuery(null, "SELECT 1", "MySQL", null)
        );
    }

    @Test
    void nombreEnBlancoLanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FavoritoQuery("   ", "SELECT 1", "MySQL", null)
        );
    }

    @Test
    void sqlNuloLanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FavoritoQuery("Favorito1", null, "MySQL", null)
        );
    }

    @Test
    void sqlEnBlancoLanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FavoritoQuery("Favorito1", "   ", "MySQL", null)
        );
    }

    @Test
    void motorNuloLanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FavoritoQuery("Favorito1", "SELECT 1", null, null)
        );
    }

    @Test
    void motorEnBlancoLanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FavoritoQuery("Favorito1", "SELECT 1", "   ", null)
        );
    }
}
