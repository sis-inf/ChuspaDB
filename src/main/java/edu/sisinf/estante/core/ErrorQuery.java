package edu.sisinf.estante.core;

/**
 * Excepcion lanzada cuando una consulta SQL falla durante su ejecucion.
 * Ejemplos: errores de sintaxis, tabla inexistente, privilegios insuficientes.
 */
public class ErrorQuery extends ErrorEstante {

    public ErrorQuery(String message) {
        super(message);
    }

    public ErrorQuery(String message, Throwable cause) {
        super(message, cause);
    }
}