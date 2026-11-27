package edu.sisinf.chuspadb.dto;

import java.util.List;

/**
 * DTO que encapsula el resultado de una consulta SELECT.
 *
 * <p>Uso típico:</p>
 * <pre>
 *   List&lt;String&gt; columnas = List.of("id", "nombre", "email");
 *   List&lt;List&lt;Object&gt;&gt; filas = List.of(
 *       List.of(1, "Ana", "ana@email.com"),
 *       List.of(2, "Luis", "luis@email.com")
 *   );
 *   QueryResult resultado = new QueryResult(columnas, filas);
 *
 *   if (!resultado.isEmpty()) {
 *       System.out.println(resultado.getColumns());
 *   }
 * </pre>
 */
public class QueryResult {

    /** Nombres de las columnas del ResultSet. */
    private final List<String> columns;

    /** Filas del ResultSet, cada fila es una lista de valores. */
    private final List<List<Object>> rows;

    /**
     * Constructor privado. Usa {@link #of(List, List)} para crear instancias.
     *
     * @param columns lista de nombres de columnas
     * @param rows    lista de filas con sus valores
     */
    private QueryResult(List<String> columns, List<List<Object>> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * Crea un nuevo QueryResult con las columnas y filas indicadas.
     *
     * @param columns lista de nombres de columnas (no nula, sin elementos nulos)
     * @param rows    lista de filas (no nula, sin filas nulas); cada fila debe
     *                tener tantos valores como columnas (los valores pueden ser null)
     * @return nueva instancia de QueryResult
     * @throws IllegalArgumentException si columns o rows son nulos, si columns
     *                                  contiene elementos nulos, si hay filas
     *                                  nulas, o si alguna fila tiene un tamaño
     *                                  distinto al número de columnas
     */
    public static QueryResult of(List<String> columns, List<List<Object>> rows) {
        if (columns == null) throw new IllegalArgumentException("columns no puede ser nulo");
        if (rows == null) throw new IllegalArgumentException("rows no puede ser nulo");

        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i) == null) {
                throw new IllegalArgumentException(
                        "columns no puede contener elementos nulos (posición " + i + ")");
            }
        }

        int index = 0;
        for (List<Object> row : rows) {
            if (row == null) {
                throw new IllegalArgumentException(
                        "rows no puede contener filas nulas (fila " + index + ")");
            }
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException(
                        "La fila " + index + " tiene " + row.size()
                                + " valores pero hay " + columns.size() + " columnas");
            }
            index++;
        }

        return new QueryResult(columns, rows);
    }

    /**
     * Retorna los nombres de las columnas.
     *
     * @return lista de columnas
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Retorna las filas del resultado.
     *
     * @return lista de filas
     */
    public List<List<Object>> getRows() {
        return rows;
    }

    /**
     * Indica si el resultado no contiene filas.
     *
     * @return true si no hay filas, false en caso contrario
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
