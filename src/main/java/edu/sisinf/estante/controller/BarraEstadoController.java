package edu.sisinf.chuspadb.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller de la barra de estado al pie de la ventana principal.
 * Muestra información contextual de la sesión: conexión activa,
 * tiempo de la última query, filas afectadas y un mensaje libre.
 * Es un componente puramente pasivo: solo muestra lo que se le indica.
 */
public class BarraEstadoController {

    @FXML private Label labelConexion;
    @FXML private Label labelTiempo;
    @FXML private Label labelFilas;
    @FXML private Label labelMensaje;

    /**
     * Actualiza el label de conexión activa.
     *
     * @param texto Nombre o descripción de la conexión activa.
     *              Si es {@code null}, muestra "Sin conexión".
     */
    public void setConexion(String texto) {
        labelConexion.setText(texto != null ? texto : "Sin conexión");
    }

    /**
     * Actualiza el label de tiempo con el formato "X ms".
     *
     * @param ms Tiempo en milisegundos de la última query ejecutada.
     */
    public void setTiempo(long ms) {
        labelTiempo.setText(ms + " ms");
    }

    /**
     * Actualiza el label de filas con el formato "X filas".
     *
     * @param filas Número de filas devueltas o afectadas.
     */
    public void setFilas(int filas) {
        labelFilas.setText(filas + " filas");
    }

    /**
     * Actualiza las etiquetas de filas y tiempo tras ejecutar una query.
     * Muestra "Filas: N" y "Tiempo: X ms".
     *
     * @param filas    Número de filas devueltas o afectadas.
     * @param tiempoMs Tiempo de ejecución en milisegundos.
     */
    public void actualizarEstadoQuery(int filas, long tiempoMs) {
        labelFilas.setText("Filas: " + filas);
        labelTiempo.setText("Tiempo: " + tiempoMs + " ms");
    }

    /**
     * Actualiza el label de mensaje libre.
     *
     * @param mensaje Mensaje a mostrar. Si es {@code null}, muestra texto vacío.
     */
    public void setMensaje(String mensaje) {
        labelMensaje.setText(mensaje != null ? mensaje : "");
    }

    /**
     * Limpia los labels de tiempo, filas y mensaje.
     * El label de conexión no se modifica.
     */
    public void limpiar() {
        labelTiempo.setText("");
        labelFilas.setText("");
        labelMensaje.setText("");
    }
}