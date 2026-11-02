package edu.sisinf.chuspadb.controller;

import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller del panel de estadísticas de la sesión actual.
 * Muestra: total de queries, exitosas, fallidas,
 * tiempo promedio de ejecución y máximo de filas retornadas.
 * Los valores se actualizan cada vez que se llama a {@link #actualizar()}.
 */
public class PanelEstadisticasController {

    @FXML private Label labelTotalQueries;
    @FXML private Label labelExitosas;
    @FXML private Label labelFallidas;
    @FXML private Label labelTiempoPromedio;
    @FXML private Label labelMaxFilas;

    /** Historial de resultados de queries de la sesión actual. */
    private final List<ResultadoQuery> historial = new ArrayList<>();

    /**
     * Agrega un resultado al historial de la sesión
     * y actualiza las estadísticas en pantalla.
     *
     * @param resultado resultado de la query ejecutada.
     */
    public void agregarResultado(ResultadoQuery resultado) {
        historial.add(resultado);
        actualizar();
    }

    /**
     * Actualiza los labels con las estadísticas calculadas
     * a partir del historial de queries de la sesión.
     * Se invoca cada vez que se abre el panel o se agrega un resultado.
     */
    public void actualizar() {
        int total    = historial.size();
        int exitosas = 0;
        int fallidas = 0;
        long sumaMs  = 0;
        int maxFilas = 0;

        for (ResultadoQuery r : historial) {
            if (r.getTipo() == ResultadoQuery.Tipo.ERROR) {
                fallidas++;
            } else {
                exitosas++;
            }

            sumaMs += r.getTiempoMs();

            int filas = r.getTipo() == ResultadoQuery.Tipo.LECTURA
                    ? r.getTotalFilas()
                    : r.getFilasAfectadas();

            if (filas > maxFilas) {
                maxFilas = filas;
            }
        }

        long promedio = total > 0 ? sumaMs / total : 0;

        labelTotalQueries.setText(String.valueOf(total));
        labelExitosas.setText(String.valueOf(exitosas));
        labelFallidas.setText(String.valueOf(fallidas));
        labelTiempoPromedio.setText(promedio + " ms");
        labelMaxFilas.setText(String.valueOf(maxFilas));
    }

    /**
     * Limpia el historial de la sesión y reinicia las estadísticas.
     */
    public void limpiar() {
        historial.clear();
        actualizar();
    }
}
