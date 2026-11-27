package edu.sisinf.chuspadb.modelo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TipoMotorTest {

    @Test
    @DisplayName("Debe contener exactamente SQLITE, MYSQL y POSTGRESQL")
    void valuesDebeContenerExactamenteLosTresTipos() {
        TipoMotor[] esperados = {
            TipoMotor.SQLITE,
            TipoMotor.MYSQL,
            TipoMotor.POSTGRESQL
        };

        assertArrayEquals(esperados, TipoMotor.values());
    }

    @Test
    @DisplayName("Debe devolver MYSQL al usar valueOf")
    void valueOfDebeDevolverMySQL() {
        assertEquals(
            TipoMotor.MYSQL,
            TipoMotor.valueOf("MYSQL")
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException para ORACLE")
    void valueOfDebeLanzarExcepcionParaOracle() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf("ORACLE")
        );
    }

    @Test
    @DisplayName("El ordinal de SQLITE debe ser cero")
    void ordinalDeSQLiteDebeSerCero() {
        assertEquals(0, TipoMotor.SQLITE.ordinal());
    }

    @Test
    @DisplayName("valueOf con null debe lanzar NullPointerException")
    void valueOfConNullDebeLanzarNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> TipoMotor.valueOf(null)
        );
    }

    @Test
    @DisplayName("valueOf con cadena vacia debe lanzar IllegalArgumentException")
    void valueOfConCadenaVaciaDebeLanzarExcepcion() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf("")
        );
    }

    @Test
    @DisplayName("valueOf con solo espacios debe lanzar IllegalArgumentException")
    void valueOfConSoloEspaciosDebeLanzarExcepcion() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf("   ")
        );
    }

    @Test
    @DisplayName("valueOf con espacios alrededor del nombre debe lanzar IllegalArgumentException")
    void valueOfConEspaciosAlrededorDebeLanzarExcepcion() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf(" MYSQL ")
        );
    }

    @Test
    @DisplayName("valueOf con salto de linea final debe lanzar IllegalArgumentException")
    void valueOfConSaltoDeLineaDebeLanzarExcepcion() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf("SQLITE\n")
        );
    }

    @Test
    @DisplayName("valueOf distingue mayusculas y minusculas (minusculas)")
    void valueOfEnMinusculasDebeLanzarExcepcion() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf("mysql")
        );
    }

    @Test
    @DisplayName("valueOf distingue mayusculas y minusculas (mixto)")
    void valueOfEnMayusculasMixtasDebeLanzarExcepcion() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf("PostgreSQL")
        );
    }

    @Test
    @DisplayName("valueOf con nombre parcial POSTGRES debe lanzar IllegalArgumentException")
    void valueOfConNombreParcialDebeLanzarExcepcion() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TipoMotor.valueOf("POSTGRES")
        );
    }

    @Test
    @DisplayName("Enum.valueOf con nombre null debe lanzar NullPointerException")
    void enumValueOfConNombreNullDebeLanzarNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> Enum.valueOf(TipoMotor.class, null)
        );
    }

    @Test
    @DisplayName("valueOf debe devolver la misma instancia para cada constante")
    void valueOfDebeDevolverLaMismaInstanciaParaCadaConstante() {
        for (TipoMotor motor : TipoMotor.values()) {
            TipoMotor encontrado = TipoMotor.valueOf(motor.name());

            assertNotNull(encontrado);
            assertSame(motor, encontrado);
        }
    }

    @Test
    @DisplayName("valueOf debe devolver SQLITE y POSTGRESQL")
    void valueOfDebeDevolverSQLiteYPostgreSQL() {
        assertEquals(TipoMotor.SQLITE, TipoMotor.valueOf("SQLITE"));
        assertEquals(TipoMotor.POSTGRESQL, TipoMotor.valueOf("POSTGRESQL"));
    }

    @Test
    @DisplayName("values debe tener exactamente tres elementos")
    void valuesDebeTenerTresElementos() {
        assertEquals(3, TipoMotor.values().length);
    }

    @Test
    @DisplayName("values debe devolver una copia que no afecta al enum")
    void valuesDebeDevolverUnaCopiaDefensiva() {
        TipoMotor[] copia = TipoMotor.values();
        copia[0] = null;

        TipoMotor[] nueva = TipoMotor.values();

        assertNotSame(copia, nueva);
        assertNotNull(nueva[0]);
        assertEquals(TipoMotor.SQLITE, nueva[0]);
    }

    @Test
    @DisplayName("Ninguna constante de values debe ser null")
    void valuesNoDebeContenerNulos() {
        for (TipoMotor motor : TipoMotor.values()) {
            assertNotNull(motor);
        }
    }

    @Test
    @DisplayName("Los ordinales de MYSQL y POSTGRESQL deben ser uno y dos")
    void ordinalesDeMySQLYPostgreSQLDebenSerUnoYDos() {
        assertEquals(1, TipoMotor.MYSQL.ordinal());
        assertEquals(2, TipoMotor.POSTGRESQL.ordinal());
    }

    @Test
    @DisplayName("Los ordinales deben ser unicos y consecutivos")
    void ordinalesDebenSerUnicosYConsecutivos() {
        TipoMotor[] valores = TipoMotor.values();
        Set<Integer> vistos = new HashSet<>();

        for (int i = 0; i < valores.length; i++) {
            assertEquals(i, valores[i].ordinal());
            assertTrue(vistos.add(valores[i].ordinal()));
        }
    }

    @Test
    @DisplayName("name y toString deben coincidir para cada constante")
    void nameYToStringDebenCoincidir() {
        assertEquals("SQLITE", TipoMotor.SQLITE.name());
        assertEquals("MYSQL", TipoMotor.MYSQL.name());
        assertEquals("POSTGRESQL", TipoMotor.POSTGRESQL.name());

        for (TipoMotor motor : TipoMotor.values()) {
            assertEquals(motor.name(), motor.toString());
        }
    }

    @Test
    @DisplayName("compareTo debe respetar el orden de declaracion")
    void compareToDebeRespetarElOrdenDeDeclaracion() {
        assertTrue(TipoMotor.SQLITE.compareTo(TipoMotor.MYSQL) < 0);
        assertTrue(TipoMotor.POSTGRESQL.compareTo(TipoMotor.MYSQL) > 0);
        assertEquals(0, TipoMotor.MYSQL.compareTo(TipoMotor.MYSQL));
    }

    @Test
    @DisplayName("compareTo con null debe lanzar NullPointerException")
    void compareToConNullDebeLanzarNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> TipoMotor.SQLITE.compareTo(null)
        );
    }

    @Test
    @DisplayName("getDeclaringClass debe ser TipoMotor")
    void getDeclaringClassDebeSerTipoMotor() {
        for (TipoMotor motor : TipoMotor.values()) {
            assertEquals(TipoMotor.class, motor.getDeclaringClass());
        }
    }

    @Test
    @DisplayName("EnumSet.allOf debe contener las tres constantes")
    void enumSetAllOfDebeContenerLasTresConstantes() {
        EnumSet<TipoMotor> todos = EnumSet.allOf(TipoMotor.class);

        assertEquals(3, todos.size());
        assertTrue(todos.contains(TipoMotor.SQLITE));
        assertTrue(todos.contains(TipoMotor.MYSQL));
        assertTrue(todos.contains(TipoMotor.POSTGRESQL));
    }
}
