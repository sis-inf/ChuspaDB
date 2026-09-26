// ============================================================
// ARCHIVO: ConfiguracionApp.java
// RUTA: src/main/java/edu/sisinf/estante/config/ConfiguracionApp.java
// ============================================================
package edu.sisinf.estante.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centraliza los paths de la aplicacion Estante.
 *
 * <p>Todos los metodos son estaticos. Esta clase no puede ser instanciada.</p>
 *
 * <p>La aplicacion almacena sus archivos en un directorio oculto dentro del
 * home del usuario:</p>
 * <ul>
 * <li>Linux / macOS: {@code ~/.estante/}</li>
 * <li>Windows: {@code C:\Users\<usuario>\.estante\}</li>
 * </ul>
 */
public class ConfiguracionApp {

    /** Nombre del directorio oculto que usa la aplicacion dentro del home del usuario. */
    private static final String APP_DIRECTORY = ".estante";

    /** Nombre del archivo que almacena las conexiones del usuario. */
    private static final String CONNECTIONS_FILE = "conexiones.json";

    private static final String PORTABLE_FLAG = "portable.flag";

    /** Constructor privado para evitar instanciacion. */
    private ConfiguracionApp() {}

    /**
     * Devuelve el path del directorio home de la aplicacion.
     *
     * <p>El directorio se construye combinando la propiedad del sistema
     * {@code user.home} con el subdirectorio {@value #APP_DIRECTORY}.</p>
     *
     * @return {@link Path} que apunta a {@code <user.home>/.estante/}
     */
   public static Path directorioHome() {
       if (modoPortable()) {
           return directorioEjecutable().resolve(APP_DIRECTORY);
       }
        
       return Paths.get(System.getProperty("user.home"), APP_DIRECTORY);
     }
    /**
     * Devuelve el archivo de conexiones de la aplicacion.
     *
     * <p>El archivo se ubica dentro del directorio home de la aplicacion
     * con el nombre {@value #CONNECTIONS_FILE}.</p>
     *
     * @return {@link File} que apunta a {@code <user.home>/.estante/conexiones.json}
     */
    public static File archivoConexiones() {
        return directorioHome().resolve(CONNECTIONS_FILE).toFile();
    }

    /**
     * Crea el directorio home de la aplicacion si no existe.
     *
     * <p>Este metodo es idempotente: llamarlo múltiples veces no produce error
     * ni efectos secundarios si el directorio ya existe.</p>
     *
     * <p>Solo crea el directorio; no crea ningun archivo dentro de el.</p>
     */
    public static void asegurarDirectorioHome() {
        try {
            Files.createDirectories(directorioHome());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean modoPortable() {
        return Files.exists(directorioEjecutable().resolve(PORTABLE_FLAG));
    }
    
    private static Path directorioEjecutable() {
        try {
            return Paths.get(
                ConfiguracionApp.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI())
                .getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
