package edu.sisinf.estante.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.sisinf.estante.core.ErrorConexion;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;

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
        List<String> tablas = dao.getTablas(archivo.toString());

        assertTrue(tablas.contains("estudiantes"));
        assertTrue(tablas.contains("materias"));
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
}