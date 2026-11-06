package edu.sisinf.chuspadb.modelo;

import java.util.ArrayList;
import java.util.List;

public class ResultadoQuery {

    public enum Tipo {
        LECTURA,
        ESCRITURA,
        ERROR
    }

    private Tipo tipo;
    private List<String> columnas;
    private List<List<Object>> filas;
    private int filasAfectadas;
    private long tiempoMs;
    private String mensaje;
    private int totalFilas;

    private ResultadoQuery() {
        this.columnas = new ArrayList<>();
        this.filas = new ArrayList<>();
        this.filasAfectadas = 0;
    }

    // Constructor que recibe columnas para consultas SELECT
    public ResultadoQuery(List<String> columnas) {
        this();
        this.tipo = Tipo.LECTURA;
        this.columnas = new ArrayList<>(columnas);
    }

    public static ResultadoQuery deLectura(
            List<String> columnas,
            List<List<Object>> filas,
            long tiempoMs
    ) {
        ResultadoQuery resultado = new ResultadoQuery();
        resultado.tipo = Tipo.LECTURA;
        resultado.columnas = columnas;
        if (filas == null) {
            resultado.filas = new ArrayList<>();
        } else {
            resultado.filas = new ArrayList<>(filas);
        }
        resultado.totalFilas = resultado.filas.size();
        resultado.tiempoMs = tiempoMs;
        return resultado;
    }

    public static ResultadoQuery deEscritura(
            int filasAfectadas,
            long tiempoMs
    ) {
        ResultadoQuery resultado = new ResultadoQuery();
        resultado.tipo = Tipo.ESCRITURA;
        resultado.filasAfectadas = filasAfectadas;
        resultado.tiempoMs = tiempoMs;
        return resultado;
    }

    public static ResultadoQuery deError(
            String mensaje,
            long tiempoMs
    ) {
        ResultadoQuery resultado = new ResultadoQuery();
        resultado.tipo = Tipo.ERROR;
        resultado.mensaje = mensaje;
        resultado.tiempoMs = tiempoMs;
        return resultado;
    }

    //Lanza IllegalArgumentException si el tamaño no coincide con columnas
    public void agregarFila(List<Object> fila) {
        if (fila == null || fila.size() != columnas.size()) {
            throw new IllegalArgumentException(
                "El tamaño de la fila (" + (fila == null ? "null" : fila.size()) +
                ") no coincide con el número de columnas (" + columnas.size() + ")."
            );
        }
        filas.add(new ArrayList<>(fila));
        totalFilas++;
    }
    
    //Devuelve true si no hay filas en el resultado
    public boolean estaVacio() {
        return filas.isEmpty();
    }

    public Tipo getTipo() {
        return tipo;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public List<List<Object>> getFilas() {
        return filas;
    }

    public int getFilasAfectadas() {
        return filasAfectadas;
    }

    public int getTotalFilas() {
        return totalFilas;
    }

    public long getTiempoMs() {
        return tiempoMs;
    }

    public String getMensaje() {
        return mensaje;
    }
}