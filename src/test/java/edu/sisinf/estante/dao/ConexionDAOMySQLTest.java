package edu.sisinf.chuspadb.dao;

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

import edu.sisinf.chuspadb.core.ErrorConexion;
import edu.sisinf.chuspadb.modelo.Conexion;
import edu.sisinf.chuspadb.modelo.TipoMotor;

class ConexionDAOMySQLTest {

    // Parametros que construirUrl agrega despues del '?'.
    private static final String PARAMS_SIN_SSL =
            "useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String PARAMS_CON_SSL =
            "useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";

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

    // Importante: setTipoMotor(MYSQL) fija el puerto en 3306, por eso se llama antes de setPuerto.
    private Conexion conexionCon(String host, Integer puerto, String basedatos) {
        Conexion conexion = new Conexion();
        conexion.setTipoMotor(TipoMotor.MYSQL);
        conexion.setHost(host);
        conexion.setPuerto(puerto);
        conexion.setBasedatos(basedatos);
        return conexion;
    }

    private Conexion conexionConSsl(String host, Integer puerto, String basedatos) {
        Conexion conexion = conexionCon(host, puerto, basedatos);
        conexion.setUsarSSL(true);
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

    // ---------------------------------------------------------------
    // Parametros de conexion: SSL deshabilitado (valor por defecto)
    // ---------------------------------------------------------------

    @Test
    void construirUrlSinSslDevuelveLaUrlExacta() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", 3306, "tienda"));

        assertEquals("jdbc:mysql://localhost:3306/tienda"
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", url);
    }

    @Test
    void conexionNuevaTieneSslDeshabilitadoPorDefecto() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();
        Conexion conexion = conexionCon("localhost", 3306, "tienda");

        assertFalse(conexion.isUsarSSL());
        assertTrue(dao.construirUrl(conexion).contains("useSSL=false"));
    }

    @Test
    void construirUrlSinSslIncluyeTimezoneYPublicKeyRetrieval() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", 3306, "tienda"));

