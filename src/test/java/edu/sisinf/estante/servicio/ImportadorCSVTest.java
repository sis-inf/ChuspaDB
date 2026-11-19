package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.core.ErrorPersistencia;
import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.ImportacionResultado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de ImportadorCSV: validación de identificadores, entradas
 * inválidas, valores nulos, delimitadores, comillas y errores durante la importación.
 */
class ImportadorCSVTest {

    private static final String SQL_NOMBRE_EDAD =
            "INSERT INTO usuarios (nombre, edad) VALUES (?, ?)";

    @TempDir
    Path directorioTemp;

    @Test
    void importar_debeRechazarNombreDeTablaInvalido() throws Exception {
        Path archivo = crearCsv("nombre,edad\nAna,30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorCSV().importar(
                        archivo, "usuarios; DROP TABLE usuarios", conexion, dao));

        assertTrue(error.getMessage().contains("Identificador de tabla inválido"));
        verify(conexionJdbc, never()).prepareStatement(anyString());
    }

    @Test
    void importar_debeRechazarNombreDeColumnaInvalido() throws Exception {
        Path archivo = crearCsv("nombre; DROP TABLE usuarios;--,edad\nAna,30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorCSV().importar(archivo, "usuarios", conexion, dao));

        assertTrue(error.getMessage().contains("Identificador de columna inválido"));
        verify(conexionJdbc, never()).prepareStatement(anyString());
    }

    @Test
    void importar_debeMantenerFuncionamientoConIdentificadoresValidos() throws Exception {
        Path archivo = crearCsv("nombre,edad\nAna,30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        String sqlEsperado = "INSERT INTO usuarios (nombre, edad) VALUES (?, ?)";

        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(sqlEsperado)).thenReturn(statement);

        new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        verify(conexionJdbc).prepareStatement(sqlEsperado);
        verify(statement).setString(1, "Ana");
        verify(statement).setString(2, "30");
        verify(statement).executeUpdate();
    }

    @Test
    void importar_debeFallarSiElArchivoNoExiste() throws Exception {
        Path inexistente = directorioTemp.resolve("no-existe.csv");
        IConexionDAO dao = mock(IConexionDAO.class);
        Conexion conexion = new Conexion();

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorCSV().importar(inexistente, "usuarios", conexion, dao));

        assertNotNull(error.getMessage());
        assertTrue(error.getMessage().contains("Error al leer el archivo CSV"));
        verify(dao, never()).abrir(any());
    }

    @Test
    void importar_debeFallarSiElArchivoEstaVacio() throws Exception {
        Path archivo = crearCsv("");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorCSV().importar(archivo, "usuarios", conexion, dao));

        assertTrue(error.getMessage().contains("vacío o no tiene cabeceras"));
        verify(conexionJdbc, never()).prepareStatement(anyString());
    }

    @Test
    void importar_debeFallarSiLaCabeceraEstaEnBlanco() throws Exception {
        Path archivo = crearCsv("   \nAna,30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorCSV().importar(archivo, "usuarios", conexion, dao));

        assertTrue(error.getMessage().contains("vacío o no tiene cabeceras"));
        verify(conexionJdbc, never()).prepareStatement(anyString());
    }

    @Test
    void importar_debeRechazarNombreDeTablaNulo() throws Exception {
        Path archivo = crearCsv("nombre,edad\nAna,30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorCSV().importar(archivo, null, conexion, dao));

        assertEquals("Identificador de tabla inválido: null", error.getMessage());
        verify(conexionJdbc, never()).prepareStatement(anyString());
    }

    @Test
    void importar_conSoloCabeceraNoInsertaNada() throws Exception {
        Path archivo = crearCsv("nombre,edad\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);

        ImportacionResultado resultado =
                new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        assertNotNull(resultado);
        assertEquals(0, resultado.filasInsertadas());
        assertEquals(0, resultado.filasFallidas());
        assertTrue(resultado.errores().isEmpty());
        verify(conexionJdbc, never()).prepareStatement(anyString());
    }

    @Test
    void importar_debeInsertarNullCuandoFaltaUnValorEnLaFila() throws Exception {
        Path archivo = crearCsv("nombre,edad\nAna\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(SQL_NOMBRE_EDAD)).thenReturn(statement);

        ImportacionResultado resultado =
                new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        assertEquals(1, resultado.filasInsertadas());
        assertEquals(0, resultado.filasFallidas());
        verify(statement).setString(1, "Ana");
        verify(statement).setString(2, null);
    }

    @Test
    void importar_debeOmitirLineasEnBlanco() throws Exception {
        Path archivo = crearCsv("nombre,edad\n\nAna,30\n\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(SQL_NOMBRE_EDAD)).thenReturn(statement);

        ImportacionResultado resultado =
                new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        assertEquals(1, resultado.filasInsertadas());
        assertEquals(0, resultado.filasFallidas());
        verify(statement, times(1)).executeUpdate();
    }

    @Test
    void importar_debeDetectarPuntoYComaComoDelimitador() throws Exception {
        Path archivo = crearCsv("nombre;edad\nAna;30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(SQL_NOMBRE_EDAD)).thenReturn(statement);

        ImportacionResultado resultado =
                new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        assertEquals(1, resultado.filasInsertadas());
        verify(statement).setString(1, "Ana");
        verify(statement).setString(2, "30");
    }

    @Test
    void importar_debeUsarElDelimitadorIndicado() throws Exception {
        Path archivo = crearCsv("nombre\tedad\nAna\t30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(SQL_NOMBRE_EDAD)).thenReturn(statement);

        ImportacionResultado resultado = new ImportadorCSV().importar(
                archivo, "usuarios", conexion, dao, ImportadorCSV.Delimitador.TABULADOR);

        assertEquals(1, resultado.filasInsertadas());
        verify(statement).setString(1, "Ana");
        verify(statement).setString(2, "30");
    }

    @Test
    void importar_debeIgnorarLaMarcaBomDeLaPrimeraCabecera() throws Exception {
        Path archivo = crearCsv("\uFEFFnombre,edad\nAna,30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(SQL_NOMBRE_EDAD)).thenReturn(statement);

        ImportacionResultado resultado =
                new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        assertEquals(1, resultado.filasInsertadas());
        verify(conexionJdbc).prepareStatement(SQL_NOMBRE_EDAD);
    }

    @Test
    void importar_debeConservarDelimitadorYSaltoDeLineaDentroDeComillas() throws Exception {
        Path archivo = crearCsv("nombre,nota\n\"Perez, Ana\",\"linea1\nlinea2\"\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        String sqlEsperado = "INSERT INTO usuarios (nombre, nota) VALUES (?, ?)";
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(sqlEsperado)).thenReturn(statement);

        ImportacionResultado resultado =
                new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        assertEquals(1, resultado.filasInsertadas());
        assertEquals(0, resultado.filasFallidas());
        verify(statement).setString(1, "Perez, Ana");
        verify(statement).setString(2, "linea1\nlinea2");
    }

    @Test
    void importar_debeContarFilaFallidaYContinuarConLaSiguiente() throws Exception {
        Path archivo = crearCsv("nombre,edad\nAna,30\nLuis,25\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection conexionJdbc = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenReturn(conexionJdbc);
        when(conexionJdbc.prepareStatement(SQL_NOMBRE_EDAD)).thenReturn(statement);
        when(statement.executeUpdate())
                .thenThrow(new SQLException("violación de clave"))
                .thenReturn(1);

        ImportacionResultado resultado =
                new ImportadorCSV().importar(archivo, "usuarios", conexion, dao);

        assertNotNull(resultado);
        assertEquals(1, resultado.filasInsertadas());
        assertEquals(1, resultado.filasFallidas());
        assertEquals(1, resultado.errores().size());
        assertEquals("Línea 2: violación de clave", resultado.errores().get(0));
    }

    @Test
    void importar_debeEnvolverElErrorSiNoSePuedeAbrirLaConexion() throws Exception {
        Path archivo = crearCsv("nombre,edad\nAna,30\n");
        IConexionDAO dao = mock(IConexionDAO.class);
        Conexion conexion = new Conexion();
        when(dao.abrir(conexion)).thenThrow(new IllegalStateException("sin conexión"));

        ErrorPersistencia error = assertThrows(
                ErrorPersistencia.class,
                () -> new ImportadorCSV().importar(archivo, "usuarios", conexion, dao));

        assertTrue(error.getMessage().contains("Error al importar CSV"));
        assertTrue(error.getMessage().contains("sin conexión"));
    }

    private Path crearCsv(String contenido) throws Exception {
        Path archivo = directorioTemp.resolve("datos.csv");
        Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        return archivo;
    }
}
