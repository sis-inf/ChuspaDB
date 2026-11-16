package edu.sisinf.chuspadb;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de entrada de ChuspaDB para el JAR ejecutable.
 *
 * JavaFX requiere una clase lanzadora que no extienda Application
 * para que el JAR shaded funcione con java -jar sin argumentos
 * adicionales de módulos JVM.
 */
public class Launcher {

    private static final Logger logger =
            LoggerFactory.getLogger(Launcher.class);

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args argumentos de línea de comandos
     * @throws NullPointerException si args es null
     */
    public static void main(String[] args) {
        Objects.requireNonNull(args, "Los argumentos no pueden ser null");

        logger.debug("Iniciando ChuspaDB con {} argumentos", args.length);

        try {
            App.main(args);
            logger.debug("Finalizó la llamada a App.main");
        } catch (RuntimeException | Error e) {
            logger.error("Error al iniciar ChuspaDB", e);
            throw e;
        }
    }
}
