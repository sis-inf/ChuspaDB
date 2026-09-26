package edu.sisinf.estante.modelo;

import java.util.List;

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
) {}