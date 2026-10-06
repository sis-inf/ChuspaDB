package edu.sisinf.estante.core;

/**
 * Excepcion base para todos los errores de dominio en Estante.
 * Todas las excepciones tipadas en la aplicacion deben extender de esta clase.
 */
public class ErrorEstante extends RuntimeException {

    public ErrorEstante(String message) {
        super(message);
    }

    public ErrorEstante(String message, Throwable cause) {
        super(message, cause);
    }
}