package edu.sisinf.chuspadb.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import java.util.UUID;
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

  @Test
  void guardarConexionNullLanzaNullPointerException() {
    assertThrows(NullPointerException.class, () -> repositorio.guardar(null));

    assertFalse(Files.exists(archivo));
  }

  @Test
  void actualizarConexionNullLanzaNullPointerException() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertThrows(NullPointerException.class, () -> repositorio.actualizar(null));

    assertEquals(1, repositorio.listar().size());
  }

  @Test
  void buscarPorIdCadenaVaciaDevuelveOptionalVacio() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertTrue(repositorio.buscarPorId("").isEmpty());
  }

  @Test
  void buscarPorIdDistingueMayusculasYMinusculas() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertTrue(repositorio.buscarPorId("PRINCIPAL").isEmpty());
    assertTrue(repositorio.buscarPorId("principal").isPresent());
  }

  @Test
  void eliminarIdCadenaVaciaLanzaErrorYConservaLosDatos() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertThrows(ErrorPersistencia.class, () -> repositorio.eliminar(""));

    assertEquals(1, repositorio.listar().size());
  }

  @Test
  void guardarSinIdentificadorGeneraUnUuidValido() {
    Conexion conexion = crearConexion(null, "Nueva");

    repositorio.guardar(conexion);

    assertDoesNotThrow(() -> UUID.fromString(conexion.getId()));
  }

  @Test
  void guardarDosConexionesSinIdentificadorGeneraIdentificadoresDistintos() {
    Conexion primera = crearConexion(null, "Primera");
    Conexion segunda = crearConexion(null, "Segunda");

    repositorio.guardar(primera);
    repositorio.guardar(segunda);

    assertNotEquals(primera.getId(), segunda.getId());
    assertEquals(2, repositorio.listar().size());
  }

  @Test
  void listarArchivoVacioLanzaErrorDePersistencia() throws IOException {
    Files.writeString(archivo, "", StandardCharsets.UTF_8);

    assertThrows(ErrorPersistencia.class, repositorio::listar);
  }

  @Test
  void listarJsonObjetoEnLugarDeArregloLanzaErrorDePersistencia() throws IOException {
    Files.writeString(archivo, "{\"id\":\"principal\"}", StandardCharsets.UTF_8);

    assertThrows(ErrorPersistencia.class, repositorio::listar);
  }

  @Test
  void listarArregloVacioDevuelveListaVacia() throws IOException {
    Files.writeString(archivo, "[]", StandardCharsets.UTF_8);

    assertNotNull(repositorio.listar());
    assertTrue(repositorio.listar().isEmpty());
  }

  @Test
  void listarConRutaDeDirectorioLanzaErrorDePersistencia() {
    RepositorioConexionesJSON otro = new RepositorioConexionesJSON(directorio.toFile());

    assertThrows(ErrorPersistencia.class, otro::listar);
  }

  @Test
  void listarJsonInvalidoConservaMensajeYCausaOriginal() throws IOException {
    Files.writeString(archivo, "{", StandardCharsets.UTF_8);

    ErrorPersistencia error = assertThrows(ErrorPersistencia.class, repositorio::listar);

    assertEquals("Error al leer conexiones", error.getMessage());
    assertNotNull(error.getCause());
  }

  @Test
  void listarDevuelveUnaCopiaIndependienteDelArchivo() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    List<Conexion> copia = repositorio.listar();
    copia.clear();

    assertEquals(1, repositorio.listar().size());
  }

  @Test
  void guardarYRecuperarConservaCaracteresEspeciales() {
    String nombre = "Conexión \"ñ\" áéíóú 日本";
    repositorio.guardar(crearConexion("especial", nombre));

    Conexion recuperada =
        new RepositorioConexionesJSON(archivo.toFile()).buscarPorId("especial").orElseThrow();

    assertEquals(nombre, recuperada.getNombre());
  }

  @Test
  void guardarCienConexionesLasPersisteTodas() {
    for (int i = 0; i < 100; i++) {
      repositorio.guardar(crearConexion("conexion-" + i, "Conexion " + i));
    }

    RepositorioConexionesJSON otro = new RepositorioConexionesJSON(archivo.toFile());
    assertEquals(100, otro.listar().size());
    assertEquals("Conexion 99", otro.buscarPorId("conexion-99").orElseThrow().getNombre());
  }

  @Test
  void getConexionesPorEtiquetaDevuelveSoloLasCoincidencias() {
    Conexion produccion = crearConexion("prod", "Produccion");
    produccion.setEtiquetas(List.of("produccion"));
    Conexion desarrollo = crearConexion("dev", "Desarrollo");
    desarrollo.setEtiquetas(List.of("desarrollo", "local"));
    repositorio.guardar(produccion);
    repositorio.guardar(desarrollo);

    List<Conexion> resultado = repositorio.getConexionesPorEtiqueta("local");

    assertEquals(1, resultado.size());
    assertEquals("dev", resultado.get(0).getId());
  }

  @Test
  void getConexionesPorEtiquetaSinCoincidenciasDevuelveListaVacia() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    List<Conexion> resultado = repositorio.getConexionesPorEtiqueta("inexistente");

    assertNotNull(resultado);
    assertTrue(resultado.isEmpty());
  }

  @Test
  void getConexionesPorEtiquetaDistingueMayusculasYMinusculas() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    assertTrue(repositorio.getConexionesPorEtiqueta("PRUEBAS").isEmpty());
    assertEquals(1, repositorio.getConexionesPorEtiqueta("pruebas").size());
  }

  @Test
  void getConexionesPorEtiquetaIgnoraConexionesSinEtiquetas() throws IOException {
    Files.writeString(
        archivo,
        "[{\"id\":\"sin-etiquetas\",\"nombre\":\"Sin etiquetas\"},"
            + "{\"id\":\"con-etiquetas\",\"nombre\":\"Con etiquetas\","
            + "\"etiquetas\":[\"pruebas\"]}]",
        StandardCharsets.UTF_8);

    List<Conexion> resultado =
        assertDoesNotThrow(() -> repositorio.getConexionesPorEtiqueta("pruebas"));

    assertEquals(1, resultado.size());
    assertEquals("con-etiquetas", resultado.get(0).getId());
  }

  @Test
  void getConexionesPorEtiquetaNullDevuelveListaVacia() {
    repositorio.guardar(crearConexion("principal", "Principal"));

    List<Conexion> resultado =
        assertDoesNotThrow(() -> repositorio.getConexionesPorEtiqueta(null));

    assertTrue(resultado.isEmpty());
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
