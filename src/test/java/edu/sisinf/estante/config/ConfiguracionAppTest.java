package edu.sisinf.estante.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.sisinf.chuspadb.config.ConfiguracionApp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfiguracionAppTest {

    private String userHomeOriginal;

    @BeforeEach
    void guardarUserHomeOriginal() {
        userHomeOriginal = System.getProperty("user.home");
    }

    @AfterEach
    void restaurarUserHomeOriginal() {
        System.setProperty("user.home", userHomeOriginal);
    }

    @Test
    void directorioHomeTerminaEnChuspaDBYCuelgaDeUserHome(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());

        Path directorio = ConfiguracionApp.directorioHome();

        assertEquals(".chuspadb", directorio.getFileName().toString());
        assertEquals(tmp, directorio.getParent());
    }

    @Test
    void directorioHomeNoEsNuloYEsRutaAbsoluta(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());

        Path directorio = ConfiguracionApp.directorioHome();

        assertNotNull(directorio);
        assertTrue(directorio.isAbsolute());
    }

    @Test
    void directorioHomeAdmiteUserHomeConEspaciosEnLaRuta(@TempDir Path tmp) throws IOException {
        Path conEspacios = Files.createDirectories(tmp.resolve("usuario con espacios"));
        System.setProperty("user.home", conEspacios.toString());

        Path directorio = ConfiguracionApp.directorioHome();

        assertEquals(".chuspadb", directorio.getFileName().toString());
        assertEquals(conEspacios, directorio.getParent());
    }

    @Test
    void archivoConexionesSeLlamaConexionesJsonYSuPadreEsDirectorioHome(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());

        File archivo = ConfiguracionApp.archivoConexiones();

        assertEquals("conexiones.json", archivo.getName());
        assertEquals(ConfiguracionApp.directorioHome().toFile(), archivo.getParentFile());
    }

    @Test
    void archivoConexionesNoEsNulo(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());

        assertNotNull(ConfiguracionApp.archivoConexiones());
    }

    @Test
    void asegurarDirectorioHomeCreaElDirectorioCuandoNoExiste(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());
        Path directorio = ConfiguracionApp.directorioHome();
        assertFalse(Files.exists(directorio));

        ConfiguracionApp.asegurarDirectorioHome();

        assertTrue(Files.isDirectory(directorio));
    }

    @Test
    void asegurarDirectorioHomeNoFallaSiElDirectorioYaExiste(@TempDir Path tmp) throws IOException {
        System.setProperty("user.home", tmp.toString());
        Path directorio = ConfiguracionApp.directorioHome();
        Files.createDirectories(directorio);
        assertTrue(Files.isDirectory(directorio));

        assertDoesNotThrow(ConfiguracionApp::asegurarDirectorioHome);

        assertTrue(Files.isDirectory(directorio));
    }

    @Test
    void asegurarDirectorioHomeEsIdempotente(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());

        assertDoesNotThrow(ConfiguracionApp::asegurarDirectorioHome);
        assertDoesNotThrow(ConfiguracionApp::asegurarDirectorioHome);

        assertTrue(Files.isDirectory(ConfiguracionApp.directorioHome()));
    }
}
