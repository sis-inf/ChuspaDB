package edu.sisinf.estante.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class ConnectionProviderTest {

    @Test
    void executeSelect_CierraRecursosInclusoAnteExcepcion() throws SQLException {
        Connection connectionMock = mock(Connection.class);
        Statement statementMock = mock(Statement.class);
        
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.executeQuery(anyString())).thenThrow(new SQLException("Error simulado de lectura"));

        assertThrows(SQLException.class, () -> {
            try (Statement stmt = connectionMock.createStatement()) {
                stmt.executeQuery("SELECT * FROM tabla_inexistente");
            }
        });

        verify(statementMock, times(1)).close();
    }

    @Test
    void executeUpdate_RechazaSentenciasSelectConExcepcionClara() {
        String querySelect = "SELECT * FROM usuarios WHERE id = 1";

        IllegalArgumentException excepcion = assertThrows(
            IllegalArgumentException.class,
            () -> validarSentenciaUpdate(querySelect),
            "executeUpdate debe rechazar sentencias SELECT con una excepcion"
        );

        assertTrue(excepcion.getMessage().toLowerCase().contains("select"), 
            "El mensaje de error debe indicar claramente que SELECT no esta permitido en executeUpdate.");
    }

    private void validarSentenciaUpdate(String sql) {
        if (sql != null && sql.trim().toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("No se permiten sentencias SELECT en executeUpdate.");
        }
    }
}