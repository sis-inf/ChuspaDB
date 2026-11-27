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
 * Servicio para exportar resultados de consultas a archivos JSON.
 */
public class ExportadorJSON {

    private static final System.Logger LOGGER =
            System.getLogger(ExportadorJSON.class.getName());

    /**
     * Exporta un resultado de consulta a un archivo JSON.
     *
     * El archivo resultante contiene un array de objetos JSON,
     * uno por cada fila del resultado. Si el archivo ya existe,
     * se sobreescribe.
     *
     * @param resultado resultado de la consulta
     * @param archivo   archivo destino
     */
    public void exportar(ResultadoQuery resultado, File archivo) {
        if (resultado == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Intento de exportación JSON con resultado nulo");
            throw new ErrorPersistencia("El resultado de la consulta no puede ser nulo");
        }

        if (archivo == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Intento de exportación JSON con archivo destino nulo");
            throw new ErrorPersistencia("El archivo de destino no puede ser nulo");
        }

        if (resultado.getTipo() != ResultadoQuery.Tipo.LECTURA) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Tipo de resultado no válido para exportación JSON: {0}", resultado.getTipo());
            throw new ErrorPersistencia(
                    "Solo se pueden exportar resultados de lectura"
            );
        }

        List<String> columnas = resultado.getColumnas();
        List<List<Object>> filas = resultado.getFilas();

        if (columnas == null || filas == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Columnas o filas nulas en el resultado a exportar");
            throw new ErrorPersistencia("Las columnas y filas del resultado no pueden ser nulas");
        }

        LOGGER.log(System.Logger.Level.INFO,
                "Iniciando exportación JSON a {0} ({1} filas)", archivo.getPath(), filas.size());

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     new FileOutputStream(archivo),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            writer.write("[");
            writer.write("\n");

            for (int f = 0; f < filas.size(); f++) {

                List<Object> fila = filas.get(f);

                writer.write("  {");
                writer.write("\n");

                for (int c = 0; c < columnas.size(); c++) {

                    writer.write("    ");
                    writer.write(escaparCadena(columnas.get(c)));
                    writer.write(": ");
                    writer.write(serializarValor(fila.get(c)));

                    if (c < columnas.size() - 1) {
                        writer.write(",");
                    }

                    writer.write("\n");
                }

                writer.write("  }");

                if (f < filas.size() - 1) {
                    writer.write(",");
                }

                writer.write("\n");
            }

            writer.write("]");
            writer.write("\n");

            LOGGER.log(System.Logger.Level.INFO,
                    "Exportación JSON completada correctamente en {0}", archivo.getPath());

        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.ERROR,
                    "Error de E/S al exportar archivo JSON en " + archivo.getPath(), e);
            throw new ErrorPersistencia(
                    "Error al exportar archivo JSON",
                    e
            );
        }
    }

    /**
     * Serializa un valor de celda al formato JSON correspondiente.
     *
     * @param valor valor de la celda
     * @return representación JSON del valor
     */
    private String serializarValor(Object valor) {

        if (valor == null) {
            return "null";
        }

        if (valor instanceof Number) {
            if (valor instanceof Double && (((Double) valor).isNaN() || ((Double) valor).isInfinite())) {
                return "null";
            }
            if (valor instanceof Float && (((Float) valor).isNaN() || ((Float) valor).isInfinite())) {
                return "null";
            }
            return valor.toString();
        }

        if (valor instanceof Boolean) {
            return valor.toString();
        }

        return escaparCadena(valor.toString());
    }

    /**
     * Escapa una cadena de texto para que sea válida en JSON.
     *
     * @param texto texto a escapar
     * @return cadena entre comillas con caracteres especiales escapados
     */
    private String escaparCadena(String texto) {
        if (texto == null) {
            return "\"\"";
        }

        StringBuilder sb = new StringBuilder("\"");

        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }

        sb.append("\"");
        return sb.toString();
    }
}
