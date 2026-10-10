package edu.sisinf.estante.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import edu.sisinf.estante.core.ErrorConexion;
import edu.sisinf.estante.modelo.Conexion;
import edu.sisinf.estante.modelo.TipoMotor;

class ConexionDAOMySQLTest {

    private Conexion conexionValida() {
        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setHost("localhost");
        conexion.setPuerto(3306);
        conexion.setBasedatos("tienda");
        conexion.setUsuario("root");
        conexion.setPassword("secreta");
        return conexion;
    }

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

        Conexion conexion = conexionValida();
        conexion.setTipoMotor(TipoMotor.SQLITE);

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiElHostEstaVacio() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Conexion conexion = conexionValida();
        conexion.setHost("   ");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiLaBaseDeDatosEstaVacia() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Conexion conexion = conexionValida();
        conexion.setBasedatos("");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirDevuelveLaConexionEntregadaPorElDriver() throws Exception {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();
        Connection conexionSimulada = mock(Connection.class);

        try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
            driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conexionSimulada);

            Connection resultado = dao.abrir(conexionValida());

            assertSame(conexionSimulada, resultado);
        }
    }

    @Test
    void probarDevuelveFalseConCredencialesInvalidas() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Conexion conexion = conexionValida();
        conexion.setPuerto(65534);
        conexion.setBasedatos("base_inexistente");
        conexion.setUsuario("usuario_invalido");
        conexion.setPassword("password_invalido");

        assertFalse(dao.probar(conexion));
    }

    @Test
    void getTablasDevuelveLasTablasDeLaBaseDePrueba() throws Exception {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Connection conexionSimulada = mock(Connection.class);
        Statement sentencia = mock(Statement.class);
        ResultSet filas = mock(ResultSet.class);

        when(conexionSimulada.createStatement()).thenReturn(sentencia);
        when(sentencia.executeQuery(anyString())).thenReturn(filas);
        when(filas.next()).thenReturn(true, true, false);
        when(filas.getString(1)).thenReturn("estudiantes", "materias");

        try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
            driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conexionSimulada);

            dao.abrir(conexionValida());
            List<String> tablas = dao.getTablas("prueba");

            assertEquals(List.of("estudiantes", "materias"), tablas);
        }
    }

    @Test
    void getTablasFallaSiNoSeAbrioUnaConexion() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        assertThrows(ErrorConexion.class, () -> dao.getTablas("base_inexistente"));
    }
}
