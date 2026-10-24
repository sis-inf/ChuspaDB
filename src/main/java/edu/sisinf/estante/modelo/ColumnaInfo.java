package edu.sisinf.chuspadb.modelo;

public record ColumnaInfo(
        String nombre,
        String tipoSQL,
        boolean nullable,
        Integer tamano,
        String valorDefault,
        boolean esPrimaryKey
) {}
