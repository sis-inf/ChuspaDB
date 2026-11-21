package edu.sisinf.chuspadb.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConfigTest {

    @Test
    void constructorConParametros_debeAsignarYNormalizarCamposValidos() {
        DBConfig config = new DBConfig(
                "  jdbc:mysql://localhost:3306/chuspadb_db  ",
                "  admin  ",
                "1234",
                "  com.mysql.cj.jdbc.Driver  "
        );

        assertNotNull(config);
        assertEquals("jdbc:mysql://localhost:3306/chuspadb_db", config.getUrl());
        assertEquals("admin", config.getUsuario());
        assertEquals("1234", config.getPassword());
        assertEquals("com.mysql.cj.jdbc.Driver", config.getDriver());
    }

    @Test
    void constructorConParametros_debeNormalizarPasswordNuloACadenaVacia() {
        DBConfig config = new DBConfig(
                "jdbc:sqlite:memoria.db",
                "",
                null,
                "org.sqlite.JDBC"
        );

        assertNotNull(config);
        assertEquals("", config.getUsuario());
        assertEquals("", config.getPassword());
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
    void setUrl_debeLanzarExcepcionConValoresNulosVaciosOSinPrefijoJdbc() {
        DBConfig config = new DBConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setUrl(null));
        assertThrows(IllegalArgumentException.class, () -> config.setUrl(""));
        assertThrows(IllegalArgumentException.class, () -> config.setUrl("   "));
        assertThrows(IllegalArgumentException.class, () -> config.setUrl("mysql://localhost:3306/db"));
    }

    @Test
    void setUsuario_debeLanzarExcepcionSiEsNuloYPermitirVacio() {
        DBConfig config = new DBConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setUsuario(null));

        assertDoesNotThrow(() -> config.setUsuario("   "));
        assertEquals("", config.getUsuario());
    }

    @Test
    void setPassword_debeNormalizarNuloACadenaVaciaSinLanzarExcepcion() {
        DBConfig config = new DBConfig();

        assertDoesNotThrow(() -> config.setPassword(null));
        assertEquals("", config.getPassword());
    }

    @Test
    void setDriver_debeLanzarExcepcionConValoresNulosOVacios() {
        DBConfig config = new DBConfig();

        assertThrows(IllegalArgumentException.class, () -> config.setDriver(null));
        assertThrows(IllegalArgumentException.class, () -> config.setDriver(""));
        assertThrows(IllegalArgumentException.class, () -> config.setDriver("   "));
    }

    @Test
    void constructorConParametros_debeLanzarExcepcionAnteEntradasInvalidas() {
        assertThrows(IllegalArgumentException.class, () ->
                new DBConfig(null, "admin", "1234", "com.mysql.cj.jdbc.Driver"));
        assertThrows(IllegalArgumentException.class, () ->
                new DBConfig("http://localhost:3306/db", "admin", "1234", "com.mysql.cj.jdbc.Driver"));
        assertThrows(IllegalArgumentException.class, () ->
                new DBConfig("jdbc:mysql://localhost:3306/db", null, "1234", "com.mysql.cj.jdbc.Driver"));
        assertThrows(IllegalArgumentException.class, () ->
                new DBConfig("jdbc:mysql://localhost:3306/db", "admin", "1234", ""));
    }

    @Test
    void validar_debeLanzarIllegalStateExceptionSiConfiguracionEsIncompleta() {
        DBConfig vacia = new DBConfig();
        assertThrows(IllegalStateException.class, vacia::validar);

        DBConfig sinDriver = new DBConfig();
        sinDriver.setUrl("jdbc:mysql://localhost:3306/db");
        sinDriver.setUsuario("admin");
        assertThrows(IllegalStateException.class, sinDriver::validar);

        DBConfig sinUsuario = new DBConfig();
        sinUsuario.setUrl("jdbc:mysql://localhost:3306/db");
        sinUsuario.setDriver("com.mysql.cj.jdbc.Driver");
        assertThrows(IllegalStateException.class, sinUsuario::validar);
    }

    @Test
    void validar_debeCompletarConExitoCuandoLosCamposObligatoriosEstanConfigurados() {
        DBConfig config = new DBConfig();
        config.setUrl("jdbc:mysql://localhost:3306/db");
        config.setDriver("com.mysql.cj.jdbc.Driver");
        config.setUsuario("admin");

        assertDoesNotThrow(config::validar);
        assertEquals("", config.getPassword());
    }
}
