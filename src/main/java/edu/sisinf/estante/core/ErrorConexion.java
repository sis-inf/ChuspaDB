package edu.sisinf.estante.core;

/**
 * Excepcion lanzada cuando falla un intento de conexion a la base de datos.
 * Ejemplos: credenciales invalidas, host inalcanzable, puerto incorrecto.
 */
public class ErrorConexion extends ErrorEstante {

    public ErrorConexion(String message) {
        super(message);
    }

    public ErrorConexion(String message, Throwable cause) {
        super(message, cause);
    }
}