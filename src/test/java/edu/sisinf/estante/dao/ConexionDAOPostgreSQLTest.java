package edu.sisinf.estante.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.sisinf.estante.core.ErrorConexion;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;

class ConexionDAOPostgreSQLTest {

    @Test
    void motorDevuelvePostgreSQL() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        assertEquals(TipoMotor.POSTGRESQL, dao.motor());
    }

    @Test
    void construirUrlUsaHostPuertoYBaseDeDatos() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.POSTGRESQL);
        conexion.setHost("192.168.1.20");
        conexion.setPuerto(5433);
        conexion.setBasedatos("inventario");

        String url = dao.construirUrl(conexion);

        assertEquals("jdbc:postgresql://192.168.1.20:5433/inventario", url);
    }

    @Test
    void construirUrlUsaElPuertoPorDefectoSiNoSeIndica() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.POSTGRESQL);
        conexion.setHost("localhost");
        conexion.setPuerto(null);
        conexion.setBasedatos("inventario");

        String url = dao.construirUrl(conexion);

        assertEquals("jdbc:postgresql://localhost:5432/inventario", url);
    }

    @Test
    void abrirFallaSiElMotorNoEsPostgreSQL() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setHost("localhost");
        conexion.setBasedatos("inventario");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiElHostEstaVacio() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.POSTGRESQL);
        conexion.setHost("");
        conexion.setBasedatos("inventario");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiLaBaseDeDatosEstaVacia() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.POSTGRESQL);
        conexion.setHost("localhost");
        conexion.setBasedatos("   ");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }
}