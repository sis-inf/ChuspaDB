package edu.sisinf.chuspadb.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;

class ConexionDAOSQLiteTest {

    @TempDir
    Path carpetaTemporal;

    private Conexion conexionHacia(Path archivo) {
        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.SQLITE);
        conexion.setBasedatos(archivo.toString());
        return conexion;
    }

    @Test
    void motorDevuelveSQLite() {
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        assertEquals(TipoMotor.SQLITE, dao.motor());
    }

    @Test
    void construirUrlUsaLaRutaDeLaBaseDeDatos() {
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.SQLITE);
        conexion.setBasedatos("prueba.db");

        String url = dao.construirUrl(conexion);

        assertEquals("jdbc:sqlite:prueba.db", url);
    }

    @Test
    void abrirFallaSiElMotorNoEsSQLite() {
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setBasedatos("prueba.db");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiLaBaseDeDatosEstaVacia() {
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.SQLITE);
        conexion.setBasedatos("   ");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirDevuelveUnaConexionValidaSobreUnaBaseDeDatosReal() throws Exception {
        Path archivo = carpetaTemporal.resolve("prueba.db");
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        try (Connection conn = dao.abrir(conexionHacia(archivo))) {
            assertNotNull(conn);
            assertTrue(conn.isValid(3));
        }
    }

    @Test
    void probarDevuelveTrueSobreUnaBaseDeDatosReal() {
        Path archivo = carpetaTemporal.resolve("prueba.db");
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        assertTrue(dao.probar(conexionHacia(archivo)));
    }

    @Test
    void getTablasDevuelveLasTablasDeLaBaseDePrueba() throws Exception {
        Path archivo = carpetaTemporal.resolve("prueba.db");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + archivo);
             Statement sentencia = conn.createStatement()) {
            sentencia.execute("CREATE TABLE estudiantes (id INTEGER PRIMARY KEY, nombre TEXT)");
            sentencia.execute("CREATE TABLE materias (id INTEGER PRIMARY KEY, nombre TEXT)");
        }

        ConexionDAOSQLite dao = new ConexionDAOSQLite();
        dao.abrir(conexionHacia(archivo)).close();

        List<String> tablas = dao.getTablas(archivo.toString());

        assertTrue(tablas.contains("estudiantes"));
        assertTrue(tablas.contains("materias"));
    }

    @Test
    void getTablasFallaSiNoSeAbrioUnaConexion() {
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        assertThrows(ErrorConexion.class, () -> dao.getTablas("cualquiera.db"));
    }

    @Test
    void probarDevuelveFalseSiLaRutaNoExiste() {
        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.SQLITE);
        conexion.setBasedatos(
                carpetaTemporal.resolve("carpeta-inexistente").resolve("x.db").toString());

        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        assertFalse(dao.probar(conexion));
    }

    // -------------------------------------------------------------------------
    // Ciclo de vida de la conexión: apertura y cierre
    // -------------------------------------------------------------------------

    @Test
    void conexionAbiertaPorElDaoSeCierraCorrectamente() throws Exception {
        Path archivo = carpetaTemporal.resolve("prueba.db");
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        Connection conn = dao.abrir(conexionHacia(archivo));
        assertFalse(conn.isClosed());

        conn.close();

        assertTrue(conn.isClosed());
        // Una conexión cerrada ya no puede usarse
        assertThrows(SQLException.class, conn::createStatement);
    }

    // -------------------------------------------------------------------------
    // DDL, inserción y lectura a través de la conexión del DAO
    // -------------------------------------------------------------------------

    @Test
    void ddlCreaUnaTablaYLosRegistrosInsertadosSeRecuperan() throws Exception {
        Path archivo = carpetaTemporal.resolve("prueba.db");
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        try (Connection conn = dao.abrir(conexionHacia(archivo));
             Statement sentencia = conn.createStatement()) {

            sentencia.execute("CREATE TABLE estudiantes (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL)");
            sentencia.executeUpdate("INSERT INTO estudiantes VALUES (1, 'Ana')");
            sentencia.executeUpdate("INSERT INTO estudiantes VALUES (2, 'Luis')");

            try (ResultSet rs = sentencia.executeQuery(
                    "SELECT id, nombre FROM estudiantes ORDER BY id")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("id"));
                assertEquals("Ana", rs.getString("nombre"));

                assertTrue(rs.next());
                assertEquals(2, rs.getInt("id"));
                assertEquals("Luis", rs.getString("nombre"));

                assertFalse(rs.next());
            }
        }
    }

    @Test
    void getTablasListaLasTablasCreadasConLaConexionDelDao() throws Exception {
        Path archivo = carpetaTemporal.resolve("prueba.db");
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        try (Connection conn = dao.abrir(conexionHacia(archivo));
             Statement sentencia = conn.createStatement()) {
            sentencia.execute("CREATE TABLE cursos (id INTEGER PRIMARY KEY)");
        }

        List<String> tablas = dao.getTablas(archivo.toString());

        assertTrue(tablas.contains("cursos"));
    }

    @Test
    void tablaEliminadaConDropYaNoApareceEnLaBase() throws Exception {
        Path archivo = carpetaTemporal.resolve("prueba.db");
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        try (Connection conn = dao.abrir(conexionHacia(archivo));
             Statement sentencia = conn.createStatement()) {
            sentencia.execute("CREATE TABLE temporal (id INTEGER)");
            sentencia.execute("DROP TABLE temporal");
        }

        assertFalse(dao.getTablas(archivo.toString()).contains("temporal"));
    }

    // -------------------------------------------------------------------------
    // Desconexión limpia: los datos persisten al cerrar y reabrir
    // -------------------------------------------------------------------------

    @Test
    void losDatosPersistenAlCerrarYReabrirLaConexion() throws Exception {
        Path archivo = carpetaTemporal.resolve("prueba.db");
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        try (Connection conn = dao.abrir(conexionHacia(archivo));
             Statement sentencia = conn.createStatement()) {
            sentencia.execute("CREATE TABLE estudiantes (id INTEGER PRIMARY KEY, nombre TEXT)");
            sentencia.executeUpdate("INSERT INTO estudiantes VALUES (1, 'Marta')");
        }

        assertTrue(Files.exists(archivo));

        // Se abre una conexión nueva sobre el mismo archivo
        try (Connection conn = dao.abrir(conexionHacia(archivo));
             Statement sentencia = conn.createStatement();
             ResultSet rs = sentencia.executeQuery("SELECT nombre FROM estudiantes")) {
            assertTrue(rs.next());
            assertEquals("Marta", rs.getString("nombre"));
            assertFalse(rs.next());
        }
    }
}
