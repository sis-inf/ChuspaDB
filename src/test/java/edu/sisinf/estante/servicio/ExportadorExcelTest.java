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
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    private XSSFWorkbook leer(File archivo) throws IOException {
        try (FileInputStream entrada = new FileInputStream(archivo)) {
            return new XSSFWorkbook(entrada);
        }
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

    /**
     * Verifica que un resultado nulo lanza NullPointerException.
     */
    @Test
    void exportar_debeLanzarNullPointerExceptionSiElResultadoEsNulo() {
        File archivo = archivoTemp("resultado-nulo.xlsx");

        assertThrows(
                NullPointerException.class,
                () -> exportador.exportar(null, archivo)
        );
    }

    /**
     * Verifica que un archivo destino nulo lanza NullPointerException.
     */
    @Test
    void exportar_debeLanzarNullPointerExceptionSiElArchivoEsNulo() {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                List.of(List.of(1)),
                10L
        );

        assertThrows(
                NullPointerException.class,
                () -> exportador.exportar(resultado, null)
        );
    }

    /**
     * Verifica que un destino que es un directorio lanza ErrorPersistencia.
     */
    @Test
    void exportar_debeLanzarErrorPersistenciaSiElDestinoEsUnDirectorio() {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                List.of(List.of(1)),
                10L
        );
        File directorio = directorioTemp.toFile();

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> exportador.exportar(resultado, directorio)
        );

        assertEquals("Error al exportar archivo Excel", error.getMessage());
        assertNotNull(error.getCause());
    }

    /**
     * Verifica que una carpeta destino inexistente lanza ErrorPersistencia.
     */
    @Test
    void exportar_debeLanzarErrorPersistenciaSiLaCarpetaDestinoNoExiste() {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                List.of(List.of(1)),
                10L
        );
        File archivo = directorioTemp.resolve("no-existe").resolve("salida.xlsx").toFile();

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> exportador.exportar(resultado, archivo)
        );

        assertEquals("Error al exportar archivo Excel", error.getMessage());
        assertFalse(archivo.exists());
    }

    /**
     * Verifica que un resultado con solo encabezados no escribe filas de datos.
     */
    @Test
    void exportar_conSoloEncabezadosNoEscribeFilasDeDatos() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(),
                10L
        );

        File archivo = archivoTemp("solo-encabezados.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Sheet hoja = workbook.getSheet("Resultado");

            assertEquals(0, hoja.getLastRowNum());
            assertEquals("id", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("nombre", hoja.getRow(0).getCell(1).getStringCellValue());
        }
    }

    /**
     * Verifica que una lista de filas nula se trata como un resultado sin filas.
     */
    @Test
    void exportar_debeTratarFilasNulasComoSinFilas() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                null,
                10L
        );

        File archivo = archivoTemp("filas-nulas.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Sheet hoja = workbook.getSheet("Resultado");

            assertEquals(0, hoja.getLastRowNum());
            assertEquals("id", hoja.getRow(0).getCell(0).getStringCellValue());
        }
    }

    /**
     * Verifica que el valor false produce una celda booleana con valor falso.
     */
    @Test
    void exportar_debeProducirCeldaBooleanaParaFalse() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("activo"),
                List.of(List.of(false)),
                10L
        );

        File archivo = archivoTemp("booleano-falso.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Row fila = workbook.getSheet("Resultado").getRow(1);

            assertEquals(CellType.BOOLEAN, fila.getCell(0).getCellType());
            assertFalse(fila.getCell(0).getBooleanCellValue());
        }
    }

    /**
     * Verifica que decimales, Long y BigDecimal se guardan como celdas numericas.
     */
    @Test
    void exportar_debeGuardarDecimalesLongYBigDecimalComoNumericos() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("decimal", "entero", "exacto"),
                List.of(Arrays.asList(3.5, 10L, new BigDecimal("2.25"))),
                10L
        );

        File archivo = archivoTemp("numericos.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Row fila = workbook.getSheet("Resultado").getRow(1);

            assertEquals(CellType.NUMERIC, fila.getCell(0).getCellType());
            assertEquals(3.5, fila.getCell(0).getNumericCellValue());
            assertEquals(CellType.NUMERIC, fila.getCell(1).getCellType());
            assertEquals(10.0, fila.getCell(1).getNumericCellValue());
            assertEquals(CellType.NUMERIC, fila.getCell(2).getCellType());
            assertEquals(2.25, fila.getCell(2).getNumericCellValue());
        }
    }

    /**
     * Verifica que un tipo no reconocido se guarda como texto usando toString.
     */
    @Test
    void exportar_debeGuardarTiposNoReconocidosComoTexto() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("fecha"),
                List.of(List.of(LocalDate.of(2026, 11, 16))),
                10L
        );

        File archivo = archivoTemp("fechas.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Row fila = workbook.getSheet("Resultado").getRow(1);

            assertEquals(CellType.STRING, fila.getCell(0).getCellType());
            assertEquals("2026-11-16", fila.getCell(0).getStringCellValue());
        }
    }

    /**
     * Verifica que tildes y simbolos especiales se conservan al leer el archivo.
     */
    @Test
    void exportar_debePreservarTildesYSimbolosEspeciales() throws IOException {
        String texto = "Peña & Cía <ñandú>";
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("nombre"),
                List.of(List.of(texto)),
                10L
        );

        File archivo = archivoTemp("especiales.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Row fila = workbook.getSheet("Resultado").getRow(1);

            assertEquals(texto, fila.getCell(0).getStringCellValue());
        }
    }

    /**
     * Verifica que una fila con mas valores que columnas escribe todas sus celdas.
     */
    @Test
    void exportar_debeEscribirTodasLasCeldasDeUnaFilaMasLargaQueLasColumnas() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                List.of(List.of(1, "extra")),
                10L
        );

        File archivo = archivoTemp("fila-larga.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Sheet hoja = workbook.getSheet("Resultado");

            assertNull(hoja.getRow(0).getCell(1));
            assertEquals("extra", hoja.getRow(1).getCell(1).getStringCellValue());
        }
    }

    /**
     * Verifica que una fila con menos valores que columnas no crea las celdas faltantes.
     */
    @Test
    void exportar_noDebeCrearCeldasFaltantesEnUnaFilaMasCorta() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(List.of(1)),
                10L
        );

        File archivo = archivoTemp("fila-corta.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Row fila = workbook.getSheet("Resultado").getRow(1);

            assertEquals(1.0, fila.getCell(0).getNumericCellValue());
            assertNull(fila.getCell(1));
        }
    }

    /**
     * Verifica que el libro tiene una unica hoja llamada Resultado.
     */
    @Test
    void exportar_debeCrearUnaUnicaHojaLlamadaResultado() throws IOException {
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id"),
                List.of(List.of(1)),
                10L
        );

        File archivo = archivoTemp("una-hoja.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("Resultado", workbook.getSheetName(0));
        }
    }

    /**
     * Verifica que exportar sobre un archivo existente reemplaza su contenido.
     */
    @Test
    void exportar_debeSobrescribirUnArchivoExistente() throws IOException {
        File archivo = archivoTemp("sobrescrito.xlsx");

        exportador.exportar(
                ResultadoQuery.deLectura(
                        List.of("valor"),
                        List.of(List.of("uno"), List.of("dos"), List.of("tres")),
                        10L),
                archivo);
        exportador.exportar(
                ResultadoQuery.deLectura(
                        List.of("valor"),
                        List.of(List.of("solo")),
                        10L),
                archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Sheet hoja = workbook.getSheet("Resultado");

            assertEquals(1, hoja.getLastRowNum());
            assertEquals("solo", hoja.getRow(1).getCell(0).getStringCellValue());
        }
    }

    /**
     * Verifica que se exporta un resultado construido con agregarFila.
     */
    @Test
    void exportar_debeExportarResultadoCreadoConAgregarFila() throws IOException {
        ResultadoQuery resultado = new ResultadoQuery(List.of("id", "nombre"));
        resultado.agregarFila(List.of(7, "Eva"));

        File archivo = archivoTemp("agregar-fila.xlsx");
        exportador.exportar(resultado, archivo);

        try (XSSFWorkbook workbook = leer(archivo)) {
            Row fila = workbook.getSheet("Resultado").getRow(1);

            assertEquals(7.0, fila.getCell(0).getNumericCellValue());
            assertEquals("Eva", fila.getCell(1).getStringCellValue());
        }
    }
}
