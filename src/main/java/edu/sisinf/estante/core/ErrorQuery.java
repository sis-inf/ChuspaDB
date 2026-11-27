package edu.sisinf.chuspadb.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
/**
 * Excepcion lanzada cuando una consulta SQL falla durante su ejecucion.
 * Ejemplos: errores de sintaxis, tabla inexistente, privilegios insuficientes.
 */
public class ErrorQuery extends ErrorChuspaDB {
    private static final Logger logger = LoggerFactory.getLogger(ErrorQuery.class);

    public ErrorQuery(String message) {
        super(validarMensaje(message));
        logger.debug("Instanciada ErrorQuery con mensaje: {}", getMessage());
    }

    public ErrorQuery(String message, Throwable cause) {
        super(validarMensaje(message), validarCausa(cause));
        logger.debug("Instanciada ErrorQuery con mensaje '{}' y causa '{}': {}",
                getMessage(), cause.getClass().getSimpleName(), cause.getMessage());
    }

     public static String validarSqlNoVacia(String sql) throws ErrorQuery {
        if (sql == null || sql.isBlank()) {
            logger.warn("Validacion fallida en ErrorQuery.validarSqlNoVacia: sentencia SQL nula o vacia.");
            throw new ErrorQuery("La sentencia SQL no puede ser nula ni estar vacia.");
        }
        return sql.trim();
    }
     public String obtenerDetalleCompleto() {
        Throwable causa = getCause();
        if (causa instanceof SQLException sqlEx) {
            String estado = (sqlEx.getSQLState() != null && !sqlEx.getSQLState().isBlank())
                    ? sqlEx.getSQLState()
                    : "N/A";
            return getMessage() + " [SQLState=" + estado + ", ErrorCode=" + sqlEx.getErrorCode() + "]";
        }
        if (causa != null && causa.getMessage() != null && !causa.getMessage().isBlank()) {
            return getMessage() + " (Causa: " + causa.getMessage() + ")";
        }
        return getMessage();
    }

    private static String validarMensaje(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("El mensaje de ErrorQuery no puede ser nulo ni estar vacio.");
        }
        return message.trim();
    }

    private static Throwable validarCausa(Throwable cause) {
        if (cause == null) {
            throw new IllegalArgumentException("La causa de ErrorQuery no puede ser nula.");
        }
        return cause;
    }
}
