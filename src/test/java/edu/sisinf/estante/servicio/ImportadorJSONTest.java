package edu.sisinf.estante.servicio;

import edu.sisinf.estante.core.ErrorPersistencia;
import edu.sisinf.estante.dao.IConexionDAO;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.ImportacionResultado;
import edu.sisinf.estante.modelo.ResultadoQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
}
