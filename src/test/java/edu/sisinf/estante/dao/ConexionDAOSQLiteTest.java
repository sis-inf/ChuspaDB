package edu.sisinf.estante.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.sisinf.estante.core.ErrorConexion;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;

class ConexionDAOSQLiteTest {

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
    void motorDevuelveSQLite() {
        ConexionDAOSQLite dao = new ConexionDAOSQLite();

        assertEquals(TipoMotor.SQLITE, dao.motor());
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
}