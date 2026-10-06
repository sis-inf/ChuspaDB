package edu.sisinf.estante.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sisinf.estante.core.ErrorPersistencia;
import edu.sisinf.estante.dao.IConexionDAO;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.ImportacionResultado;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio para importar datos desde un archivo JSON a una tabla existente.
 *
 * El archivo debe contener un array de objetos, como el que genera
 * {@link ExportadorJSON}. Las claves del primer objeto se usan como
 * nombres de columnas. Cada objeto genera un INSERT. Si una fila falla,
 * el error se registra y la importación continúa con la siguiente.
 */
public class ImportadorJSON {

    private static final Pattern IDENTIFICADOR_SEGURO = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Importa los datos del JSON a la tabla indicada.
     *
     * @param archivoJson ruta al archivo JSON
     * @param tabla       nombre de la tabla destino
     * @param conexion    datos de conexión a la base de datos
     * @param dao         DAO para abrir la conexión JDBC
     * @return {@link ImportacionResultado} con el resumen de la operación
     */
    public ImportacionResultado importar(
            Path archivoJson,
            String tabla,
            Conexion conexion,
            IConexionDAO dao
    ) {
        int insertadas = 0;
        int fallidas = 0;
        List<String> errores = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(archivoJson, StandardCharsets.UTF_8)) {

            JsonNode raiz = mapper.readTree(reader);
            if (raiz == null || !raiz.isArray()) {
                throw new ErrorPersistencia("El archivo JSON no contiene un array de filas.");
            }
            if (raiz.isEmpty()) {
                return new ImportacionResultado(0, 0, errores);
            }
            if (!raiz.get(0).isObject()) {
                throw new ErrorPersistencia("La fila 1 del archivo JSON no es un objeto.");
            }

            List<String> columnas = new ArrayList<>();
            Iterator<String> nombres = raiz.get(0).fieldNames();
            while (nombres.hasNext()) {
                columnas.add(nombres.next());
            }
            if (columnas.isEmpty()) {
                throw new ErrorPersistencia("El archivo JSON no tiene columnas.");
            }

            String sql = construirInsert(tabla, columnas);

            try (Connection conn = dao.abrir(conexion)) {
                for (int f = 0; f < raiz.size(); f++) {
                    JsonNode fila = raiz.get(f);
                    if (!fila.isObject()) {
                        throw new ErrorPersistencia(
                                "La fila " + (f + 1) + " del archivo JSON no es un objeto.");
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        for (int i = 0; i < columnas.size(); i++) {
                            JsonNode valor = fila.get(columnas.get(i));
                            String texto = valor == null || valor.isNull() ? null : valor.asText();
                            stmt.setString(i + 1, texto);
                        }
                        stmt.executeUpdate();
                        insertadas++;
                    } catch (SQLException e) {
                        fallidas++;
                        errores.add("Fila " + (f + 1) + ": " + e.getMessage());
                    }
                }
            }

        } catch (ErrorPersistencia e) {
            throw e;
        } catch (IOException e) {
            throw new ErrorPersistencia("Error al leer el archivo JSON: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new ErrorPersistencia("Error de base de datos al importar JSON: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ErrorPersistencia("Error al importar JSON: " + e.getMessage(), e);
        }

        return new ImportacionResultado(insertadas, fallidas, errores);
    }

    /**
     * Construye la sentencia INSERT con placeholders para cada columna.
     */
    private String construirInsert(String tabla, List<String> columnas) {
        validarIdentificador(tabla, "tabla");
        for (String columna : columnas) {
            validarIdentificador(columna, "columna");
        }

        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tabla).append(" (");
        sb.append(String.join(", ", columnas));
        sb.append(") VALUES (");
        for (int i = 0; i < columnas.size(); i++) {
            sb.append("?");
            if (i < columnas.size() - 1) sb.append(", ");
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Verifica que un identificador SQL solo contenga caracteres seguros.
     */
    private void validarIdentificador(String identificador, String tipo) {
        if (identificador == null || !IDENTIFICADOR_SEGURO.matcher(identificador).matches()) {
            throw new ErrorPersistencia(
                    "Identificador de " + tipo + " inválido: " + identificador);
        }
    }
}
