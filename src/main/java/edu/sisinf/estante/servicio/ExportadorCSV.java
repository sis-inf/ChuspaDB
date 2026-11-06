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
     * @param resultado resultado de la consulta
     * @param archivo archivo destino
     */
    public void exportar(ResultadoQuery resultado, File archivo) {

        if (resultado.getTipo() != ResultadoQuery.Tipo.LECTURA) {
            throw new ErrorPersistencia(
                    "Solo se pueden exportar resultados de lectura"
            );
        }

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     new FileOutputStream(archivo),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            writer.write(
                    String.join(",", resultado.getColumnas())
            );
            writer.write("\n");

            for (List<Object> fila : resultado.getFilas()) {

                for (int i = 0; i < fila.size(); i++) {

                    writer.write(
                            escaparCelda(fila.get(i))
                    );

                    if (i < fila.size() - 1) {
                        writer.write(",");
                    }
                }

                writer.write("\n");
            }

        } catch (IOException e) {
            throw new ErrorPersistencia(
                    "Error al exportar archivo CSV",
                    e
            );
        }
    }

    private String escaparCelda(Object valor) {

        if (valor == null) {
            return "";
        }

        String texto = valor.toString();

        boolean tieneCaracteresEspeciales =
                texto.contains(",")
                        || texto.contains("\"")
                        || texto.contains("\n");

        texto = texto.replace("\"", "\"\"");

        if (tieneCaracteresEspeciales) {
            texto = "\"" + texto + "\"";
        }

        return texto;
    }
}
