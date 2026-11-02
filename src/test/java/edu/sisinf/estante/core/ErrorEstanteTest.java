package edu.sisinf.chuspadb.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorChuspaDBTest {

    @Test
    void errorConexionDebeExtenderErrorChuspaDB() {
        ErrorConexion error = new ErrorConexion("Error de conexión");

        assertTrue(error instanceof ErrorChuspaDB);
        assertEquals("Error de conexión", error.getMessage());
    }

    @Test
    void errorPersistenciaDebeExtenderErrorChuspaDB() {
        ErrorPersistencia error = new ErrorPersistencia("Error de persistencia");

        assertTrue(error instanceof ErrorChuspaDB);
        assertEquals("Error de persistencia", error.getMessage());
    }

    @Test
    void errorQueryDebeExtenderErrorChuspaDB() {
        ErrorQuery error = new ErrorQuery("Error de consulta");

        assertTrue(error instanceof ErrorChuspaDB);
        assertEquals("Error de consulta", error.getMessage());
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
