package edu.sisinf.chuspadb.core;

/**
 * Excepcion base para todos los errores de dominio en ChuspaDB.
 * Todas las excepciones tipadas en la aplicacion deben extender de esta clase.
 */
public class ErrorChuspaDB extends RuntimeException {

    public ErrorChuspaDB(String message) {
        super(message);
    }

    public ErrorChuspaDB(String message, Throwable cause) {
        super(message, cause);
    }
}