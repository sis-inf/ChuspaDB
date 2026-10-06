package edu.sisinf.estante.servicio;

import edu.sisinf.estante.core.ErrorPersistencia;
import edu.sisinf.estante.modelo.ResultadoQuery;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Servicio para exportar resultados de consultas a archivos Excel (.xlsx).
 */
public class ExportadorExcel {

    /**
     * Exporta un resultado de consulta a un archivo Excel.
     *
     * @param resultado resultado de la consulta
     * @param archivo   archivo destino
     */
    public void exportar(ResultadoQuery resultado, File archivo) {

        if (resultado.getTipo() != ResultadoQuery.Tipo.LECTURA) {
            throw new ErrorPersistencia(
                    "Solo se pueden exportar resultados de lectura"
            );
        }

        List<String> columnas = resultado.getColumnas();
        List<List<Object>> filas = resultado.getFilas();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(archivo)) {

            Sheet hoja = workbook.createSheet("Resultado");

            Row filaEncabezado = hoja.createRow(0);
            for (int c = 0; c < columnas.size(); c++) {
                filaEncabezado.createCell(c).setCellValue(columnas.get(c));
            }

            for (int f = 0; f < filas.size(); f++) {
                Row fila = hoja.createRow(f + 1);
                List<Object> datosFila = filas.get(f);

                for (int c = 0; c < datosFila.size(); c++) {
                    Cell celda = fila.createCell(c);
                    escribirValor(celda, datosFila.get(c));
                }
            }

            for (int c = 0; c < columnas.size(); c++) {
                hoja.autoSizeColumn(c);
            }

            workbook.write(out);

        } catch (IOException e) {
            throw new ErrorPersistencia(
                    "Error al exportar archivo Excel",
                    e
            );
        }
    }

    private void escribirValor(Cell celda, Object valor) {

        if (valor == null) {
            celda.setBlank();
            return;
        }

        if (valor instanceof Number numero) {
            celda.setCellValue(numero.doubleValue());
            return;
        }

        if (valor instanceof Boolean booleano) {
            celda.setCellValue(booleano);
            return;
        }

        celda.setCellValue(valor.toString());
    }
}
