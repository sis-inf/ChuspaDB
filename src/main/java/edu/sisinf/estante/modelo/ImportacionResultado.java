package edu.sisinf.chuspadb.modelo;

import java.util.List;
import java.util.Objects;

/**
 * Resultado de una operación de importación desde CSV.
 *
 * @param filasInsertadas número de filas insertadas exitosamente
 * @param filasFallidas  número de filas que fallaron
 * @param errores         lista de mensajes de error por fila fallida
 */
public record ImportacionResultado(
        int filasInsertadas,
        int filasFallidas,
        List<String> errores
) {

    /**
     * Valida los invariantes y copia la lista de errores de forma inmutable.
     *
     * @throws NullPointerException     si errores es nulo o contiene elementos nulos
     * @throws IllegalArgumentException si algún contador es negativo
     */
    public ImportacionResultado {
        Objects.requireNonNull(errores, "errores no puede ser nulo");
        if (filasInsertadas < 0) {
            throw new IllegalArgumentException(
                    "filasInsertadas no puede ser negativo: " + filasInsertadas);
        }
        if (filasFallidas < 0) {
            throw new IllegalArgumentException(
                    "filasFallidas no puede ser negativo: " + filasFallidas);
        }
        errores = List.copyOf(errores);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportacionResultado)) {
            return false;
        }
        ImportacionResultado otro = (ImportacionResultado) o;
        return filasInsertadas == otro.filasInsertadas
                && filasFallidas == otro.filasFallidas
                && errores.equals(otro.errores);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filasInsertadas, filasFallidas, errores);
    }

    @Override
    public String toString() {
        return "ImportacionResultado{"
                + "filasInsertadas=" + filasInsertadas
                + ", filasFallidas=" + filasFallidas
                + ", errores=" + errores
                + '}';
    }
}
