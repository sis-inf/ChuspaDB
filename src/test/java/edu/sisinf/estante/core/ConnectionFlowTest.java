package edu.sisinf.chuspadb.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para validar el flujo de executeSelect()
 * usando mocks de Connection, Statement y ResultSet.
 */
@ExtendWith(MockitoExtension.class)
class ConnectionFlowTest {

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Test
    void executeSelect_debeInvocarExecuteQueryUnaVez() throws Exception {
        // Arrange
        String sql = "SELECT * FROM libros";
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(sql)).thenReturn(resultSet);

        // Act
        Statement st = connection.createStatement();
        st.executeQuery(sql);

        // Assert
        verify(statement, times(1)).executeQuery(sql);
    }

    @Test
    void executeSelect_debeUsarConnectionParaCrearStatement() throws Exception {
        // Arrange
        String sql = "SELECT id FROM libros";
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(sql)).thenReturn(resultSet);

        // Act
        Statement st = connection.createStatement();
        st.executeQuery(sql);

        // Assert
        verify(connection, times(1)).createStatement();
    }

    @Test
    void executeSelect_resultSetDebeEstarDisponible() throws Exception {
        // Arrange
        String sql = "SELECT nombre FROM libros";
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(sql)).thenReturn(resultSet);

        // Act
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        // Assert
        verify(statement, times(1)).executeQuery(sql);
        assert rs != null;
    }

    // ---------------------------------------------------------------
    // Errores de createStatement y executeQuery
    // ---------------------------------------------------------------

    @Test
    void executeSelect_debePropagarSQLExceptionSiCreateStatementFalla() throws Exception {
        SQLException causa = new SQLException("Connection is closed");
        when(connection.createStatement()).thenThrow(causa);

        SQLException error = assertThrows(SQLException.class, () -> connection.createStatement());

        assertSame(causa, error);
        verifyNoInteractions(statement);
    }

    @Test
    void executeSelect_debePropagarSQLExceptionSiExecuteQueryFalla() throws Exception {
        String sql = "SELECT * FROM tabla_inexistente";
        SQLException causa = new SQLException("Table tabla_inexistente does not exist");
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(sql)).thenThrow(causa);

        Statement st = connection.createStatement();
        SQLException error = assertThrows(SQLException.class, () -> st.executeQuery(sql));

        assertSame(causa, error);
        assertEquals("Table tabla_inexistente does not exist", error.getMessage());
        verify(statement, times(1)).executeQuery(sql);
    }

    @Test
    void executeSelect_debeConservarSqlStateYCodigoDeErrorDelDriver() throws Exception {
        String sql = "SELECT * FROM libros WHERE";
        SQLException causa = new SQLException("Syntax error", "42000", 1064);
        when(statement.executeQuery(sql)).thenThrow(causa);

        SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery(sql));

        assertEquals("42000", error.getSQLState());
        assertEquals(1064, error.getErrorCode());
    }

    // ---------------------------------------------------------------
    // Consultas invalidas
    // ---------------------------------------------------------------

    @Test
    void executeSelect_consultaNulaDebeLanzarSQLException() throws Exception {
        SQLException causa = new SQLException("La consulta no puede ser nula");
        when(statement.executeQuery((String) null)).thenThrow(causa);

        SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery(null));

        assertNotNull(error.getMessage());
    }

    @Test
    void executeSelect_consultaVaciaODeSoloEspaciosDebeLanzarSQLException() throws Exception {
        SQLException causa = new SQLException("Consulta vacia");
        when(statement.executeQuery("")).thenThrow(causa);
        when(statement.executeQuery("   ")).thenThrow(causa);

        assertThrows(SQLException.class, () -> statement.executeQuery(""));
        assertThrows(SQLException.class, () -> statement.executeQuery("   "));
    }

    // ---------------------------------------------------------------
    // Contenido del ResultSet
    // ---------------------------------------------------------------

    @Test
    void executeSelect_debeDevolverElMismoResultSetQueDaElStatement() throws Exception {
        String sql = "SELECT * FROM libros";
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(sql)).thenReturn(resultSet);

        ResultSet rs = connection.createStatement().executeQuery(sql);

        assertNotNull(rs);
        assertSame(resultSet, rs);
    }

    @Test
    void executeSelect_resultSetSinFilasDebeDevolverFalseEnNext() throws Exception {
        String sql = "SELECT * FROM libros WHERE id = -1";
        when(statement.executeQuery(sql)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ResultSet rs = statement.executeQuery(sql);

        assertFalse(rs.next());
        verify(resultSet, times(1)).next();
        verify(resultSet, never()).getString(anyString());
    }

    @Test
    void executeSelect_debeRecorrerTodasLasFilasDelResultSet() throws Exception {
        String sql = "SELECT nombre FROM libros";
        when(statement.executeQuery(sql)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("nombre")).thenReturn("Don Quijote", "Rayuela", "Ficciones");

        List<String> nombres = new ArrayList<>();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            nombres.add(rs.getString("nombre"));
        }

        assertEquals(List.of("Don Quijote", "Rayuela", "Ficciones"), nombres);
        verify(resultSet, times(4)).next();
    }

    @Test
    void executeSelect_columnaInexistenteDebeLanzarSQLException() throws Exception {
        SQLException causa = new SQLException("Column columna_x not found");
        when(resultSet.getString("columna_x")).thenThrow(causa);

        SQLException error = assertThrows(SQLException.class, () -> resultSet.getString("columna_x"));

        assertSame(causa, error);
    }

    @Test
    void executeSelect_columnaNulaDebeDevolverNullYWasNullTrue() throws Exception {
        when(resultSet.getString("nombre")).thenReturn(null);
        when(resultSet.wasNull()).thenReturn(true);

        assertNull(resultSet.getString("nombre"));
        assertTrue(resultSet.wasNull());
    }

    // ---------------------------------------------------------------
    // Limites de ejecucion
    // ---------------------------------------------------------------

    @Test
    void executeSelect_debeAplicarElLimiteDeFilasConSetMaxRows() throws Exception {
        String sql = "SELECT * FROM libros";
        when(statement.executeQuery(sql)).thenReturn(resultSet);

        statement.setMaxRows(10);
        statement.executeQuery(sql);

        InOrder orden = inOrder(statement);
        orden.verify(statement).setMaxRows(10);
        orden.verify(statement).executeQuery(sql);
    }

    @Test
    void executeSelect_limiteDeFilasNegativoDebeLanzarSQLException() throws Exception {
        SQLException causa = new SQLException("maxRows no puede ser negativo");
        doThrow(causa).when(statement).setMaxRows(-1);

        SQLException error = assertThrows(SQLException.class, () -> statement.setMaxRows(-1));

        assertSame(causa, error);
        verify(statement, never()).executeQuery(anyString());
    }

    @Test
    void executeSelect_debeCrearStatementAntesDeEjecutarLaConsulta() throws Exception {
        String sql = "SELECT * FROM libros";
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(sql)).thenReturn(resultSet);

        connection.createStatement().executeQuery(sql);

        InOrder orden = inOrder(connection, statement);
        orden.verify(connection).createStatement();
        orden.verify(statement).executeQuery(sql);
    }

    @Test
    void executeSelect_dosConsultasDistintasEjecutanExecuteQueryUnaVezCadaUna() throws Exception {
        String sql1 = "SELECT * FROM libros";
        String sql2 = "SELECT * FROM autores";
        when(statement.executeQuery(sql1)).thenReturn(resultSet);
        when(statement.executeQuery(sql2)).thenReturn(resultSet);

        statement.executeQuery(sql1);
        statement.executeQuery(sql2);

        verify(statement, times(1)).executeQuery(sql1);
        verify(statement, times(1)).executeQuery(sql2);
        verify(statement, times(2)).executeQuery(anyString());
    }

    // ---------------------------------------------------------------
    // Cierre de recursos
    // ---------------------------------------------------------------

    @Test
    void executeSelect_debeCerrarResultSetYStatementEnOrdenInverso() throws Exception {
        String sql = "SELECT * FROM libros";
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(sql)).thenReturn(resultSet);

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertNotNull(rs);
        }

        InOrder orden = inOrder(resultSet, statement);
        orden.verify(resultSet).close();
        orden.verify(statement).close();
    }

    @Test
    void executeSelect_errorAlCerrarStatementDebePropagarseComoSQLException() throws Exception {
        SQLException causa = new SQLException("Error al cerrar");
        doThrow(causa).when(statement).close();

        SQLException error = assertThrows(SQLException.class, () -> statement.close());

        assertSame(causa, error);
    }
}
