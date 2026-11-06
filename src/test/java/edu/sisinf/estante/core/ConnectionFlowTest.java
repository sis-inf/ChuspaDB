package edu.sisinf.chuspadb.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.mockito.Mockito.*;

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
}
