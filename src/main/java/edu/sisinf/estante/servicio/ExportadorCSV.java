package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servicio para exportar resultados de consultas a archivos CSV.
 */
public class ExportadorCSV {

    /**
     * Exporta un resultado de consulta a un archivo CSV.
     *
     * @param resultado resultado de la consulta; no puede ser nulo
     * @param archivo archivo destino; no puede ser nulo ni un directorio
     * @throws IllegalArgumentException si algún argumento es nulo o el
     *         archivo destino es un directorio
     * @throws ErrorPersistencia si el resultado no es de lectura, si su
     *         estado es inconsistente o si ocurre un error de escritura
     */
    public void exportar(ResultadoQuery resultado, File archivo) {

        validarArgumentos(resultado, archivo);
        validarResultado(resultado);

        // Se valida todo antes de abrir el archivo para no dejar
        // un CSV a medio escribir si el resultado es inconsistente.
        List<String> columnas = resultado.getColumnas();
        List<List<Object>> filas = resultado.getFilas();
        validarEstructura(columnas, filas);

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     new FileOutputStream(archivo),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            escribirFila(writer, columnas);

            for (List<Object> fila : filas) {
                escribirFila(writer, fila);
            }

        } catch (IOException e) {
            throw new ErrorPersistencia(
                    "Error al exportar archivo CSV",
                    e
            );
        }
    }

    private void validarArgumentos(ResultadoQuery resultado, File archivo) {

        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado de la consulta no puede ser nulo"
            );
        }

        if (archivo == null) {
            throw new IllegalArgumentException(
                    "El archivo destino no puede ser nulo"
            );
        }

        if (archivo.isDirectory()) {
            throw new IllegalArgumentException(
                    "El archivo destino no puede ser un directorio: "
                            + archivo.getPath()
            );
        }
    }

    private void validarResultado(ResultadoQuery resultado) {

        if (resultado.getTipo() != ResultadoQuery.Tipo.LECTURA) {
            throw new ErrorPersistencia(
                    "Solo se pueden exportar resultados de lectura"
            );
        }
    }

    private void validarEstructura(List<String> columnas, List<List<Object>> filas) {

        if (columnas == null) {
            throw new ErrorPersistencia(
                    "El resultado de lectura no tiene columnas definidas"
            );
        }

        if (filas == null) {
            throw new ErrorPersistencia(
                    "El resultado de lectura no tiene filas definidas"
            );
        }

        for (int i = 0; i < filas.size(); i++) {

            List<Object> fila = filas.get(i);

            if (fila == null) {
                throw new ErrorPersistencia(
                        "La fila " + i + " del resultado es nula"
                );
            }

            if (fila.size() != columnas.size()) {
                throw new ErrorPersistencia(
                        "La fila " + i + " tiene " + fila.size()
                                + " celdas pero el resultado define "
                                + columnas.size() + " columnas"
                );
            }
        }
    }

    private void escribirFila(BufferedWriter writer, List<?> celdas)
            throws IOException {

        for (int i = 0; i < celdas.size(); i++) {

            writer.write(escaparCelda(celdas.get(i)));

            if (i < celdas.size() - 1) {
                writer.write(",");
            }
        }

        writer.write("\n");
    }

    private String escaparCelda(Object valor) {

        if (valor == null) {
            return "";
        }

        String texto = valor.toString();

        boolean tieneCaracteresEspeciales =
                texto.contains(",")
                        || texto.contains("\"")
                        || texto.contains("\n")
                        || texto.contains("\r");

        texto = texto.replace("\"", "\"\"");

        if (tieneCaracteresEspeciales) {
            texto = "\"" + texto + "\"";
        }

        return texto;
    }
}