        assertTrue(url.contains("serverTimezone=UTC"), "Falta serverTimezone=UTC en: " + url);
        assertTrue(url.contains("allowPublicKeyRetrieval=true"),
                "Falta allowPublicKeyRetrieval=true en: " + url);
        assertFalse(url.contains("requireSSL"), "Sin SSL no debe aparecer requireSSL: " + url);
    }

    @Test
    void construirUrlTieneUnSoloSeparadorDeParametros() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", 3306, "tienda"));

        assertEquals(1, url.chars().filter(c -> c == '?').count(),
                "La URL debe tener un unico '?': " + url);
    }

    // ---------------------------------------------------------------
    // Parametros de conexion: SSL habilitado
    // ---------------------------------------------------------------

    @Test
    void construirUrlConSslDevuelveLaUrlExacta() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionConSsl("localhost", 3306, "tienda"));

        assertEquals("jdbc:mysql://localhost:3306/tienda"
                + "?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true", url);
    }

    @Test
    void construirUrlConSslExigeSslYConservaTimezoneYPublicKeyRetrieval() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionConSsl("localhost", 3306, "tienda"));

        assertTrue(url.contains("useSSL=true"), "Falta useSSL=true en: " + url);
        assertTrue(url.contains("requireSSL=true"), "Falta requireSSL=true en: " + url);
        assertTrue(url.contains("serverTimezone=UTC"), "Falta serverTimezone=UTC en: " + url);
        assertTrue(url.contains("allowPublicKeyRetrieval=true"),
                "Falta allowPublicKeyRetrieval=true en: " + url);
        assertFalse(url.contains("useSSL=false"), "No debe aparecer useSSL=false: " + url);
    }

    @Test
    void construirUrlConSslRespetaHostPuertoYBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionConSsl("db.ejemplo.com", 3307, "ventas"));

        assertEquals("jdbc:mysql://db.ejemplo.com:3307/ventas?" + PARAMS_CON_SSL, url);
    }

    @Test
    void activarYDesactivarSslCambiaSoloLosParametros() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();
        Conexion conexion = conexionCon("localhost", 3306, "tienda");

        conexion.setUsarSSL(true);
        String urlConSsl = dao.construirUrl(conexion);
        conexion.setUsarSSL(false);
        String urlSinSsl = dao.construirUrl(conexion);

        assertEquals("jdbc:mysql://localhost:3306/tienda?" + PARAMS_CON_SSL, urlConSsl);
        assertEquals("jdbc:mysql://localhost:3306/tienda?" + PARAMS_SIN_SSL, urlSinSsl);
    }

    // ---------------------------------------------------------------
    // Hosts y puertos
    // ---------------------------------------------------------------

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
            Integer puerto = (Integer) caso[1];

            String url = dao.construirUrl(conexionCon(host, puerto, "tienda"));

            assertEquals("jdbc:mysql://" + host + ":" + puerto + "/tienda?" + PARAMS_SIN_SSL, url);
        }
    }

    @Test
    void construirUrlUsaPuertoPorDefectoCuandoElPuertoEsNulo() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", null, "tienda"));

        assertEquals("jdbc:mysql://localhost:3306/tienda?" + PARAMS_SIN_SSL, url);
    }

    @Test
    void construirUrlCambiaCuandoCambianHostOPuerto() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String urlBase = dao.construirUrl(conexionCon("localhost", 3306, "tienda"));
        String urlOtroPuerto = dao.construirUrl(conexionCon("localhost", 3307, "tienda"));
        String urlOtroHost = dao.construirUrl(conexionCon("otro-host", 3306, "tienda"));

        assertNotEquals(urlBase, urlOtroPuerto);
        assertNotEquals(urlBase, urlOtroHost);
        assertNotEquals(urlOtroPuerto, urlOtroHost);
    }

    @Test
    void construirUrlNoIncluyeUsuarioNiPassword() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String urlSinSsl = dao.construirUrl(conexionValida());
        Conexion conSsl = conexionValida();
        conSsl.setUsarSSL(true);
        String urlConSsl = dao.construirUrl(conSsl);

        assertFalse(urlSinSsl.contains("root") || urlSinSsl.contains("secreta"),
                "La URL no debe contener credenciales: " + urlSinSsl);
        assertFalse(urlConSsl.contains("root") || urlConSsl.contains("secreta"),
                "La URL no debe contener credenciales: " + urlConSsl);
    }

    // ---------------------------------------------------------------
    // Caracteres especiales en el nombre de la base de datos.
    // Comportamiento actual: construirUrl NO codifica el nombre, lo inserta tal cual.
    // ---------------------------------------------------------------

    @Test
    void construirUrlConservaGuionesYGuionesBajosEnLaBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String[] nombres = {"mi-base", "mi_base", "base_2024-v2", "Tienda_UTO"};

        for (String nombre : nombres) {
            String url = dao.construirUrl(conexionCon("localhost", 3306, nombre));

            assertEquals("jdbc:mysql://localhost:3306/" + nombre + "?" + PARAMS_SIN_SSL, url);
        }
    }

    @Test
    void construirUrlNoCodificaEspaciosEnLaBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", 3306, "mi base"));

        assertEquals("jdbc:mysql://localhost:3306/mi base?" + PARAMS_SIN_SSL, url);
        assertFalse(url.contains("%20"), "El espacio no se codifica: " + url);
    }

    @Test
    void construirUrlNoCodificaTildesNiEnieEnLaBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String urlTilde = dao.construirUrl(conexionCon("localhost", 3306, "educación"));
        String urlEnie = dao.construirUrl(conexionCon("localhost", 3306, "año_2024"));

        assertEquals("jdbc:mysql://localhost:3306/educación?" + PARAMS_SIN_SSL, urlTilde);
        assertEquals("jdbc:mysql://localhost:3306/año_2024?" + PARAMS_SIN_SSL, urlEnie);
    }

    @Test
    void construirUrlNoCodificaElNumeralEnLaBaseDeDatos() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionCon("localhost", 3306, "base#1"));

        assertEquals("jdbc:mysql://localhost:3306/base#1?" + PARAMS_SIN_SSL, url);
        assertFalse(url.contains("%23"), "El '#' no se codifica: " + url);
    }

    @Test
    void construirUrlConCaracteresEspecialesConSslConservaLosParametros() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String url = dao.construirUrl(conexionConSsl("localhost", 3306, "mi base"));

        assertEquals("jdbc:mysql://localhost:3306/mi base?" + PARAMS_CON_SSL, url);
    }

    @Test
    void getTablasRechazaNombresConCaracteresEspeciales() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        String[] invalidos = {
                "mi-base", "mi base", "educación", "año_2024", "base#1",
                "tienda;DROP DATABASE x", "base.tabla", ""
        };

        for (String nombre : invalidos) {
            assertThrows(ErrorConexion.class, () -> dao.getTablas(nombre),
                    "Debia rechazar el nombre: '" + nombre + "'");
        }
    }

    @Test
    void getTablasRechazaNombreNulo() {
        ConexionDAOMySQL dao = new ConexionDAOMySQL();

        assertThrows(ErrorConexion.class, () -> dao.getTablas(null));
    }

    // ---------------------------------------------------------------
    // Comportamiento existente de abrir / probar / getTablas
    // ---------------------------------------------------------------

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
