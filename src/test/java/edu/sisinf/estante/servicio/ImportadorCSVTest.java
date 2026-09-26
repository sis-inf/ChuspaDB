package edu.sisinf.estante.servicio;

import edu.sisinf.estante.core.ErrorPersistencia;
import edu.sisinf.estante.dao.IConexionDAO;
import edu.sisinf.estante.modelo.Conexion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Tests unitarios para la validación de identificadores de ImportadorCSV.
 */
class ImportadorCSVTest {

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

    private Path crearCsv(String contenido) throws Exception {
        Path archivo = directorioTemp.resolve("datos.csv");
        Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        return archivo;
    }
}
