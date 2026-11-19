package edu.sisinf.chuspadb.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorEstanteTest {

    @Test
    void errorConexionDebeExtenderErrorChuspaDB() {
        Throwable causa = new RuntimeException("Causa original");
        ErrorConexion error = new ErrorConexion("Error de conexión", causa);

        assertTrue(error instanceof ErrorChuspaDB);
        assertEquals("Error de conexión", error.getMessage());
        assertSame(causa, error.getCause());
    }

    @Test
    void errorPersistenciaDebeExtenderErrorChuspaDB() {
        Throwable causa = new RuntimeException("Causa original");
        ErrorPersistencia error = new ErrorPersistencia("Error de persistencia", causa);

        assertTrue(error instanceof ErrorChuspaDB);
        assertEquals("Error de persistencia", error.getMessage());
        assertSame(causa, error.getCause());
    }

    @Test
    void errorQueryDebeExtenderErrorChuspaDB() {
        Throwable causa = new RuntimeException("Causa original");
        ErrorQuery error = new ErrorQuery("Error de consulta", causa);

        assertTrue(error instanceof ErrorChuspaDB);
        assertEquals("Error de consulta", error.getMessage());
        assertSame(causa, error.getCause());
    }

    @Test
    void errorChuspaDBDebeSerRuntimeException() {
        ErrorChuspaDB error = new ErrorChuspaDB("Error base");

        assertTrue(error instanceof RuntimeException);
        assertEquals("Error base", error.getMessage());
    }

    @Test
    void debePoderLanzarseSinTryCatch() {
        assertThrows(ErrorChuspaDB.class, () -> {
            throw new ErrorChuspaDB("Excepción sin try/catch");
        });
    }
}
