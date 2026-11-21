package edu.sisinf.chuspadb.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConfigTest {

    @Test
    void constructorConParametros_debeAsignarTodosLosCampos() {
        DBConfig config = new DBConfig(
                "jdbc:mysql://localhost:3306/chuspadb_db",
                "admin",
                "1234",
                "com.mysql.cj.jdbc.Driver"
        );

        assertNotNull(config);
        assertEquals("jdbc:mysql://localhost:3306/chuspadb_db", config.getUrl());
        assertEquals("admin", config.getUsuario());
        assertEquals("1234", config.getPassword());
        assertEquals("com.mysql.cj.jdbc.Driver", config.getDriver());
    }

    @Test
    void constructorVacio_debeInicializarCamposEnNulo() {
        DBConfig config = new DBConfig();

        assertNotNull(config);
        assertNull(config.getUrl());
        assertNull(config.getUsuario());
        assertNull(config.getPassword());
        assertNull(config.getDriver());
    }

    @Test
    void settersYGetters_debenActualizarYRetornarValoresCorrectamente() {
        DBConfig config = new DBConfig();

        config.setUrl("jdbc:postgresql://127.0.0.1:5432/estante");
        config.setUsuario("postgres");
        config.setPassword("secreto");
        config.setDriver("org.postgresql.Driver");

        assertEquals("jdbc:postgresql://127.0.0.1:5432/estante", config.getUrl());
        assertEquals("postgres", config.getUsuario());
        assertEquals("secreto", config.getPassword());
        assertEquals("org.postgresql.Driver", config.getDriver());
    }

    @Test
    void constructor_debeManejarValoresNulosSinLanzarExcepcion() {
        DBConfig config = assertDoesNotThrow(() ->
                new DBConfig(null, null, null, null)
        );

        assertNotNull(config);
        assertNull(config.getUrl());
        assertNull(config.getUsuario());
        assertNull(config.getPassword());
        assertNull(config.getDriver());
    }

    @Test
    void settersYConstructor_debenPermitirCadenasVaciasYConEspacios() {
        DBConfig config = new DBConfig("", "   ", "", "  ");

        assertEquals("", config.getUrl());
        assertEquals("   ", config.getUsuario());
        assertEquals("", config.getPassword());
        assertEquals("  ", config.getDriver());

        config.setUrl("jdbc:sqlite:memoria.db");
        config.setPassword("");

        assertEquals("jdbc:sqlite:memoria.db", config.getUrl());
        assertEquals("", config.getPassword());
    }
}
