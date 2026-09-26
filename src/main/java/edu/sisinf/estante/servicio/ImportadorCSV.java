package edu.sisinf.estante.servicio;

import edu.sisinf.estante.core.ErrorPersistencia;
import edu.sisinf.estante.dao.IConexionDAO;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.ImportacionResultado;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio para importar datos desde un archivo CSV a una tabla existente.
 *
 * La primera línea del CSV se usa como nombres de columnas.
 * Cada fila posterior genera un INSERT. Si una fila falla,
 * el error se registra y la importación continúa con la siguiente.
 */
public class ImportadorCSV {

    private static final Pattern IDENTIFICADOR_SEGURO = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * Importa los datos del CSV a la tabla indicada.
     *
     * @param archivoCsv ruta al archivo CSV
     * @param tabla      nombre de la tabla destino
     * @param conexion   datos de conexión a la base de datos
     * @param dao        DAO para abrir la conexión JDBC
     * @return {@link ImportacionResultado} con el resumen de la operación
     */
    public ImportacionResultado importar(
            Path archivoCsv,
            String tabla,
            Conexion conexion,
            IConexionDAO dao
    ) {
        int insertadas = 0;
        int fallidas = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(
                     archivoCsv, StandardCharsets.UTF_8);
             Connection conn = dao.abrir(conexion)) {

            // Primera línea: cabeceras
            String lineaCabecera = reader.readLine();
            if (lineaCabecera == null || lineaCabecera.isBlank()) {
                throw new ErrorPersistencia("El archivo CSV está vacío o no tiene cabeceras.");
            }

            String[] columnas = parsearLinea(lineaCabecera);
            String sql = construirInsert(tabla, columnas);

            String linea;
            int numeroLinea = 2;

            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) {
                    numeroLinea++;
                    continue;
                }

                String[] valores = parsearLinea(linea);

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < columnas.length; i++) {
                        String valor = i < valores.length ? valores[i] : null;
                        stmt.setString(i + 1, valor);
                    }
                    stmt.executeUpdate();
                    insertadas++;
                } catch (SQLException e) {
                    fallidas++;
                    errores.add("Línea " + numeroLinea + ": " + e.getMessage());
                }

                numeroLinea++;
            }

        } catch (ErrorPersistencia e) {
            throw e;
        } catch (IOException e) {
            throw new ErrorPersistencia("Error al leer el archivo CSV: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new ErrorPersistencia("Error de base de datos al importar CSV: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ErrorPersistencia("Error al importar CSV: " + e.getMessage(), e);
        }

        return new ImportacionResultado(insertadas, fallidas, errores);
    }

    /**
     * Construye la sentencia INSERT con placeholders para cada columna.
     */
    private String construirInsert(String tabla, String[] columnas) {
        validarIdentificador(tabla, "tabla");
        for (String columna : columnas) {
            validarIdentificador(columna, "columna");
        }

        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tabla).append(" (");

        for (int i = 0; i < columnas.length; i++) {
            sb.append(columnas[i]);
            if (i < columnas.length - 1) sb.append(", ");
        }

        sb.append(") VALUES (");
        for (int i = 0; i < columnas.length; i++) {
            sb.append("?");
            if (i < columnas.length - 1) sb.append(", ");
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

    /**
     * Parsea una línea CSV respetando valores entre comillas.
     */
    private String[] parsearLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campo.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (c == ',' && !entreComillas) {
                campos.add(campo.toString().trim());
                campo.setLength(0);
            } else {
                campo.append(c);
            }
        }

        campos.add(campo.toString().trim());
        return campos.toArray(new String[0]);
    }
}