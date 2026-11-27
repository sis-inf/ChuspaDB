
package edu.sisinf.chuspadb.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Optional;

public class SqlValidator {

    private static final System.Logger LOGGER =
            System.getLogger(SqlValidator.class.getName());

    public enum TipoQuery {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        CREATE,
        DROP,
        ALTER,
        DESCONOCIDO
    }

    public static TipoQuery tipo(String query) {
        LOGGER.log(System.Logger.Level.DEBUG, "Clasificando consulta SQL");

        if (query == null) {
            return TipoQuery.DESCONOCIDO;
        }

        String normalizada = normalizar(query);

        if (normalizada.isEmpty()) {
            return TipoQuery.DESCONOCIDO;
        }

        String primeraPalabra = normalizada.split("\\s+", 2)[0];

        return switch (primeraPalabra) {
            case "SELECT" -> TipoQuery.SELECT;
            case "INSERT" -> TipoQuery.INSERT;
            case "UPDATE" -> TipoQuery.UPDATE;
            case "DELETE" -> TipoQuery.DELETE;
            case "CREATE" -> TipoQuery.CREATE;
            case "DROP" -> TipoQuery.DROP;
            case "ALTER" -> TipoQuery.ALTER;
            default -> TipoQuery.DESCONOCIDO;
        };
    }

    public static boolean esDML(String query) {
        TipoQuery tipo = tipo(query);
        return tipo == TipoQuery.INSERT
                || tipo == TipoQuery.UPDATE
                || tipo == TipoQuery.DELETE;
    }

    /**
     * Determina si la consulta corresponde a una operación DML.
     *
     * @param query consulta SQL a evaluar
     * @return true si corresponde a una operación DML
     */
    public static boolean isDML(String query) {
        return esDML(query);
    }

    public static boolean esDDL(String query) {
        TipoQuery tipo = tipo(query);
        return tipo == TipoQuery.CREATE
                || tipo == TipoQuery.DROP
                || tipo == TipoQuery.ALTER;
    }

    public static boolean esLectura(String query) {
        return tipo(query) == TipoQuery.SELECT;
    }

    public static boolean esDestructiva(String query) {
        TipoQuery tipo = tipo(query);

        if (tipo == TipoQuery.DELETE || tipo == TipoQuery.DROP) {
            return true;
        }

        if (tipo == TipoQuery.UPDATE) {
            return !contieneWhere(query);
        }

        return false;
    }

    private static String normalizar(String query) {
        if (query == null) {
            return "";
        }

        String normalizada = query.strip();

        while (normalizada.startsWith("--")) {
            int finLinea = normalizada.indexOf('\n');

            if (finLinea == -1) {
                return "";
            }

            normalizada = normalizada.substring(finLinea + 1).strip();
        }

        return normalizada.toUpperCase(Locale.ROOT);
    }

    private static boolean contieneWhere(String query) {
        return normalizar(query).matches("(?s).*\\bWHERE\\b.*");
    }

    public static boolean esSentenciaValida(String query) {
        LOGGER.log(System.Logger.Level.DEBUG,
                "Validando estructura de sentencia SQL");

        if (query == null) {
            return false;
        }

        return !normalizar(query).isEmpty()
                && validarBalanceo(query).isEmpty();
    }

    /**
     * Verifica el balanceo de comillas simples y paréntesis,
     * ignorando literales y comentarios de línea.
     *
     * @param query sentencia SQL a revisar
     * @return mensaje de error o Optional.empty() si está balanceada
     */
    public static Optional<String> validarBalanceo(String query) {
        if (query == null) {
            return Optional.empty();
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Validando balanceo de la consulta SQL");

        Deque<Integer> parentesisAbiertos = new ArrayDeque<>();
        boolean dentroDeCadena = false;
        int inicioCadena = -1;

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);

            if (dentroDeCadena) {
                if (c == '\'') {
                    if (i + 1 < query.length()
                            && query.charAt(i + 1) == '\'') {
                        i++;
                    } else {
                        dentroDeCadena = false;
                    }
                }
                continue;
            }

            if (c == '-' && i + 1 < query.length()
                    && query.charAt(i + 1) == '-') {
                int finLinea = query.indexOf('\n', i);

                if (finLinea == -1) {
                    break;
                }

                i = finLinea;
                continue;
            }

            if (c == '\'') {
                dentroDeCadena = true;
                inicioCadena = i;
            } else if (c == '(') {
                parentesisAbiertos.push(i);
            } else if (c == ')') {
                if (parentesisAbiertos.isEmpty()) {
                    return Optional.of(
                            "Paréntesis de cierre sin apertura cerca de la posición "
                                    + (i + 1));
                }

                parentesisAbiertos.pop();
            }
        }

        if (dentroDeCadena) {
            return Optional.of(
                    "Literal de texto con comilla simple sin cerrar, iniciado cerca de la posición "
                            + (inicioCadena + 1));
        }

        if (!parentesisAbiertos.isEmpty()) {
            return Optional.of(
                    "Paréntesis sin cerrar cerca de la posición "
                            + (parentesisAbiertos.peek() + 1));
        }

        return Optional.empty();
    }

    // ==================== METODOS INDIVIDUALES REQUERIDOS ====================

    public static boolean isSelect(String query) {
        return esLectura(query);
    }

    public static boolean isInsert(String query) {
        return tipo(query) == TipoQuery.INSERT;
    }

    public static boolean isUpdate(String query) {
        return tipo(query) == TipoQuery.UPDATE;
    }

    public static boolean isDelete(String query) {
        return tipo(query) == TipoQuery.DELETE;
    }
}
