// ============================================================
// ARCHIVO: ConfiguracionApp.java
// RUTA: src/main/java/edu/sisinf/chuspadb/config/ConfiguracionApp.java
// ============================================================
package edu.sisinf.chuspadb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

/**
 * Centraliza los paths de la aplicacion ChuspaDB.
 *
 * <p>Todos los metodos son estaticos. Esta clase no puede ser instanciada.</p>
 *
 * <p>La aplicacion almacena sus archivos en un directorio oculto dentro del
 * home del usuario:</p>
 * <ul>
 * <li>Linux / macOS: {@code ~/.chuspadb/}</li>
 * <li>Windows: {@code C:\Users\<usuario>\.chuspadb\}</li>
 * </ul>
 */
public class ConfiguracionApp {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionApp.class);

    /** Nombre del directorio oculto que usa la aplicacion dentro del home del usuario. */
    private static final String APP_DIRECTORY = ".chuspadb";

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
     * @return {@link Path} que apunta a {@code <user.home>/.chuspadb/}
     */
   public static Path directorioHome() {
       if (modoPortable()) {
           Path rutaPortable = directorioEjecutable().resolve(APP_DIRECTORY);
           logger.debug("Resolviendo directorioHome en modo portable: {}", rutaPortable);
           return rutaPortable;
       }
       String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            logger.error("La propiedad del sistema 'user.home' es nula o esta vacia.");
            throw new IllegalStateException("No se pudo determinar el directorio home del usuario ('user.home' no definido).");
        }

        try {
            Path rutaHome = Paths.get(userHome.trim(), APP_DIRECTORY);
            logger.debug("Resolviendo directorioHome estandar: {}", rutaHome);
            return rutaHome;
        } catch (InvalidPathException e) {
            logger.error("Ruta invalida construida a partir de user.home='{}'", userHome, e);
            throw new IllegalStateException("La ruta de 'user.home' no es valida: " + userHome, e);
        }
     }
    /**
     * Devuelve el archivo de conexiones de la aplicacion.
     *
     * <p>El archivo se ubica dentro del directorio home de la aplicacion
     * con el nombre {@value #CONNECTIONS_FILE}.</p>
     *
     * @return {@link File} que apunta a {@code <user.home>/.chuspadb/conexiones.json}
     */
    public static File archivoConexiones() {
        return resolverArchivoEnHome(CONNECTIONS_FILE).toFile();
    }
    public static Path resolverArchivoEnHome(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            logger.warn("Intento de resolver un nombre de archivo nulo o vacio en ConfiguracionApp.");
            throw new IllegalArgumentException("El nombre del archivo no puede ser nulo ni estar vacio.");
        }

        String nombreLimpio = nombreArchivo.trim();
        if (nombreLimpio.contains("..")) {
            logger.warn("Nombre de archivo rechazado por contener traversal ('..'): {}", nombreLimpio);
            throw new IllegalArgumentException("El nombre del archivo no puede contener secuencias de ruta relativa ('..').");
        }

        Path home = directorioHome().normalize();
        Path destino = home.resolve(nombreLimpio).normalize();
        if (!destino.startsWith(home)) {
            logger.warn("Ruta resuelta fuera del directorio de la aplicacion: {}", destino);
            throw new IllegalArgumentException("El archivo debe ubicarse dentro del directorio de configuracion de la aplicacion.");
        }

        logger.debug("Archivo resuelto en directorio de aplicacion: {}", destino);
        return destino;
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
        Path home = directorioHome();
        if (Files.exists(home) && !Files.isDirectory(home)) {
            logger.error("La ruta de configuracion '{}' existe pero no es un directorio.", home);
            throw new IllegalStateException("La ruta de configuracion ya existe y no es un directorio: " + home);
        }
        try {
            Files.createDirectories(home);
            logger.debug("Directorio home de la aplicacion asegurado en: {}", home);
        } catch (IOException | SecurityException e) {
            logger.error("No se pudo crear o verificar el directorio home en '{}'", home, e);
            throw new IllegalStateException("No se pudo crear el directorio de configuracion: " + home, e);
        }
    }
    private static boolean modoPortable() {
        if (!fuenteEsJar()) {
            return false;
        }
        Path flag = directorioEjecutable().resolve(PORTABLE_FLAG);
        boolean activo = Files.exists(flag);
        logger.debug("Verificacion de modo portable (flag={}): {}", flag, activo);
        return activo;
    }

    private static boolean fuenteEsJar() {
        URL ubicacion = obtenerUbicacionCodeSource();
        if (ubicacion == null) {
            logger.debug("CodeSource no disponible; asumiendo ejecucion fuera de JAR.");
            return false;
        }
        try {
            String ruta = ubicacion.toURI().toString();
            return ruta.toLowerCase().endsWith(".jar");
        } catch (URISyntaxException e) {
            logger.error("URI invalida en CodeSource de ConfiguracionApp: {}", ubicacion, e);
            throw new IllegalStateException("No se pudo interpretar la ubicacion del ejecutable.", e);
        }
    }
    
    private static Path directorioEjecutable() {
        URL ubicacion = obtenerUbicacionCodeSource();
        if (ubicacion == null) {
            logger.error("No se pudo obtener CodeSource para resolver el directorio ejecutable.");
            throw new IllegalStateException("Ubicacion del ejecutable no disponible en el entorno actual.");
        }
        try {
            Path padre = Paths.get(ubicacion.toURI()).getParent();
            if (padre == null) {
                throw new IllegalStateException("El ejecutable no tiene un directorio padre valido.");
            }
            return padre;
        } catch (URISyntaxException | IllegalArgumentException e) {
            logger.error("Error al resolver el directorio ejecutable desde '{}'", ubicacion, e);
            throw new IllegalStateException("No se pudo resolver el directorio del ejecutable.", e);
        }
    }
    private static URL obtenerUbicacionCodeSource() {
        if (ConfiguracionApp.class.getProtectionDomain() == null) {
            return null;
        }
        CodeSource codeSource = ConfiguracionApp.class.getProtectionDomain().getCodeSource();
        return (codeSource != null) ? codeSource.getLocation() : null;
    }
}
