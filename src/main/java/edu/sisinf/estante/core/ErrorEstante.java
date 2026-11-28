package edu.sisinf.chuspadb.core;

/**
 * Excepcion base del dominio de Estante/ChuspaDB.
 */
public class ErrorEstante extends RuntimeException {

    public ErrorEstante(String message) {
        super(message);
    }

    public ErrorEstante(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Alias de compatibilidad para el codigo que aun usa ErrorChuspaDB.
 */
class ErrorChuspaDB extends ErrorEstante {

    ErrorChuspaDB(String message) {
        super(message);
    }

    ErrorChuspaDB(String message, Throwable cause) {
        super(message, cause);
    }
}
