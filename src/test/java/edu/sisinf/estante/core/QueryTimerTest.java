package edu.sisinf.estante.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QueryTimerTest {

  @Test
  void getTiempoMsSinIniciarLanzaIllegalStateException() {
    QueryTimer timer = new QueryTimer();

    assertThrows(IllegalStateException.class, timer::getTiempoMs);
  }

  @Test
  void getTiempoMsSinDetenerLanzaIllegalStateException() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();

    assertThrows(IllegalStateException.class, timer::getTiempoMs);
  }

  @Test
  void detenerSinIniciarLanzaIllegalStateException() {
    QueryTimer timer = new QueryTimer();

    assertThrows(IllegalStateException.class, timer::detener);
  }

  @Test
  void medicionCompletaDevuelveTiempoNoNegativo() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();
    timer.detener();

    assertTrue(timer.getTiempoMs() >= 0.0);
  }

  @Test
  void getTiempoFormateadoIncluyeValorYUnidad() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();
    timer.detener();

    assertTrue(timer.getTiempoFormateado().matches("\\d+ ms|\\d+[.,]\\d s"));
  }

  @Test
  void getTiempoFormateadoSinIniciarLanzaIllegalStateException() {
    QueryTimer timer = new QueryTimer();

    assertThrows(IllegalStateException.class, timer::getTiempoFormateado);
  }

  @Test
  void getTiempoFormateadoSinDetenerLanzaIllegalStateException() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();

    assertThrows(IllegalStateException.class, timer::getTiempoFormateado);
  }

  @Test
  void reiniciarDescartaElFinalDeLaMedicionAnterior() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();
    timer.detener();

    timer.iniciar();

    assertThrows(IllegalStateException.class, timer::getTiempoMs);

    timer.detener();

    assertTrue(timer.getTiempoMs() >= 0.0);
  }
}
