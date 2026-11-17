package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.ImportacionResultado;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para ImportadorJSON.
 */
class ImportadorJSONTest {

    @TempDir
    Path directorioTemp;

    @Test
    void importar_debeReimportarArchivoExportadoPorExportadorJSON() throws Exception {
        Path archivo = directorioTemp.resolve("datos.json");
        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(List.of(1, "Ana"), List.of(2, "Luis")),
                10
        );
        new ExportadorJSON().exportar(resultado, archivo.toFile());

        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        String sqlEsperado = "INSERT INTO usuarios (id, nombre) VALUES (?, ?)";

        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(sqlEsperado)).thenReturn(statement);

        ImportacionResultado importacion =
                new ImportadorJSON().importar(archivo, "usuarios", conexion, dao);

        assertEquals(2, importacion.filasInsertadas());
        assertEquals(0, importacion.filasFallidas());
        verify(statement).setString(1, "1");
        verify(statement).setString(2, "Ana");
        verify(statement).setString(1, "2");
        verify(statement).setString(2, "Luis");
    }

    @Test
    void importar_debeLanzarErrorPersistenciaConArchivoInvalido() throws Exception {
        Path archivo = directorioTemp.resolve("invalido.json");
        Files.writeString(archivo, "{ esto no es json", StandardCharsets.UTF_8);

        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);

        assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorJSON().importar(archivo, "usuarios", conexion, dao));

        verify(conexionJdbc, never()).prepareStatement(anyString());
    }

    private Path escribirJson(String contenido) throws Exception {
        Path archivo = directorioTemp.resolve("datos.json");
        Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        return archivo;
    }

    private ErrorPersistencia importarEsperandoError(String contenido, String tabla) throws Exception {
        Path archivo = escribirJson(contenido);
        IConexionDAO dao = mock(IConexionDAO.class);
        Conexion conexion = new Conexion();

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorJSON().importar(archivo, tabla, conexion, dao));

        verifyNoInteractions(dao);
        return error;
    }

    private IConexionDAO daoConStatement(Conexion conexion, String sql, PreparedStatement statement)
            throws Exception {
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(sql)).thenReturn(statement);
        return dao;
    }

    @Test
    void importar_debeLanzarErrorPersistenciaConArchivoInexistente() {
        Path inexistente = directorioTemp.resolve("no-existe.json");
        IConexionDAO dao = mock(IConexionDAO.class);

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorJSON().importar(inexistente, "usuarios", new Conexion(), dao));

        assertNotNull(error.getMessage());
        assertTrue(error.getMessage().contains("Error al leer el archivo JSON"));
        verifyNoInteractions(dao);
    }

    @Test
    void importar_debeLanzarErrorPersistenciaConArchivoVacio() throws Exception {
        ErrorPersistencia error = importarEsperandoError("", "usuarios");

        assertNotNull(error.getMessage());
    }

    @Test
    void importar_debeLanzarErrorPersistenciaConArrayVacio() throws Exception {
        ErrorPersistencia error = importarEsperandoError("[]", "usuarios");

        assertTrue(error.getMessage().contains("El array del archivo JSON está vacío"));
    }

    @Test
    void importar_debeLanzarErrorPersistenciaSiLaRaizNoEsUnArray() throws Exception {
        ErrorPersistencia error = importarEsperandoError("{\"id\": 1}", "usuarios");

        assertTrue(error.getMessage().contains("debe ser un array de objetos"));
        assertTrue(error.getMessage().contains("un objeto"));
    }

    @Test
    void importar_debeLanzarErrorPersistenciaSiUnaFilaNoEsObjeto() throws Exception {
        ErrorPersistencia error = importarEsperandoError("[1, 2]", "usuarios");

        assertTrue(error.getMessage().contains("La fila 1 del archivo JSON debe ser un objeto"));
    }

    @Test
    void importar_debeLanzarErrorPersistenciaSiElObjetoNoTieneColumnas() throws Exception {
        ErrorPersistencia error = importarEsperandoError("[{}]", "usuarios");

        assertEquals("El archivo JSON no tiene columnas.", error.getMessage());
    }

    @Test
    void importar_debeLanzarErrorPersistenciaConNombreDeTablaInvalido() throws Exception {
        ErrorPersistencia error = importarEsperandoError("[{\"id\": 1}]", "usuarios; DROP TABLE x");

        assertTrue(error.getMessage().contains("Identificador de tabla inválido"));
    }

    @Test
    void importar_debeLanzarErrorPersistenciaConNombreDeTablaNulo() throws Exception {
        ErrorPersistencia error = importarEsperandoError("[{\"id\": 1}]", null);

        assertTrue(error.getMessage().contains("Identificador de tabla inválido: null"));
    }

    @Test
    void importar_debeInsertarNullCuandoElValorEsNullEnElJson() throws Exception {
        Path archivo = escribirJson("[{\"id\": 1, \"nombre\": null}]");
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        IConexionDAO dao = daoConStatement(
                conexion, "INSERT INTO usuarios (id, nombre) VALUES (?, ?)", statement);

        ImportacionResultado resultado = new ImportadorJSON().importar(archivo, "usuarios", conexion, dao);

        assertEquals(1, resultado.filasInsertadas());
        assertEquals(0, resultado.filasFallidas());
        verify(statement).setString(1, "1");
        verify(statement).setString(2, null);
    }

    @Test
    void importar_debeInsertarNullCuandoFaltaUnaClaveEnUnaFila() throws Exception {
        Path archivo = escribirJson("[{\"id\": 1, \"nombre\": \"Ana\"}, {\"id\": 2}]");
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        IConexionDAO dao = daoConStatement(
                conexion, "INSERT INTO usuarios (id, nombre) VALUES (?, ?)", statement);

        ImportacionResultado resultado = new ImportadorJSON().importar(archivo, "usuarios", conexion, dao);

        assertEquals(2, resultado.filasInsertadas());
        assertEquals(0, resultado.filasFallidas());
        verify(statement).setString(2, "Ana");
        verify(statement).setString(2, null);
    }

    @Test
    void importar_debeContarFilaFallidaYContinuarConLaSiguiente() throws Exception {
        Path archivo = escribirJson("[{\"id\": 1}, {\"id\": 2}]");
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        IConexionDAO dao = daoConStatement(conexion, "INSERT INTO usuarios (id) VALUES (?)", statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("fila duplicada")).thenReturn(1);

        ImportacionResultado resultado = new ImportadorJSON().importar(archivo, "usuarios", conexion, dao);

        assertEquals(1, resultado.filasInsertadas());
        assertEquals(1, resultado.filasFallidas());
    }
}
