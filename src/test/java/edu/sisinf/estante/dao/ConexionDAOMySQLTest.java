package edu.sisinf.estante.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.sisinf.estante.core.ErrorConexion;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;

class ConexionDAOMySQLTest {

    @Test
    void motorDevuelveMySQL() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        assertEquals(TipoMotor.MYSQL, dao.motor());
    }

    @Test
    void construirUrlUsaHostPuertoYBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setHost("192.168.1.10");
        conexion.setPuerto(3307);
        conexion.setBasedatos("tienda");

        String url = dao.construirUrl(conexion);

        assertTrue(url.startsWith("jdbc:mysql://192.168.1.10:3307/tienda"));
    }

    @Test
    void abrirFallaSiElMotorNoEsMySQL() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.SQLITE);
        conexion.setHost("localhost");
        conexion.setBasedatos("tienda");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiElHostEstaVacio() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setHost("   ");
        conexion.setBasedatos("tienda");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiLaBaseDeDatosEstaVacia() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setHost("localhost");
        conexion.setBasedatos("");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }
}