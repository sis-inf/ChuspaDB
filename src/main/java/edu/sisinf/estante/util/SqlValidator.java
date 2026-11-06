package edu.sisinf.chuspadb.util;

import java.util.Locale;

public class SqlValidator {


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
   * <p>Este método se mantiene como un alias de {@link #esDML(String)}
   * para preservar la compatibilidad con el código existente.</p>
   *
   * @param query consulta SQL a evaluar
   * @return {@code true} si la consulta corresponde a una operación DML;
   *         {@code false} en caso contrario
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
    if (query == null) {
        return false;
    }

    return !normalizar(query).isEmpty();
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
