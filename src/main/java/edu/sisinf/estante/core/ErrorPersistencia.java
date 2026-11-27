package edu.sisinf.chuspadb.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
/**
 * Excepcion lanzada cuando falla una operacion de persistencia de configuracion.
 * Ejemplos: archivo no escribible, configuracion corrupta, error de E/S al guardar.
 */
public class ErrorPersistencia extends ErrorChuspaDB {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPersistencia.class);

    public ErrorPersistencia(String message) {
        super(validarMensaje(message));
        logger.debug("Instanciada ErrorPersistencia con mensaje: {}", getMessage());
    }

    public ErrorPersistencia(String message, Throwable cause) {
        super(validarMensaje(message), validarCausa(cause));
        logger.debug("Instanciada ErrorPersistencia con mensaje '{}' y causa '{}': {}",
                getMessage(), cause.getClass().getSimpleName(), cause.getMessage());
    }
    public static String validarRutaNoVacia(String ruta) throws ErrorPersistencia {
        if (ruta == null || ruta.isBlank()) {
            logger.warn("Validacion fallida en ErrorPersistencia.validarRutaNoVacia: ruta nula o vacia.");
            throw new ErrorPersistencia("La ruta de persistencia no puede ser nula ni estar vacia.");
        }
        return ruta.trim();
    }
    public String obtenerDetalleCompleto() {
        Throwable causa = getCause();
        if (causa instanceof IOException ioEx) {
            String detalleIo = (ioEx.getMessage() != null && !ioEx.getMessage().isBlank())
                    ? ioEx.getMessage()
                    : "Error de entrada/salida";
            return getMessage() + " [IOException: " + detalleIo + "]";
        }
        if (causa != null && causa.getMessage() != null && !causa.getMessage().isBlank()) {
            return getMessage() + " (Causa: " + causa.getMessage() + ")";
        }
        return getMessage();
    }

    private static String validarMensaje(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("El mensaje de ErrorPersistencia no puede ser nulo ni estar vacio.");
        }
        return message.trim();
    }

    private static Throwable validarCausa(Throwable cause) {
        if (cause == null) {
            throw new IllegalArgumentException("La causa de ErrorPersistencia no puede ser nula.");
        }
        return cause;
    }
}
