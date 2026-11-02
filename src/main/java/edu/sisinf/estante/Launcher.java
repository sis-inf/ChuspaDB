package edu.sisinf.chuspadb;

/**
 * Clase de entrada de ChuspaDB para el JAR ejecutable.
 *
 * JavaFX requiere una clase lanzadora que no extienda Application
 * para que el JAR shaded funcione con java -jar sin argumentos
 * adicionales de módulos JVM.
 */
public class Launcher {

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        App.main(args);
    }
}
