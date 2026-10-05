package edu.sisinf.estante.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionProviderTest {

    @Test
    void testObtenerConexionExito(@TempDir Path tempDir) throws SQLException {
        Path dbPath = tempDir.resolve("test_db.db");
        String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();

        ConnectionProvider provider = new ConnectionProvider(url);
        
        try (Connection conn = provider.getConnection()) {
            assertNotNull(conn, "La conexión a la base de datos no debe ser nula");
            assertFalse(conn.isClosed(), "La conexión debe estar abierta");
        }
    }

    @Test
    void testObtenerConexionErrorUrlInvalida() {
        String urlInvalida = "jdbc:sqlite:/ruta/invalida/no_existente/db.sqlite";
        ConnectionProvider provider = new ConnectionProvider(urlInvalida);

        assertThrows(SQLException.class, () -> {
            try (Connection conn = provider.getConnection()) {
                conn.createStatement().execute("SELECT 1");
            }
        }, "Debe lanzar SQLException al intentar conectar con una URL inválida");
    }
}