package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.ImportacionResultado;

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
 * Cada registro posterior genera un INSERT. Si un registro falla,
 * el error se registra y la importación continúa con el siguiente.
 *
 * El delimitador puede indicarse (coma, punto y coma o tabulador) o detectarse
 * automáticamente a partir de la línea de encabezado. Los campos encerrados entre
 * comillas dobles pueden contener el delimitador y saltos de línea.
 */
public class ImportadorCSV {

    /** Delimitadores de campo admitidos. */
    public enum Delimitador {
        COMA(','),
        PUNTO_Y_COMA(';'),
        TABULADOR('\t');

        private final char caracter;

        Delimitador(char caracter) {
            this.caracter = caracter;
        }

        public char getCaracter() {
            return caracter;
        }
    }

    /** Registro CSV lógico: puede ocupar varias líneas físicas si hay saltos dentro de comillas. */
    private record Registro(String texto, int lineas) {}

    private static final Pattern IDENTIFICADOR_SEGURO = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /** Marca de orden de bytes (BOM) que Excel agrega al guardar CSV en UTF-8. */
    private static final String BOM_UTF8 = "\uFEFF";

    /**
     * Importa los datos del CSV a la tabla indicada, detectando el delimitador
     * automáticamente a partir de la línea de encabezado.
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
        return importar(archivoCsv, tabla, conexion, dao, null);
    }

    /**
     * Importa los datos del CSV a la tabla indicada usando el delimitador dado.
     *
     * @param archivoCsv ruta al archivo CSV
     * @param tabla      nombre de la tabla destino
     * @param conexion   datos de conexión a la base de datos
     * @param dao        DAO para abrir la conexión JDBC
     * @param delimitador delimitador a usar; si es {@code null}, se detecta a partir
     *                    de la línea de encabezado (y si no se puede deducir, se usa la coma)
     * @return {@link ImportacionResultado} con el resumen de la operación
     */
    public ImportacionResultado importar(
            Path archivoCsv,
            String tabla,
            Conexion conexion,
            IConexionDAO dao,
            Delimitador delimitador
    ) {
        int insertadas = 0;
        int fallidas = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(
                     archivoCsv, StandardCharsets.UTF_8);
             Connection conn = dao.abrir(conexion)) {

            // Primera línea: cabeceras
            Registro cabecera = leerRegistro(reader);
            if (cabecera == null || cabecera.texto().isBlank()) {
                throw new ErrorPersistencia("El archivo CSV está vacío o no tiene cabeceras.");
            }

            String textoCabecera = quitarBom(cabecera.texto());

            Delimitador usado = delimitador != null
                    ? delimitador
                    : detectarDelimitador(textoCabecera);
            char separador = usado.getCaracter();

            String[] columnas = parsearLinea(textoCabecera, separador);
            String sql = construirInsert(tabla, columnas);

            int numeroLinea = 1 + cabecera.lineas();
            Registro registro;

            while ((registro = leerRegistro(reader)) != null) {
                if (registro.texto().isBlank()) {
                    numeroLinea += registro.lineas();
                    continue;
                }

                String[] valores = parsearLinea(registro.texto(), separador);

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

                numeroLinea += registro.lineas();
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
     * Lee el siguiente registro lógico del CSV.
     *
     * Si una línea termina dentro de un campo entre comillas, se siguen leyendo líneas
     * hasta cerrar la comilla, de modo que un campo con saltos de línea queda en un solo
     * registro. Si el archivo termina con una comilla sin cerrar, se entrega lo leído.
     *
     * @return el registro, o {@code null} si no quedan más líneas
     */
    private Registro leerRegistro(BufferedReader reader) throws IOException {
        String linea = reader.readLine();
        if (linea == null) {
            return null;
        }

        StringBuilder registro = new StringBuilder(linea);
        int lineas = 1;
        boolean entreComillas = alternarComillas(linea, false);

        while (entreComillas) {
            String siguiente = reader.readLine();
            if (siguiente == null) {
                break;
            }
            registro.append('\n').append(siguiente);
            lineas++;
            entreComillas = alternarComillas(siguiente, entreComillas);
        }

        return new Registro(registro.toString(), lineas);
    }

    /**
     * Devuelve el estado "dentro de comillas" al terminar de recorrer el texto.
     * Una comilla escapada ("") alterna dos veces, por lo que no cambia el estado.
     */
    private boolean alternarComillas(String texto, boolean estadoInicial) {
        boolean entreComillas = estadoInicial;
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) == '"') {
                entreComillas = !entreComillas;
            }
        }
        return entreComillas;
    }

    /**
     * Detecta el delimitador contando cuántas veces aparece cada candidato, fuera de
     * comillas, en la línea de encabezado. Gana el más frecuente; si ninguno aparece
     * o hay empate, se conserva el primero en el orden coma, punto y coma, tabulador.
     */
    private Delimitador detectarDelimitador(String cabecera) {
        Delimitador mejor = Delimitador.COMA;
        int mayor = 0;

        for (Delimitador candidato : Delimitador.values()) {
            int cantidad = contarFueraDeComillas(cabecera, candidato.getCaracter());
            if (cantidad > mayor) {
                mayor = cantidad;
                mejor = candidato;
            }
        }

        return mejor;
    }

    /**
     * Cuenta las apariciones de un carácter que no están dentro de comillas.
     */
    private int contarFueraDeComillas(String texto, char buscado) {
        int cantidad = 0;
        boolean entreComillas = false;

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"') {
                entreComillas = !entreComillas;
            } else if (c == buscado && !entreComillas) {
                cantidad++;
            }
        }

        return cantidad;
    }

    /**
     * Quita la marca BOM del inicio del texto, si existe.
     */
    private String quitarBom(String texto) {
        return texto.startsWith(BOM_UTF8) ? texto.substring(1) : texto;
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
     * Parsea un registro CSV respetando valores entre comillas, que pueden contener
     * el delimitador y saltos de línea.
     */
    private String[] parsearLinea(String linea, char delimitador) {
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
            } else if (c == delimitador && !entreComillas) {
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
