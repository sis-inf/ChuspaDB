
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
     * Verifica que el número de filas coincide con el ResultadoQuery.
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
        assertEquals(4, lineas.size());
    }

    /**
     * Verifica que el archivo se genera en la ruta indicada.
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
                List.of(Arrays.asList(1, null)),
                10L
        );

        File archivo = directorioTemp.resolve("test.csv").toFile();
        exportador.exportar(resultado, archivo);

        List<String> lineas = Files.readAllLines(archivo.toPath());
        assertEquals("1,", lineas.get(1));
    }

    /**
     * Verifica que las celdas con comas se envuelven entre comillas dobles.
     */
    @Test
    void exportar_debeEscaparValoresConComas() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("nombre"),
                List.of(List.of("Ana, María")),
                10L
        );

        File archivo = directorioTemp.resolve("comas.csv").toFile();
        exportador.exportar(resultado, archivo);

        String contenido = Files.readString(archivo.toPath());

        assertTrue(contenido.contains("\"Ana, María\""));
    }

    /**
     * Verifica que las comillas dobles internas se duplican según RFC 4180.
     */
    @Test
    void exportar_debeEscaparComillasDobles() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("comentario"),
                List.of(List.of("Dijo \"hola\"")),
                10L
        );

        File archivo = directorioTemp.resolve("comillas.csv").toFile();
        exportador.exportar(resultado, archivo);

        String contenido = Files.readString(archivo.toPath());

        assertTrue(contenido.contains("\"Dijo \"\"hola\"\"\""));
    }

    /**
     * Verifica que los saltos de línea internos se conservan entre comillas.
     */
    @Test
    void exportar_debeEscaparSaltosDeLinea() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("descripcion"),
                List.of(List.of("Primera línea\nSegunda línea")),
                10L
        );

        File archivo = directorioTemp.resolve("saltos.csv").toFile();
        exportador.exportar(resultado, archivo);

        String contenido = Files.readString(archivo.toPath());

        assertTrue(contenido.contains(
                "\"Primera línea\nSegunda línea\""
        ));
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
