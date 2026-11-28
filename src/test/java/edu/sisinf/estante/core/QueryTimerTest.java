package edu.sisinf.estante.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.sisinf.chuspadb.core.QueryTimer;
import org.junit.jupiter.api.Test;

class QueryTimerTest {

  @Test
  void constructorSinNombreUsaNombrePorDefecto() {
    QueryTimer timer = new QueryTimer();

    assertNotNull(timer);
  }

  @Test
  void constructorConNombreNuloLanzaNullPointerException() {
    assertThrows(NullPointerException.class, () -> new QueryTimer(null));
  }

  @Test
  void constructorConNombreVacioLanzaIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new QueryTimer(""));
  }

  @Test
  void constructorConNombreEnBlancoLanzaIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new QueryTimer("   "));
  }

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
  void iniciarDosVecesSinDetenerLanzaIllegalStateException() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();

    assertThrows(IllegalStateException.class, timer::iniciar);
  }

  @Test
  void detenerDosVecesLanzaIllegalStateException() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();
    timer.detener();

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

    assertTrue(
        timer.getTiempoFormateado().matches("\\d+ ms|\\d+[.,]\\d s"));
  }

  @Test
  void getTiempoFormateadoNuncaDevuelveNulo() {
    QueryTimer timer = new QueryTimer();
    timer.iniciar();
    timer.detener();

    assertNotNull(timer.getTiempoFormateado());
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
