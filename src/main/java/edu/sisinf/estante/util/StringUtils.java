package edu.sisinf.chuspadb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringUtils {
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    private StringUtils() {
        throw new UnsupportedOperationException("StringUtils es una clase utilitaria y no debe instanciarse.");
    }

    public static String truncar(String texto, int maxLen) {
        if (texto == null) {
            logger.debug("truncar recibió texto nulo; devolviendo cadena vacía.");
            return "";
        }
        if (maxLen <= 0) {
            logger.warn("truncar recibió maxLen no positivo ({}); devolviendo texto original.", maxLen);
            return texto;
        }
        if (esVacioONulo(texto)) {
            logger.debug("truncar recibió texto vacío o solo con espacios.");
            return texto;
        }
        if (texto.length() <= maxLen) return texto;
        if (maxLen <= 3) {
            logger.debug("truncar aplicando corte directo para maxLen={}.", maxLen);
            return texto.substring(0, maxLen);
        }
        String resultado = texto.substring(0, maxLen - 3) + "...";
        logger.debug("Texto truncado correctamente a {} caracteres.", resultado.length());
        return resultado;
    }

    /**
     * Escapa los caracteres comodín de SQL LIKE ({@code %} y {@code _})
     * anteponiéndoles una barra invertida.
     *
     * @param texto Texto a escapar. Si es {@code null}, devuelve cadena vacía.
     * @return Texto con {@code %} y {@code _} escapados.
     */
    public static String escaparLike(String texto) {
        if (texto == null) {
            logger.debug("escaparLike recibió texto nulo; devolviendo cadena vacía.");
            return "";
        }
        String escapado = texto.replace("%", "\\%").replace("_", "\\_");
        logger.debug("Caracteres comodín SQL LIKE escapados correctamente.");
        return escapado;
    }
    
    public static String enmascarar(String texto) {
        if (esVacioONulo(texto)) {
            logger.debug("enmascarar recibió texto nulo o vacío; no se aplica máscara.");
            return texto;
        }
        logger.debug("Cadena sensible enmascarada correctamente.");
        return "****";
    }
    
    public static String capitalizar(String texto) {
        if (esVacioONulo(texto)) {
            logger.debug("capitalizar recibió texto nulo o vacío; devolviendo valor original.");
            return texto;
        }
        String textoLimpio = texto.trim();
        if (textoLimpio.isEmpty()) {
            throw new IllegalStateException("Estado inconsistente al normalizar texto no vacío en capitalizar.");
        }
        String[] partes = textoLimpio.split("\\s+");
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < partes.length; i++) {
            String parte = partes[i];
            if (parte.isEmpty()) continue;
            res.append(parte.substring(0, 1).toUpperCase());

            if (parte.length() > 1) {
                res.append(parte.substring(1).toLowerCase());
            }

            if (i < partes.length - 1) {
                res.append(" ");
            }
        }
        logger.debug("Texto capitalizado correctamente en {} palabra(s).", partes.length);
        return res.toString();
    }
    public static boolean esVacioONulo(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
