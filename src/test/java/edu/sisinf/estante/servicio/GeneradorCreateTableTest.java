package edu.sisinf.estante.servicio;

import edu.sisinf.estante.dao.IConexionDAO;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de generación de CREATE TABLE para los motores soportados.
 */
class GeneradorCreateTableTest {

    private static final String TABLE_NAME = "usuarios";

    @Test
    void generar_debeUsarSintaxisMySql() throws SQLException {
        String ddl = generarDdl(
                TipoMotor.MYSQL,
                new String[]{"id", "nombre"},
                new String[]{"INT", "VARCHAR"},
                new int[]{11, 100},
                new String[]{"NO", "YES"});

        assertTrue(ddl.startsWith("CREATE TABLE `usuarios` ("));
        assertTrue(ddl.contains("`id` INT NOT NULL"));
        assertTrue(ddl.contains("`nombre` VARCHAR(100)"));
        assertTrue(ddl.contains("PRIMARY KEY (`id`)"));
        assertTrue(ddl.endsWith("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"));
    }

    @Test
    void generar_debeUsarTiposYSintaxisPostgreSql() throws SQLException {
        String ddl = generarDdl(
                TipoMotor.POSTGRESQL,
                new String[]{"id", "activo", "descripcion"},
                new String[]{"SERIAL", "BOOL", "LONGVARCHAR"},
                new int[]{10, 1, 255},
                new String[]{"NO", "YES", "YES"});

        assertTrue(ddl.startsWith("CREATE TABLE \"usuarios\" ("));
        assertTrue(ddl.contains("\"id\" SERIAL NOT NULL"));
        assertTrue(ddl.contains("\"activo\" BOOLEAN"));
        assertTrue(ddl.contains("\"descripcion\" TEXT"));
        assertTrue(ddl.contains("PRIMARY KEY (\"id\")"));
        assertFalse(ddl.contains("ENGINE=InnoDB"));
    }

    @Test
    void generar_debeUsarAfinidadesSqlite() throws SQLException {
        String ddl = generarDdl(
                TipoMotor.SQLITE,
                new String[]{"id", "nombre", "saldo"},
                new String[]{"BIGINT", "VARCHAR", "DECIMAL"},
                new int[]{19, 100, 10},
                new String[]{"NO", "YES", "YES"});

        assertTrue(ddl.startsWith("CREATE TABLE \"usuarios\" ("));
        assertTrue(ddl.contains("\"id\" INTEGER NOT NULL"));
        assertTrue(ddl.contains("\"nombre\" TEXT"));
        assertTrue(ddl.contains("\"saldo\" NUMERIC"));
        assertTrue(ddl.contains("PRIMARY KEY (\"id\")"));
        assertFalse(ddl.contains("ENGINE=InnoDB"));
    }

    private String generarDdl(
            TipoMotor motor,
            String[] columnNames,
            String[] typeNames,
            int[] columnSizes,
            String[] nullableValues
    ) throws SQLException {
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        ResultSet primaryKeys = mock(ResultSet.class);
        ResultSet columns = mock(ResultSet.class);

        when(dao.abrir(any(Conexion.class))).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getPrimaryKeys(null, null, TABLE_NAME)).thenReturn(primaryKeys);
        when(metadata.getColumns(null, null, TABLE_NAME, null)).thenReturn(columns);

        int[] primaryKeyIndex = {-1};
        when(primaryKeys.next()).thenAnswer(invocation -> ++primaryKeyIndex[0] == 0);
        when(primaryKeys.getString("COLUMN_NAME")).thenReturn("id");

        int[] columnIndex = {-1};
        when(columns.next()).thenAnswer(
                invocation -> ++columnIndex[0] < columnNames.length);
        when(columns.getString("COLUMN_NAME")).thenAnswer(
                invocation -> columnNames[columnIndex[0]]);
        when(columns.getString("TYPE_NAME")).thenAnswer(
                invocation -> typeNames[columnIndex[0]]);
        when(columns.getInt("COLUMN_SIZE")).thenAnswer(
                invocation -> columnSizes[columnIndex[0]]);
        when(columns.getString("IS_NULLABLE")).thenAnswer(
                invocation -> nullableValues[columnIndex[0]]);

        return GeneradorCreateTable.generar(TABLE_NAME, new Conexion(), dao, motor);
    }
}
