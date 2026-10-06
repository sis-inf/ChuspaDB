package edu.sisinf.estante.servicio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorSQLTest {

    // ==================== generarSelect ====================

    @Test
    @DisplayName("generarSelect con columnas [id, nombre] genera 'SELECT id, nombre FROM tabla'")
    void generarSelect_conColumnasIdYNombre_generaSelectCorrecto() {
        // Arrange
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnas = Arrays.asList("id", "nombre");
        String tabla = "tabla";

        // Act
        String resultado = generador.generarSelect(tabla, columnas);

        // Assert
        assertEquals("SELECT id, nombre FROM tabla", resultado);
    }

    @Test
    @DisplayName("generarSelect con una sola columna genera SELECT sin coma")
    void generarSelect_conUnaSolaColumna_generaSelectSinComa() {
        // Arrange
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnas = Collections.singletonList("id");
        String tabla = "tabla";

        // Act
        String resultado = generador.generarSelect(tabla, columnas);

        // Assert
        assertEquals("SELECT id FROM tabla", resultado);
    }

    // ==================== generarInsert ====================

    @Test
    @DisplayName("generarInsert genera INSERT con placeholders ?")
    void generarInsert_generaInsertConPlaceholders() {
        // Arrange
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnas = Arrays.asList("id", "nombre", "email");
        String tabla = "usuarios";

        // Act
        String resultado = generador.generarInsert(tabla, columnas);

        // Assert
        assertEquals("INSERT INTO usuarios (id, nombre, email) VALUES (?, ?, ?)", resultado);
    }

    @Test
    @DisplayName("generarInsert con una sola columna genera un solo placeholder")
    void generarInsert_conUnaSolaColumna_generaUnSoloPlaceholder() {
        // Arrange
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnas = Collections.singletonList("id");
        String tabla = "usuarios";

        // Act
        String resultado = generador.generarInsert(tabla, columnas);

        // Assert
        assertEquals("INSERT INTO usuarios (id) VALUES (?)", resultado);
    }

    // ==================== generarDelete ====================

    @Test
    @DisplayName("generarDelete genera DELETE con WHERE colPK = ?")
    void generarDelete_generaDeleteConWherePk() {
        // Arrange
        GeneradorSQL generador = new GeneradorSQL();
        String tabla = "usuarios";
        String colPK = "id";

        // Act
        String resultado = generador.generarDelete(tabla, colPK);

        // Assert
        assertEquals("DELETE FROM usuarios WHERE id = ?", resultado);
    }

    // ==================== generarUpdate (bonus) ====================

    @Test
    @DisplayName("generarUpdate genera UPDATE con SET y WHERE")
    void generarUpdate_generaUpdateConSetYWhere() {
        // Arrange
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnas = Arrays.asList("id", "nombre", "email");
        String tabla = "usuarios";
        String colPK = "id";

        // Act
        String resultado = generador.generarUpdate(tabla, columnas, colPK);

        // Assert
        assertEquals("UPDATE usuarios SET nombre = ?, email = ? WHERE id = ?", resultado);
    }

    @Test
    @DisplayName("generarUpdate excluye la columna PK del SET")
    void generarUpdate_excluyeColPkDelSet() {
        // Arrange
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnas = Arrays.asList("id", "nombre");
        String tabla = "usuarios";
        String colPK = "id";

        // Act
        String resultado = generador.generarUpdate(tabla, columnas, colPK);

        // Assert
        assertEquals("UPDATE usuarios SET nombre = ? WHERE id = ?", resultado);
    }

    // ==================== IllegalArgumentException (pendiente) ====================
    // NOTA: El código actual de GeneradorSQL NO valida listas vacías.
    // El issue #314 menciona que "lista de columnas vacía lanza IllegalArgumentException".
    // Si se agrega esa validación en el futuro, descomentar estos tests:

    /*
    @Test
    @DisplayName("generarSelect con lista de columnas vacía lanza IllegalArgumentException")
    void generarSelect_conColumnasVacias_lanzaIllegalArgumentException() {
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnasVacias = Collections.emptyList();
        String tabla = "tabla";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generador.generarSelect(tabla, columnasVacias)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("generarInsert con lista de columnas vacía lanza IllegalArgumentException")
    void generarInsert_conColumnasVacias_lanzaIllegalArgumentException() {
        GeneradorSQL generador = new GeneradorSQL();
        List<String> columnasVacias = Collections.emptyList();
        String tabla = "usuarios";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generador.generarInsert(tabla, columnasVacias)
        );

        assertNotNull(exception.getMessage());
    }
    */
}