package edu.sisinf.chuspadb.util;

public class StringUtils {

    public static String truncar(String texto, int maxLen) {
        if (esVacioONulo(texto) || maxLen <= 0) return texto;
        if (texto.length() <= maxLen) return texto;
        if (maxLen <= 3) return texto.substring(0, maxLen);
        return texto.substring(0, maxLen - 3) + "...";
    }

    public static String enmascarar(String texto) {
        if (esVacioONulo(texto)) return texto;
        return "****";
    }

    public static String capitalizar(String texto) {
        if (esVacioONulo(texto)) return texto;

        String[] partes = texto.trim().split("\\s+");
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < partes.length; i++) {
            String parte = partes[i];
            res.append(parte.substring(0, 1).toUpperCase());

            if (parte.length() > 1) {
                res.append(parte.substring(1).toLowerCase());
            }

            if (i < partes.length - 1) {
                res.append(" ");
            }
        }

        return res.toString();
    }

    public static boolean esVacioONulo(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
