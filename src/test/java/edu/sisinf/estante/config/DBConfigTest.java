package edu.sisinf.chuspadb.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConfigTest {

    @Test
    void toJdbcUrl_debeRetornarUrlValida() {
        DBConfig config = new DBConfig("localhost", 3306, "chuspadb_db", "admin", "1234");
        String url = config.toJdbcUrl();
        assertNotNull(url);
        assertEquals("jdbc:mysql://localhost:3306/chuspadb_db", url);
    }
    
    @Test
    void toJdbcUrl_debeRetornarUrlValida_enPuertosLimite() {
        DBConfig configMin = new DBConfig("127.0.0.1", 1, "test_db", "root", "pass");
        DBConfig configMax = new DBConfig("127.0.0.1", 65535, "test_db", "root", "");

        assertNotNull(configMin.toJdbcUrl());
        assertEquals("jdbc:mysql://127.0.0.1:1/test_db", configMin.toJdbcUrl());

        assertNotNull(configMax.toJdbcUrl());
        assertEquals("jdbc:mysql://127.0.0.1:65535/test_db", configMax.toJdbcUrl());
    }

    @Test
    void constructor_debeLanzarExcepcion_cuandoPuertoEsCero() {
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 0, "chuspadb_db", "admin", "1234")
        );
    }
    
    @Test
    void constructor_debeLanzarExcepcion_cuandoPuertoEsNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", -1, "chuspadb_db", "admin", "1234")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", -3306, "chuspadb_db", "admin", "1234")
        );
    }

    @Test
    void constructor_debeLanzarExcepcion_cuandoPuertoSuperaMaximo() {
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 65536, "chuspadb_db", "admin", "1234")
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

    @Test
    void constructor_debeLanzarExcepcion_cuandoCamposSoloEspacios() {
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("   ", 3306, "chuspadb_db", "admin", "1234")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 3306, "   ", "admin", "1234")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 3306, "chuspadb_db", "   ", "1234")
        );
    }

    @Test
    void constructor_debeLanzarExcepcion_cuandoParametrosSonNulos() {
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig(null, 3306, "chuspadb_db", "admin", "1234")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 3306, null, "admin", "1234")
        );
        assertThrows(IllegalArgumentException.class, () ->
            new DBConfig("localhost", 3306, "chuspadb_db", null, "1234")
        );
    }
}
