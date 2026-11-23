
package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

/**
 * Tests unitarios para ExportadorCSV.
 */
class ExportadorCSVTest {

    private final ExportadorCSV exportador = new ExportadorCSV();

    @TempDir
    Path directorioTemp;

    /**
     * Verifica que el archivo CSV generado contiene cabeceras y filas exactas.
     */
    @Test
    void exportar_debeGenerarContenidoCsvExacto() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(
                        List.of(1, "Ana"),
                        List.of(2, "Luis")
                ),
                10L
        );

        File archivo = directorioTemp.resolve("test.csv").toFile();
        exportador.exportar(resultado, archivo);

        String contenido = Files.readString(
                archivo.toPath(),
                StandardCharsets.UTF_8
        );

        assertEquals("id,nombre\n1,Ana\n2,Luis\n", contenido);
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

        String contenido = Files.readString(
                archivo.toPath(),
                StandardCharsets.UTF_8
        );

        assertEquals("nombre\n\"Ana, María\"\n", contenido);
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

        String contenido = Files.readString(
                archivo.toPath(),
                StandardCharsets.UTF_8
        );

        assertEquals("comentario\n\"Dijo \"\"hola\"\"\"\n", contenido);
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

        String contenido = Files.readString(
                archivo.toPath(),
                StandardCharsets.UTF_8
        );

        assertEquals(
                "descripcion\n\"Primera línea\nSegunda línea\"\n",
                contenido
        );
    }

    /**
     * Verifica que una celda con CRLF conserva ambos caracteres dentro de comillas.
     */
    @Test
    void exportar_debeEscaparSaltosDeLineaCrlf() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("descripcion"),
                List.of(List.of("Primera línea\r\nSegunda línea")),
                10L
        );

        File archivo = directorioTemp.resolve("saltos-crlf.csv").toFile();
        exportador.exportar(resultado, archivo);

        String contenido = Files.readString(
                archivo.toPath(),
                StandardCharsets.UTF_8
        );

        assertEquals(
                "descripcion\n\"Primera línea\r\nSegunda línea\"\n",
                contenido
        );
    }

    /**
     * Verifica el escape combinado de comas, comillas y saltos de línea.
     */
    @Test
    void exportar_debeEscaparCaracteresEspecialesCombinados() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("descripcion"),
                List.of(List.of("Parte 1, \"citada\"\nParte 2")),
                10L
        );

        File archivo = directorioTemp.resolve("combinados.csv").toFile();
        exportador.exportar(resultado, archivo);

        String contenido = Files.readString(
                archivo.toPath(),
                StandardCharsets.UTF_8
        );

        assertEquals(
                "descripcion\n\"Parte 1, \"\"citada\"\"\nParte 2\"\n",
                contenido
        );
    }

    /**
     * Verifica que lanza ErrorPersistencia si el resultado no es de lectura.
     */
    @Test
    void exportar_debeLanzarExcepcionSiNoEsLectura() {
        ResultadoQuery resultado = ResultadoQuery.deEscritura(5, 10L);
        File archivo = directorioTemp.resolve("test.csv").toFile();

        ErrorPersistencia error = assertThrows(ErrorPersistencia.class, () ->
                exportador.exportar(resultado, archivo)
        );

        assertEquals(
                "Solo se pueden exportar resultados de lectura",
                error.getMessage()
        );
        assertFalse(archivo.exists());
    }

    /**
     * Verifica que un fallo de E/S se envuelve en ErrorPersistencia.
     */
    @Test
    void exportar_debeEnvolverFalloDeEntradaSalida() {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                List.of(List.of(1)),
                10L
        );
        File archivo = directorioTemp.resolve("fallo.csv").toFile();
        IOException causa = new IOException("Fallo simulado de escritura");

        try (MockedConstruction<FileOutputStream> construccion =
                     mockConstruction(
                             FileOutputStream.class,
                             (stream, context) -> {
                                 doThrow(causa).when(stream).write(
                                         any(byte[].class),
                                         anyInt(),
                                         anyInt()
                                 );
                             })) {
            ErrorPersistencia error = assertThrows(
                    ErrorPersistencia.class,
                    () -> exportador.exportar(resultado, archivo)
            );

            assertEquals("Error al exportar archivo CSV", error.getMessage());
            assertSame(causa, error.getCause());
            assertEquals(1, construccion.constructed().size());
        }
    }
}
