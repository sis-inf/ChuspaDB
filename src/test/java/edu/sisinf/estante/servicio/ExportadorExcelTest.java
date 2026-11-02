package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios para ExportadorExcel.
 */
class ExportadorExcelTest {

    private final ExportadorExcel exportador = new ExportadorExcel();

    @TempDir
    Path directorioTemp;

    private File archivoTemp(String nombre) {
        return directorioTemp.resolve(nombre).toFile();
    }

    /**
     * Verifica que exportar un resultado de lectura crea un archivo .xlsx no vacio.
     */
    @Test
    void exportar_debeCrearArchivoXlsxNoVacio() {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(List.of(1, "Ana")),
                10L
        );

        File archivo = archivoTemp("salida.xlsx");
        exportador.exportar(resultado, archivo);

        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);
    }

    /**
     * Verifica que la hoja Resultado tiene la fila 0 con los encabezados.
     */
    @Test
    void exportar_debeEscribirEncabezadosEnLaFilaCero() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(List.of(1, "Ana")),
                10L
        );

        File archivo = archivoTemp("encabezados.xlsx");
        exportador.exportar(resultado, archivo);

        try (FileInputStream entrada = new FileInputStream(archivo);
             XSSFWorkbook workbook = new XSSFWorkbook(entrada)) {

            Sheet hoja = workbook.getSheet("Resultado");
            assertNotNull(hoja);

            Row encabezado = hoja.getRow(0);
            assertEquals("id", encabezado.getCell(0).getStringCellValue());
            assertEquals("nombre", encabezado.getCell(1).getStringCellValue());
        }
    }

    /**
     * Verifica que los valores numericos se guardan como celdas numericas.
     */
    @Test
    void exportar_debeGuardarNumerosComoCeldaNumerica() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("cantidad"),
                List.of(List.of(42)),
                10L
        );

        File archivo = archivoTemp("numeros.xlsx");
        exportador.exportar(resultado, archivo);

        try (FileInputStream entrada = new FileInputStream(archivo);
             XSSFWorkbook workbook = new XSSFWorkbook(entrada)) {

            Sheet hoja = workbook.getSheet("Resultado");
            Row fila = hoja.getRow(1);

            assertEquals(CellType.NUMERIC, fila.getCell(0).getCellType());
            assertEquals(42.0, fila.getCell(0).getNumericCellValue());
        }
    }

    /**
     * Verifica que los textos se guardan como celdas de cadena.
     */
    @Test
    void exportar_debeGuardarTextosComoCeldaDeCadena() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("nombre"),
                List.of(List.of("Ana")),
                10L
        );

        File archivo = archivoTemp("textos.xlsx");
        exportador.exportar(resultado, archivo);

        try (FileInputStream entrada = new FileInputStream(archivo);
             XSSFWorkbook workbook = new XSSFWorkbook(entrada)) {

            Sheet hoja = workbook.getSheet("Resultado");
            Row fila = hoja.getRow(1);

            assertEquals(CellType.STRING, fila.getCell(0).getCellType());
            assertEquals("Ana", fila.getCell(0).getStringCellValue());
        }
    }

    /**
     * Verifica que un valor null produce una celda vacia.
     */
    @Test
    void exportar_debeProducirCeldaVaciaParaNull() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(Arrays.asList(1, null)),
                10L
        );

        File archivo = archivoTemp("nulos.xlsx");
        exportador.exportar(resultado, archivo);

        try (FileInputStream entrada = new FileInputStream(archivo);
             XSSFWorkbook workbook = new XSSFWorkbook(entrada)) {

            Sheet hoja = workbook.getSheet("Resultado");
            Row fila = hoja.getRow(1);

            assertEquals(CellType.BLANK, fila.getCell(1).getCellType());
        }
    }

    /**
     * Verifica que un valor Boolean produce una celda booleana.
     */
    @Test
    void exportar_debeProducirCeldaBooleanaParaBoolean() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("activo"),
                List.of(List.of(true)),
                10L
        );

        File archivo = archivoTemp("booleanos.xlsx");
        exportador.exportar(resultado, archivo);

        try (FileInputStream entrada = new FileInputStream(archivo);
             XSSFWorkbook workbook = new XSSFWorkbook(entrada)) {

            Sheet hoja = workbook.getSheet("Resultado");
            Row fila = hoja.getRow(1);

            assertEquals(CellType.BOOLEAN, fila.getCell(0).getCellType());
            assertTrue(fila.getCell(0).getBooleanCellValue());
        }
    }

    /**
     * Verifica que un resultado de escritura lanza ErrorPersistencia.
     */
    @Test
    void exportar_debeLanzarErrorPersistenciaSiEsEscritura() {
        ResultadoQuery resultado = ResultadoQuery.deEscritura(5, 10L);
        File archivo = archivoTemp("escritura.xlsx");

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> exportador.exportar(resultado, archivo)
        );

        assertEquals("Solo se pueden exportar resultados de lectura", error.getMessage());
    }

    /**
     * Verifica que un resultado de error lanza ErrorPersistencia.
     */
    @Test
    void exportar_debeLanzarErrorPersistenciaSiEsError() {
        ResultadoQuery resultado = ResultadoQuery.deError("fallo", 10L);
        File archivo = archivoTemp("error.xlsx");

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> exportador.exportar(resultado, archivo)
        );

        assertEquals("Solo se pueden exportar resultados de lectura", error.getMessage());
    }
}
