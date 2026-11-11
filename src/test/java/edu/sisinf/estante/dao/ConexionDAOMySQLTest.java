package edu.sisinf.chuspadb.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;

class ConexionDAOMySQLTest {

    private static final String PARAMETRO_SSL = "useSSL=false";

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

    private Conexion conexionCon(String host, int puerto, String basedatos) {
        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setHost(host);
        conexion.setPuerto(puerto);
        conexion.setBasedatos(basedatos);
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
    void construirUrlTieneElFormatoJdbcConParametroSsl() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", 3306, "tienda"));

        assertTrue(url.startsWith("jdbc:mysql://localhost:3306/tienda?"),
                "La URL debe separar la base de datos de los parametros con '?': " + url);
        assertTrue(url.contains(PARAMETRO_SSL),
                "La URL debe incluir " + PARAMETRO_SSL + ": " + url);
    }

    @Test
    void construirUrlUsaUnSoloSeparadorDeParametros() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", 3306, "tienda"));

        assertEquals(1, url.chars().filter(c -> c == '?').count(),
                "La URL debe tener un unico '?': " + url);
    }

    @Test
    void construirUrlRespetaDistintosHostsYPuertos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        Object[][] casos = {
                {"localhost", 3306},
                {"127.0.0.1", 3306},
                {"192.168.1.10", 3307},
                {"db.ejemplo.com", 3306},
                {"servidor-mysql", 33060},
                {"10.0.0.5", 65535},
        };

        for (Object[] caso : casos) {
            String host = (String) caso[0];
            int puerto = (Integer) caso[1];

            String url = dao.construirUrl(conexionCon(host, puerto, "tienda"));

            String esperado = "jdbc:mysql://" + host + ":" + puerto + "/tienda?";
            assertTrue(url.startsWith(esperado),
                    "Se esperaba que la URL empiece con " + esperado + " pero fue: " + url);
            assertTrue(url.contains(PARAMETRO_SSL),
                    "Falta " + PARAMETRO_SSL + " en: " + url);
        }
    }

    @Test
    void construirUrlCambiaSoloLoQueCambiaLaConexion() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String urlA = dao.construirUrl(conexionCon("localhost", 3306, "tienda"));
        String urlB = dao.construirUrl(conexionCon("localhost", 3307, "tienda"));
        String urlC = dao.construirUrl(conexionCon("otro-host", 3306, "tienda"));

        assertNotEquals(urlA, urlB);
        assertNotEquals(urlA, urlC);
        assertNotEquals(urlB, urlC);
    }

    @Test
    void construirUrlNoIncluyeUsuarioNiPassword() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionValida());

        assertFalse(url.contains("root"), "La URL no debe contener el usuario: " + url);
        assertFalse(url.contains("secreta"), "La URL no debe contener la password: " + url);
    }

    @Test
    void construirUrlConservaGuionesYGuionesBajosEnLaBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String[] nombres = {"mi-base", "mi_base", "base_2024-v2", "Tienda_UTO"};

        for (String nombre : nombres) {
            String url = dao.construirUrl(conexionCon("localhost", 3306, nombre));

            assertTrue(url.startsWith("jdbc:mysql://localhost:3306/" + nombre + "?"),
                    "El nombre " + nombre + " debe quedar intacto en: " + url);
        }
    }

    @Test
    void construirUrlManejaCaracteresEspecialesEnLaBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String[] nombres = {"mi base", "educación", "año_2024", "base#1"};

        for (String nombre : nombres) {
            String url = dao.construirUrl(conexionCon("localhost", 3306, nombre));

            // El nombre puede aparecer tal cual o codificado como URL; ambas formas son validas.
            String codificado = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
            String prefijo = "jdbc:mysql://localhost:3306/";

            assertTrue(url.startsWith(prefijo),
                    "La URL debe conservar el host y el puerto: " + url);
            assertTrue(url.contains(nombre) || url.contains(codificado),
                    "No se encontro el nombre '" + nombre + "' (ni su forma codificada) en: " + url);
            assertTrue(url.contains(PARAMETRO_SSL),
                    "Los parametros de conexion se perdieron con el nombre '" + nombre + "': " + url);
        }
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