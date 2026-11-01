package edu.sisinf.chuspadb.modelo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
