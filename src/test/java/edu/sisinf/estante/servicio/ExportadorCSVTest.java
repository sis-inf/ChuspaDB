package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para ExportadorCSV.
 */
class ExportadorCSVTest {

    private final ExportadorCSV exportador = new ExportadorCSV();

    @TempDir
    Path directorioTemp;

    /**
     * Verifica que la primera línea del CSV contiene las cabeceras.
     */
    @Test
    void exportar_debeGenerarCabeceraEnPrimeraLinea() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre", "email"),
                List.of(List.of(1, "Ana", "ana@mail.com")),
                10L
        );

        File archivo = directorioTemp.resolve("test.csv").toFile();
        exportador.exportar(resultado, archivo);

        List<String> lineas = Files.readAllLines(archivo.toPath());
        assertEquals("id,nombre,email", lineas.get(0));
    }

    /**
     * Verifica que el separador usado es la coma.
     */
    @Test
    void exportar_debeUsarComoSeparador() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("col1", "col2", "col3"),
                List.of(List.of("a", "b", "c")),
                10L
        );

        File archivo = directorioTemp.resolve("test.csv").toFile();
        exportador.exportar(resultado, archivo);

        List<String> lineas = Files.readAllLines(archivo.toPath());
        String filaDatos = lineas.get(1);
        assertEquals(2, filaDatos.chars().filter(c -> c == ',').count());
    }

    /**
     * Verifica que el número de filas en el CSV coincide con el ResultadoQuery.
     */
    @Test
    void exportar_debeGenerarMismasFilasQueResultado() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(
                        List.of(1, "Ana"),
                        List.of(2, "Luis"),
                        List.of(3, "María")
                ),
                10L
        );

        File archivo = directorioTemp.resolve("test.csv").toFile();
        exportador.exportar(resultado, archivo);

        List<String> lineas = Files.readAllLines(archivo.toPath());
        // 1 cabecera + 3 filas de datos
        assertEquals(4, lineas.size());
    }

    /**
     * Verifica que el archivo es generado en la ruta indicada.
     */
    @Test
    void exportar_debeCrearArchivoEnRutaIndicada() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                List.of(List.of(1)),
                10L
        );

        File archivo = directorioTemp.resolve("salida.csv").toFile();
        exportador.exportar(resultado, archivo);

        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);
    }

    /**
     * Verifica que los valores nulos se exportan como celda vacía.
     */
    @Test
    void exportar_debeEscribirCeldaVaciaParaNull() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(List.of(1, null)),
                10L
        );

        File archivo = directorioTemp.resolve("test.csv").toFile();
        exportador.exportar(resultado, archivo);

        List<String> lineas = Files.readAllLines(archivo.toPath());
        assertEquals("1,", lineas.get(1));
    }

    /**
     * Verifica que lanza ErrorPersistencia si el resultado no es de lectura.
     */
    @Test
    void exportar_debeLanzarExcepcionSiNoEsLectura() {
        ResultadoQuery resultado = ResultadoQuery.deEscritura(5, 10L);
        File archivo = directorioTemp.resolve("test.csv").toFile();

        assertThrows(ErrorPersistencia.class, () ->
                exportador.exportar(resultado, archivo)
        );
    }
}
