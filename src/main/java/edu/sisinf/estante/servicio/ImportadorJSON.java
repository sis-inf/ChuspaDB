package edu.sisinf.chuspadb.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.ImportacionResultado;

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
 * El archivo debe contener un array no vacío de objetos, como el que genera
 * {@link ExportadorJSON}. Esa estructura se valida por completo antes de abrir
 * la conexión o insertar nada. Las claves del primer objeto se usan como
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
     * @throws ErrorPersistencia si el JSON no es un array no vacío de objetos
     *                           (el mensaje indica qué se encontró) o si falla la lectura
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

            // Se valida toda la estructura antes de tocar la base de datos
            validarEstructura(raiz);

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
     * Valida que el JSON tenga estructura tabular: un array no vacío cuyos
     * elementos sean todos objetos.
     *
     * @param raiz nodo raíz leído del archivo
     * @throws ErrorPersistencia con un mensaje que indica qué se encontró en lugar
     *                           de la estructura esperada
     */
    private void validarEstructura(JsonNode raiz) {
        if (raiz == null || raiz.isMissingNode()) {
            throw new ErrorPersistencia(
                    "El archivo JSON está vacío: se esperaba un array de objetos.");
        }
        if (!raiz.isArray()) {
            throw new ErrorPersistencia(
                    "La raíz del archivo JSON debe ser un array de objetos, pero se encontró "
                            + describirTipo(raiz) + ".");
        }
        if (raiz.isEmpty()) {
            throw new ErrorPersistencia(
                    "El array del archivo JSON está vacío: debe contener al menos un objeto.");
        }

        for (int f = 0; f < raiz.size(); f++) {
            JsonNode fila = raiz.get(f);
            if (!fila.isObject()) {
                throw new ErrorPersistencia(
                        "La fila " + (f + 1) + " del archivo JSON debe ser un objeto, pero se encontró "
                                + describirTipo(fila) + ".");
            }
        }
    }

    /**
     * Describe en español el tipo de un nodo JSON, para los mensajes de error.
     */
    private String describirTipo(JsonNode nodo) {
        if (nodo.isObject())  return "un objeto";
        if (nodo.isTextual()) return "una cadena de texto";
        if (nodo.isNumber())  return "un número";
        if (nodo.isBoolean()) return "un valor booleano";
        if (nodo.isNull())    return "null";
        return "un valor de tipo desconocido";
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
