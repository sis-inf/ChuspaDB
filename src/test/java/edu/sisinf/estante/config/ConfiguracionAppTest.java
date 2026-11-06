package edu.sisinf.estante.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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

        assertEquals(".ChuspaDB", directorio.getFileName().toString());
        assertEquals(tmp, directorio.getParent());
    }

    @Test
    void archivoConexionesSeLlamaConexionesJsonYSuPadreEsDirectorioHome(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());

        File archivo = ConfiguracionApp.archivoConexiones();

        assertEquals("conexiones.json", archivo.getName());
        assertEquals(ConfiguracionApp.directorioHome().toFile(), archivo.getParentFile());
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
    void asegurarDirectorioHomeEsIdempotente(@TempDir Path tmp) {
        System.setProperty("user.home", tmp.toString());

        assertDoesNotThrow(ConfiguracionApp::asegurarDirectorioHome);
        assertDoesNotThrow(ConfiguracionApp::asegurarDirectorioHome);

        assertTrue(Files.isDirectory(ConfiguracionApp.directorioHome()));
    }
}
