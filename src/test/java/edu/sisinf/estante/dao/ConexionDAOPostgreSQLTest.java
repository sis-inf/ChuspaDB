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

class ConexionDAOPostgreSQLTest {

    private Conexion conexionValida() {
        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.POSTGRESQL);
        conexion.setHost("localhost");
        conexion.setPuerto(5432);
        conexion.setBasedatos("inventario");
        conexion.setUsuario("postgres");
        conexion.setPassword("secreta");
        return conexion;
    }

    @Test
    void motorDevuelvePostgreSQL() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        assertEquals(TipoMotor.POSTGRESQL, dao.motor());
    }

    @Test
    void construirUrlUsaHostPuertoYBaseDeDatos() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = conexionValida();
        conexion.setHost("192.168.1.20");
        conexion.setPuerto(5433);

        String url = dao.construirUrl(conexion);

        assertEquals("jdbc:postgresql://192.168.1.20:5433/inventario", url);
    }

    @Test
    void construirUrlUsaElPuertoPorDefectoSiNoSeIndica() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = conexionValida();
        conexion.setPuerto(null);

        String url = dao.construirUrl(conexion);

        assertEquals("jdbc:postgresql://localhost:5432/inventario", url);
    }

    @Test
    void abrirFallaSiElMotorNoEsPostgreSQL() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = conexionValida();
        conexion.setTipoMotor(TipoMotor.MYSQL);

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiElHostEstaVacio() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = conexionValida();
        conexion.setHost("");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirFallaSiLaBaseDeDatosEstaVacia() {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = conexionValida();
        conexion.setBasedatos("   ");

        assertThrows(ErrorConexion.class, () -> dao.abrir(conexion));
    }

    @Test
    void abrirDevuelveLaConexionEntregadaPorElDriver() throws Exception {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();
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
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Conexion conexion = conexionValida();
        conexion.setPuerto(65533);
        conexion.setBasedatos("base_inexistente");
        conexion.setUsuario("usuario_invalido");
        conexion.setPassword("password_invalido");

        assertFalse(dao.probar(conexion));
    }

    @Test
    void getTablasDevuelveLasTablasDeLaBaseDePrueba() throws Exception {
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        Connection conexionSimulada = mock(Connection.class);
        Statement sentencia = mock(Statement.class);
        ResultSet filas = mock(ResultSet.class);

        when(conexionSimulada.createStatement()).thenReturn(sentencia);
        when(sentencia.executeQuery(anyString())).thenReturn(filas);
        when(filas.next()).thenReturn(true, true, false);
        when(filas.getString("tablename")).thenReturn("estudiantes", "materias");

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
        ConexionDAOPostgreSQL dao = new ConexionDAOPostgreSQL();

        assertThrows(ErrorConexion.class, () -> dao.getTablas("base_inexistente"));
    }
}
