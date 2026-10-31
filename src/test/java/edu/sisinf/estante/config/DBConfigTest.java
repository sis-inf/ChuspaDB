package edu.sisinf.chuspadb.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConfigTest {

    @Test
    void toJdbcUrl_debeRetornarUrlValida() {
        DBConfig config = new DBConfig("localhost", 3306, "chuspadb_db", "admin", "1234");
        String url = config.toJdbcUrl();
        assertEquals("jdbc:mysql://localhost:3306/chuspadb_db", url);
    }

    @Test
    void constructor_debeLanzarExcepcion_cuandoPuertoEsCero() {
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 0, "chuspadb_db", "admin", "1234")
        );
    }

    @Test
    void constructor_debeLanzarExcepcion_cuandoCamposVacios() {
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("", 3306, "chuspadb_db", "admin", "1234")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 3306, "", "admin", "1234")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 3306, "chuspadb_db", "", "1234")
        );
    }
}
