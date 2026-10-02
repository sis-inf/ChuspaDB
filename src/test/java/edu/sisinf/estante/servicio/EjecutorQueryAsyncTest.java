package edu.sisinf.estante.servicio;

import edu.sisinf.estante.core.ConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class EjecutorQueryAsyncTest {

    private ConnectionProvider connectionProvider;
    private EjecutorQueryAsync ejecutor;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        Path dbPath = tempDir.resolve("test_async.db");
        String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        connectionProvider = new ConnectionProvider(url);
        ejecutor = new EjecutorQueryAsync(connectionProvider);

        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE usuarios (id INT PRIMARY KEY, nombre VARCHAR(50))");
            stmt.execute("INSERT INTO usuarios VALUES (1, 'TestUser')");
        }
    }

    @Test
    void testEjecutarQueryExitoYVerificarCierreResultSet() throws Exception {
        String sql = "SELECT * FROM usuarios WHERE id = 1";

        CompletableFuture<Boolean> future = ejecutor.ejecutarQuery(sql, rs -> {
            assertNotNull(rs, "El ResultSet no debe ser nulo");
            assertTrue(rs.next(), "Debe existir al menos un resultado");
            assertEquals("TestUser", rs.getString("nombre"));
            assertFalse(rs.isClosed(), "El ResultSet debe estar abierto durante el callback");
            return true;
        });

        Boolean resultado = future.get();
        assertTrue(resultado, "La consulta debe completarse exitosamente");
    }

    @Test
    void testEjecutarQueryErrorSqlInvalido() {
        String sqlInvalido = "SELECT * FROM tabla_que_no_existe";

        CompletableFuture<Boolean> future = ejecutor.ejecutarQuery(sqlInvalido, rs -> true);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertNotNull(exception.getCause(), "La causa del error no debe ser nula");
    }
}