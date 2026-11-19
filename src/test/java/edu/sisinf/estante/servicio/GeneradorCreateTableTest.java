package edu.sisinf.chuspadb.servicio;

import edu.sisinf.chuspadb.dao.IConexionDAO;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de generación de CREATE TABLE para los motores soportados.
 */
class GeneradorCreateTableTest {

    private static final String TABLE_NAME = "usuarios";
    private static final String[] SIN_CLAVES = {};

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

    @Test
    void generar_debeLanzarNullPointerExceptionSiElDaoEsNulo() {
        assertThrows(
                NullPointerException.class,
                () -> GeneradorCreateTable.generar(
                        TABLE_NAME, new Conexion(), null, TipoMotor.MYSQL));
    }

    @Test
    void generar_debeLanzarNullPointerExceptionSiElNombreDeTablaEsNulo() throws SQLException {
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(dao.abrir(any(Conexion.class))).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);

        assertThrows(
                NullPointerException.class,
                () -> GeneradorCreateTable.generar(
                        null, new Conexion(), dao, TipoMotor.MYSQL));
    }

    @Test
    void generar_debeLanzarNullPointerExceptionSiUnaColumnaNoTieneNombre() throws SQLException {
        Escenario escenario = preparar(TABLE_NAME, SIN_CLAVES, new String[][]{
                {null, "INT", "10", "NO", "NO"}});

        assertThrows(
                NullPointerException.class,
                () -> GeneradorCreateTable.generar(
                        TABLE_NAME, new Conexion(), escenario.dao(), TipoMotor.MYSQL));
    }

    @Test
    void generar_debePropagarSqlExceptionYCerrarLaConexionSiFallanLosMetadatos()
            throws SQLException {
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection connection = mock(Connection.class);
        when(dao.abrir(any(Conexion.class))).thenReturn(connection);
        when(connection.getMetaData()).thenThrow(new SQLException("fallo de metadatos"));

        SQLException error = assertThrows(
                SQLException.class,
                () -> GeneradorCreateTable.generar(
                        TABLE_NAME, new Conexion(), dao, TipoMotor.MYSQL));

        assertEquals("fallo de metadatos", error.getMessage());
        verify(connection).close();
    }

    @Test
    void generar_debeCerrarConexionYResultSetsAlTerminar() throws SQLException {
        Escenario escenario = preparar(TABLE_NAME, new String[]{"id"}, new String[][]{
                {"id", "INT", "10", "NO", "NO"}});

        GeneradorCreateTable.generar(
                TABLE_NAME, new Conexion(), escenario.dao(), TipoMotor.MYSQL);

        verify(escenario.clavesPrimarias()).close();
        verify(escenario.columnas()).close();
        verify(escenario.connection()).close();
    }

    @Test
    void generar_conTablaSinColumnasDebeProducirCuerpoVacio() throws SQLException {
        String mysql = ddl(TipoMotor.MYSQL, SIN_CLAVES, new String[][]{});
        String postgresql = ddl(TipoMotor.POSTGRESQL, SIN_CLAVES, new String[][]{});

        assertEquals(
                "CREATE TABLE `usuarios` (\n\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
                mysql);
        assertEquals("CREATE TABLE \"usuarios\" (\n\n);", postgresql);
    }

    @Test
    void generar_sinClavePrimariaNoDebeAgregarPrimaryKey() throws SQLException {
        String ddl = ddl(TipoMotor.POSTGRESQL, SIN_CLAVES, new String[][]{
                {"nombre", "VARCHAR", "50", "YES", "NO"}});

        assertEquals("CREATE TABLE \"usuarios\" (\n    \"nombre\" VARCHAR(50)\n);", ddl);
        assertFalse(ddl.contains("PRIMARY KEY"));
    }

    @Test
    void generar_debeDeclararClavePrimariaCompuestaAlFinal() throws SQLException {
        String ddl = ddl(TipoMotor.MYSQL, new String[]{"id", "codigo"}, new String[][]{
                {"id", "INT", "11", "NO", "NO"},
                {"codigo", "VARCHAR", "20", "NO", "NO"}});

        assertEquals(
                "CREATE TABLE `usuarios` (\n"
                        + "    `id` INT NOT NULL,\n"
                        + "    `codigo` VARCHAR(20) NOT NULL,\n"
                        + "    PRIMARY KEY (`id`, `codigo`)\n"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
                ddl);
    }

    @Test
    void generar_debeAplicarNulabilidadSegunIsNullable() throws SQLException {
        String ddl = ddl(TipoMotor.MYSQL, SIN_CLAVES, new String[][]{
                {"a", "INT", "10", "NO", "NO"},
                {"b", "INT", "10", "YES", "NO"},
                {"c", "INT", "10", "DESCONOCIDO", "NO"}});

        assertEquals(
                "CREATE TABLE `usuarios` (\n"
                        + "    `a` INT NOT NULL,\n"
                        + "    `b` INT,\n"
                        + "    `c` INT\n"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
                ddl);
    }

    @Test
    void generar_mysqlDebeConvertirVarcharGrandeEnLongtext() throws SQLException {
        String ddl = ddl(TipoMotor.MYSQL, SIN_CLAVES, new String[][]{
                {"grande", "VARCHAR", "16384", "YES", "NO"},
                {"limite", "VARCHAR", "16383", "YES", "NO"}});

        assertTrue(ddl.contains("`grande` LONGTEXT,"));
        assertTrue(ddl.contains("`limite` VARCHAR(16383)\n)"));
    }

    @Test
    void generar_mysqlDebeAdaptarTiposDeOtrosMotores() throws SQLException {
        String ddl = ddl(TipoMotor.MYSQL, SIN_CLAVES, new String[][]{
                {"c1", "CLOB", "0", "YES", "NO"},
                {"c2", "LONGVARCHAR", "100", "YES", "NO"},
                {"c3", "BOOL", "1", "YES", "NO"},
                {"c4", "TIMESTAMPTZ", "0", "YES", "NO"},
                {"c5", "TIMETZ", "0", "YES", "NO"},
                {"c6", "VARCHAR", "0", "YES", "NO"},
                {"c7", "CHAR", "5", "YES", "NO"}});

        assertTrue(ddl.contains("`c1` LONGTEXT,"));
        assertTrue(ddl.contains("`c2` LONGTEXT,"));
        assertTrue(ddl.contains("`c3` BOOLEAN,"));
        assertTrue(ddl.contains("`c4` DATETIME,"));
        assertTrue(ddl.contains("`c5` TIME,"));
        assertTrue(ddl.contains("`c6` VARCHAR,"));
        assertTrue(ddl.contains("`c7` CHAR(5)\n)"));
    }

    @Test
    void generar_postgresqlDebeUsarTextoParaVarcharMuyGrande() throws SQLException {
        String ddl = ddl(TipoMotor.POSTGRESQL, SIN_CLAVES, new String[][]{
                {"enorme", "VARCHAR", "10485761", "YES", "NO"},
                {"limite", "VARCHAR", "10485760", "YES", "NO"},
                {"sinTamano", "VARCHAR", "0", "YES", "NO"},
                {"fijo", "CHAR", "3", "YES", "NO"}});

        assertTrue(ddl.contains("\"enorme\" TEXT,"));
        assertTrue(ddl.contains("\"limite\" VARCHAR(10485760),"));
        assertTrue(ddl.contains("\"sinTamano\" VARCHAR,"));
        assertTrue(ddl.contains("\"fijo\" CHAR(3)\n)"));
    }

    @Test
    void generar_postgresqlDebeDistinguirBitDeUnoYDeVariosBits() throws SQLException {
        String ddl = ddl(TipoMotor.POSTGRESQL, SIN_CLAVES, new String[][]{
                {"bandera", "BIT", "1", "YES", "NO"},
                {"mascara", "BIT", "8", "YES", "NO"}});

        assertTrue(ddl.contains("\"bandera\" BOOLEAN,"));
        assertTrue(ddl.contains("\"mascara\" BIT(8)\n)"));
    }

    @Test
    void generar_postgresqlDebeAdaptarTiposDeOtrosMotores() throws SQLException {
        String ddl = ddl(TipoMotor.POSTGRESQL, SIN_CLAVES, new String[][]{
                {"a", "DATETIME", "0", "YES", "NO"},
                {"b", "TINYINT", "3", "YES", "NO"},
                {"c", "DOUBLE", "0", "YES", "NO"},
                {"d", "BLOB", "0", "YES", "NO"},
                {"e", "YEAR", "4", "YES", "NO"},
                {"f", "INT4", "10", "YES", "NO"},
                {"g", "geometry", "0", "YES", "NO"}});

        assertTrue(ddl.contains("\"a\" TIMESTAMP,"));
        assertTrue(ddl.contains("\"b\" SMALLINT,"));
        assertTrue(ddl.contains("\"c\" DOUBLE PRECISION,"));
        assertTrue(ddl.contains("\"d\" BYTEA,"));
        assertTrue(ddl.contains("\"e\" SMALLINT,"));
        assertTrue(ddl.contains("\"f\" INTEGER,"));
        assertTrue(ddl.contains("\"g\" GEOMETRY\n)"));
    }

    @Test
    void generar_sqliteDebeUsarAfinidadesParaCadaTipo() throws SQLException {
        String ddl = ddl(TipoMotor.SQLITE, SIN_CLAVES, new String[][]{
                {"a", "BOOLEAN", "1", "YES", "NO"},
                {"b", "BIT", "1", "YES", "NO"},
                {"c", "TIMESTAMP", "0", "YES", "NO"},
                {"d", "DATETIME", "0", "YES", "NO"},
                {"e", "DATE", "0", "YES", "NO"},
                {"f", "TIME", "0", "YES", "NO"},
                {"g", "FLOAT", "0", "YES", "NO"},
                {"h", "DOUBLE", "0", "YES", "NO"},
                {"i", "BLOB", "0", "YES", "NO"},
                {"j", "GEOMETRY", "0", "YES", "NO"},
                {"k", "LONGVARCHAR", "0", "YES", "NO"}});

        assertTrue(ddl.contains("\"a\" INTEGER,"));
        assertTrue(ddl.contains("\"b\" INTEGER,"));
        assertTrue(ddl.contains("\"c\" TIMESTAMP,"));
        assertTrue(ddl.contains("\"d\" DATETIME,"));
        assertTrue(ddl.contains("\"e\" DATE,"));
        assertTrue(ddl.contains("\"f\" TIME,"));
        assertTrue(ddl.contains("\"g\" REAL,"));
        assertTrue(ddl.contains("\"h\" REAL,"));
        assertTrue(ddl.contains("\"i\" BLOB,"));
        assertTrue(ddl.contains("\"j\" NUMERIC,"));
        assertTrue(ddl.contains("\"k\" TEXT\n)"));
    }

    @Test
    void generar_sqliteDebeDeclararAutoincrementoEnLineaConPkUnica() throws SQLException {
        String ddl = ddl(TipoMotor.SQLITE, new String[]{"id"}, new String[][]{
                {"id", "INTEGER", "10", "NO", "YES"},
                {"nombre", "TEXT", "0", "YES", "NO"}});

        assertEquals(
                "CREATE TABLE \"usuarios\" (\n"
                        + "    \"id\" INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + "    \"nombre\" TEXT\n"
                        + ");",
                ddl);
    }

    @Test
    void generar_sqliteNoDebeUsarAutoincrementoConPkCompuesta() throws SQLException {
        String ddl = ddl(TipoMotor.SQLITE, new String[]{"a", "b"}, new String[][]{
                {"a", "INTEGER", "10", "NO", "YES"},
                {"b", "INTEGER", "10", "NO", "NO"}});

        assertEquals(
                "CREATE TABLE \"usuarios\" (\n"
                        + "    \"a\" INTEGER NOT NULL,\n"
                        + "    \"b\" INTEGER NOT NULL,\n"
                        + "    PRIMARY KEY (\"a\", \"b\")\n"
                        + ");",
                ddl);
        assertFalse(ddl.contains("AUTOINCREMENT"));
    }

    @Test
    void generar_mysqlDebeAgregarAutoIncrementEnColumnasAutoincrementales() throws SQLException {
        String ddl = ddl(TipoMotor.MYSQL, new String[]{"id"}, new String[][]{
                {"id", "INT", "11", "NO", "YES"},
                {"nombre", "VARCHAR", "50", "YES", "NO"}});

        assertEquals(
                "CREATE TABLE `usuarios` (\n"
                        + "    `id` INT NOT NULL AUTO_INCREMENT,\n"
                        + "    `nombre` VARCHAR(50),\n"
                        + "    PRIMARY KEY (`id`)\n"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
                ddl);
    }

    @Test
    void generar_mysqlDebeConvertirSerialDePostgresAEnteros() throws SQLException {
        String ddl = ddl(TipoMotor.MYSQL, SIN_CLAVES, new String[][]{
                {"a", "BIGSERIAL", "19", "YES", "YES"},
                {"b", "SMALLSERIAL", "5", "YES", "YES"},
                {"c", "SERIAL", "10", "YES", "YES"}});

        assertTrue(ddl.contains("`a` BIGINT AUTO_INCREMENT,"));
        assertTrue(ddl.contains("`b` SMALLINT AUTO_INCREMENT,"));
        assertTrue(ddl.contains("`c` INT AUTO_INCREMENT\n)"));
    }

    @Test
    void generar_postgresqlDebeUsarSerialEnColumnasAutoincrementales() throws SQLException {
        String ddl = ddl(TipoMotor.POSTGRESQL, new String[]{"a"}, new String[][]{
                {"a", "BIGINT", "19", "NO", "YES"},
                {"b", "SMALLINT", "5", "YES", "YES"},
                {"c", "INTEGER", "10", "YES", "YES"}});

        assertTrue(ddl.contains("\"a\" BIGSERIAL NOT NULL,"));
        assertTrue(ddl.contains("\"b\" SMALLSERIAL,"));
        assertTrue(ddl.contains("\"c\" SERIAL,"));
        assertTrue(ddl.contains("PRIMARY KEY (\"a\")"));
        assertFalse(ddl.contains("AUTO_INCREMENT"));
    }

    @Test
    void generar_debeEscaparComillasEnLosIdentificadores() throws SQLException {
        String tablaMysql = "ta`bla";
        Escenario mysql = preparar(tablaMysql, SIN_CLAVES, new String[][]{
                {"col`x", "INT", "10", "NO", "NO"}});
        String ddlMysql = GeneradorCreateTable.generar(
                tablaMysql, new Conexion(), mysql.dao(), TipoMotor.MYSQL);

        assertTrue(ddlMysql.startsWith("CREATE TABLE `ta``bla` ("));
        assertTrue(ddlMysql.contains("`col``x` INT NOT NULL"));

        String tablaPostgres = "ta\"bla";
        Escenario postgres = preparar(tablaPostgres, SIN_CLAVES, new String[][]{
                {"col\"x", "INT", "10", "NO", "NO"}});
        String ddlPostgres = GeneradorCreateTable.generar(
                tablaPostgres, new Conexion(), postgres.dao(), TipoMotor.POSTGRESQL);

        assertTrue(ddlPostgres.startsWith("CREATE TABLE \"ta\"\"bla\" ("));
        assertTrue(ddlPostgres.contains("\"col\"\"x\" INTEGER NOT NULL"));
    }

    private record Escenario(
            IConexionDAO dao,
            Connection connection,
            ResultSet clavesPrimarias,
            ResultSet columnas
    ) {}

    /**
     * Genera el DDL de la tabla "usuarios". Cada columna se describe como
     * {nombre, tipo, tamaño, IS_NULLABLE, IS_AUTOINCREMENT}.
     */
    private String ddl(TipoMotor motor, String[] claves, String[][] columnas)
            throws SQLException {
        Escenario escenario = preparar(TABLE_NAME, claves, columnas);
        return GeneradorCreateTable.generar(
                TABLE_NAME, new Conexion(), escenario.dao(), motor);
    }

    private Escenario preparar(String tabla, String[] claves, String[][] cols)
            throws SQLException {
        IConexionDAO dao = mock(IConexionDAO.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        ResultSet clavesPrimarias = mock(ResultSet.class);
        ResultSet columnas = mock(ResultSet.class);

        when(dao.abrir(any(Conexion.class))).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getPrimaryKeys(null, null, tabla)).thenReturn(clavesPrimarias);
        when(metadata.getColumns(null, null, tabla, null)).thenReturn(columnas);

        int[] indiceClave = {-1};
        when(clavesPrimarias.next()).thenAnswer(
                invocation -> ++indiceClave[0] < claves.length);
        when(clavesPrimarias.getString("COLUMN_NAME")).thenAnswer(
                invocation -> claves[indiceClave[0]]);

        int[] indiceColumna = {-1};
        when(columnas.next()).thenAnswer(
                invocation -> ++indiceColumna[0] < cols.length);
        when(columnas.getString("COLUMN_NAME")).thenAnswer(
                invocation -> cols[indiceColumna[0]][0]);
        when(columnas.getString("TYPE_NAME")).thenAnswer(
                invocation -> cols[indiceColumna[0]][1]);
        when(columnas.getInt("COLUMN_SIZE")).thenAnswer(
                invocation -> Integer.parseInt(cols[indiceColumna[0]][2]));
        when(columnas.getString("IS_NULLABLE")).thenAnswer(
                invocation -> cols[indiceColumna[0]][3]);
        when(columnas.getString("IS_AUTOINCREMENT")).thenAnswer(
                invocation -> cols[indiceColumna[0]][4]);

        return new Escenario(dao, connection, clavesPrimarias, columnas);
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

    @Test
    void generar_debeDevolverUnDdlNoNuloYConContenido() throws SQLException {
        String ddl = ddl(TipoMotor.MYSQL, new String[]{"id"}, new String[][]{
                {"id", "INT", "11", "NO", "NO"},
                {"nombre", "VARCHAR", "100", "YES", "NO"}});

        assertNotNull(ddl);
        assertFalse(ddl.isBlank());
        assertTrue(ddl.startsWith("CREATE TABLE `usuarios` ("));
    }
}
