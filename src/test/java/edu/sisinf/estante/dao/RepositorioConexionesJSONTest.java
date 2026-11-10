package edu.sisinf.chuspadb.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositorioConexionesJSONTest {

  @TempDir Path directorio;

  private Path archivo;
  private RepositorioConexionesJSON repositorio;

  @BeforeEach
  void prepararRepositorio() {
    archivo = directorio.resolve("conexiones.json");
    repositorio = new RepositorioConexionesJSON(archivo.toFile());
  }

  @Test
  void listarSinArchivoDevuelveListaVacia() {
    assertTrue(repositorio.listar().isEmpty());
    assertFalse(Files.exists(archivo));
  }

  @Test
  void guardarPersisteLosDatosParaOtraInstancia() {
    Conexion conexion = crearConexion("principal", "Principal");

    assertSame(conexion, repositorio.guardar(conexion));

    RepositorioConexionesJSON otro = new RepositorioConexionesJSON(archivo.toFile());
    List<Conexion> guardadas = otro.listar();
    assertTrue(Files.exists(archivo));
    assertEquals(1, guardadas.size());
    Conexion recuperada = guardadas.get(0);
    assertEquals("principal", recuperada.getId());
    assertEquals("Principal", recuperada.getNombre());
    assertEquals("localhost", recuperada.getHost());
    assertEquals(3306, recuperada.getPuerto());
    assertEquals("pruebas", recuperada.getBasedatos());
    assertEquals("usuario_prueba", recuperada.getUsuario());
    assertEquals("clave_prueba", recuperada.getPassword());
    assertEquals(TipoMotor.MYSQL, recuperada.getTipoMotor());
    assertTrue(recuperada.isUsarSSL());
    assertEquals(List.of("pruebas", "local"), recuperada.getEtiquetas());
  }

  @Test
  void guardarAsignaIdentificadorCuandoEsNull() {
    Conexion conexion = crearConexion(null, "Nueva");

    repositorio.guardar(conexion);

    assertNotNull(conexion.getId());
    assertFalse(conexion.getId().isBlank());
    assertEquals(conexion.getId(), repositorio.listar().get(0).getId());
  }

  @Test
  void guardarAsignaIdentificadorCuandoEstaEnBlanco() {
    Conexion conexion = crearConexion("   ", "Nueva");

    repositorio.guardar(conexion);

    assertNotNull(conexion.getId());
    assertFalse(conexion.getId().isBlank());
    assertEquals(conexion.getId(), repositorio.listar().get(0).getId());
  }

  @Test
  void guardarCreaLosDirectoriosPadreNecesarios() {
    Path anidado = directorio.resolve("datos").resolve("conexiones.json");
    RepositorioConexionesJSON otro = new RepositorioConexionesJSON(anidado.toFile());

    otro.guardar(crearConexion("principal", "Principal"));

    assertTrue(Files.exists(anidado));
    assertEquals(1, new RepositorioConexionesJSON(anidado.toFile()).listar().size());
  }

  @Test
  void buscarPorIdDevuelveLaConexionSolicitada() {
    repositorio.guardar(crearConexion("primera", "Primera"));
    repositorio.guardar(crearConexion("segunda", "Segunda"));

    Conexion encontrada = repositorio.buscarPorId("segunda").orElseThrow();

    assertEquals("segunda", encontrada.getId());
    assertEquals("Segunda", encontrada.getNombre());
  }

  @Test
  void buscarPorIdInexistenteDevuelveOptionalVacio() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertTrue(repositorio.buscarPorId("inexistente").isEmpty());
  }

  @Test
  void buscarPorIdNullNoLanzaNullPointerException() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    Optional<Conexion> resultado = assertDoesNotThrow(() -> repositorio.buscarPorId(null));

    assertTrue(resultado.isEmpty());
  }

  @Test
  void buscarToleraUnRegistroPersistidoConIdNull() throws IOException {
    Files.writeString(
        archivo,
        "[{\"id\":null,\"nombre\":\"Sin ID\"},"
            + "{\"id\":\"principal\",\"nombre\":\"Principal\"}]",
        StandardCharsets.UTF_8);

    Optional<Conexion> resultado = assertDoesNotThrow(() -> repositorio.buscarPorId("principal"));

    assertEquals("Principal", resultado.orElseThrow().getNombre());
  }

  @Test
  void actualizarModificaLaConexionSinDuplicarNiCambiarLasDemas() {
    Conexion primera = crearConexion("primera", "Primera");
    repositorio.guardar(primera);
    repositorio.guardar(crearConexion("segunda", "Segunda"));
    primera.setNombre("Actualizada");

    repositorio.actualizar(primera);

    RepositorioConexionesJSON otro = new RepositorioConexionesJSON(archivo.toFile());
    assertEquals(2, otro.listar().size());
    assertEquals("Actualizada", otro.buscarPorId("primera").orElseThrow().getNombre());
    assertEquals("Segunda", otro.buscarPorId("segunda").orElseThrow().getNombre());
  }

  @Test
  void actualizarIdInexistenteLanzaErrorYConservaLosDatos() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertThrows(
        ErrorPersistencia.class,
        () -> repositorio.actualizar(crearConexion("inexistente", "Otra")));

    assertEquals(1, repositorio.listar().size());
    assertEquals("Principal", repositorio.buscarPorId("principal").orElseThrow().getNombre());
  }

  @Test
  void actualizarIdNullLanzaErrorDePersistencia() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertThrows(
        ErrorPersistencia.class, () -> repositorio.actualizar(crearConexion(null, "Otra")));

    assertEquals(1, repositorio.listar().size());
  }

  @Test
  void eliminarQuitaSoloLaConexionSolicitada() {
    repositorio.guardar(crearConexion("primera", "Primera"));
    repositorio.guardar(crearConexion("segunda", "Segunda"));

    repositorio.eliminar("primera");

    RepositorioConexionesJSON otro = new RepositorioConexionesJSON(archivo.toFile());
    assertEquals(1, otro.listar().size());
    assertTrue(otro.buscarPorId("primera").isEmpty());
    assertEquals("segunda", otro.listar().get(0).getId());
  }

  @Test
  void eliminarIdInexistenteLanzaErrorYConservaLosDatos() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertThrows(ErrorPersistencia.class, () -> repositorio.eliminar("inexistente"));

    assertEquals(1, repositorio.listar().size());
  }

  @Test
  void eliminarLaUltimaConexionPersisteUnaListaVacia() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    repositorio.eliminar("principal");

    assertTrue(Files.exists(archivo));
    assertTrue(new RepositorioConexionesJSON(archivo.toFile()).listar().isEmpty());
  }

  @Test
  void eliminarIdNullLanzaErrorDePersistencia() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertThrows(ErrorPersistencia.class, () -> repositorio.eliminar(null));

    assertEquals(1, repositorio.listar().size());
  }

  @Test
  void listarJsonInvalidoLanzaErrorDePersistencia() throws IOException {
    Files.writeString(archivo, "{", StandardCharsets.UTF_8);

    assertThrows(ErrorPersistencia.class, repositorio::listar);
  }

  @Test
  void guardarEnRutaConPadreArchivoLanzaErrorDePersistencia() throws IOException {
    Path padre = directorio.resolve("no-es-directorio");
    Files.writeString(padre, "contenido original", StandardCharsets.UTF_8);
    RepositorioConexionesJSON otro =
        new RepositorioConexionesJSON(padre.resolve("conexiones.json").toFile());

    assertThrows(
        ErrorPersistencia.class, () -> otro.guardar(crearConexion("principal", "Principal")));

    assertEquals("contenido original", Files.readString(padre, StandardCharsets.UTF_8));
  }

  private Conexion crearConexion(String id, String nombre) {
    Conexion conexion =
        new Conexion("", "localhost", 3306, "pruebas", "usuario_prueba", "clave_prueba");
    conexion.setId(id);
    conexion.setNombre(nombre);
    conexion.setTipoMotor(TipoMotor.MYSQL);
    conexion.setUsarSSL(true);
    conexion.setEtiquetas(List.of("pruebas", "local"));
    return conexion;
  }
}
