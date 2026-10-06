package edu.sisinf.estante.core;

/**
 * Excepcion lanzada cuando falla una operacion de persistencia de configuracion.
 * Ejemplos: archivo no escribible, configuracion corrupta, error de E/S al guardar.
 */
public class ErrorPersistencia extends ErrorEstante {

    public ErrorPersistencia(String message) {
        super(message);
    }

    public ErrorPersistencia(String message, Throwable cause) {
        super(message, cause);
    }
}