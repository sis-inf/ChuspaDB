package edu.sisinf.chuspadb.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de SqlValidator.
 * Cubren clasificación de tipo, lectura, DML, DDL, consultas destructivas y casos borde.
 */
class SqlValidatorTest {

    // -------------------------------------------------------------------------
    // tipo() — reconocimiento de SELECT
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe detectar SELECT independientemente de mayúsculas/minúsculas o espacios")
    void tipo_select_retornaSelect() {
        assertEquals(SqlValidator.TipoQuery.SELECT, SqlValidator.tipo("select * from libros"));
        assertEquals(SqlValidator.TipoQuery.SELECT, SqlValidator.tipo("SELECT * FROM libros"));
        assertEquals(SqlValidator.TipoQuery.SELECT, SqlValidator.tipo("   SELECT * FROM t"));
    }

    // -------------------------------------------------------------------------
    // tipo() — reconocimiento de cada tipo DML/DDL
    // -------------------------------------------------------------------------

    @Test
    void tipo_insert_retornaInsert() {
        assertEquals(SqlValidator.TipoQuery.INSERT, SqlValidator.tipo("INSERT INTO libros (id) VALUES (1)"));
    }

    @Test
    void tipo_update_retornaUpdate() {
        assertEquals(SqlValidator.TipoQuery.UPDATE, SqlValidator.tipo("UPDATE libros SET titulo='a' WHERE id=1"));
    }

    @Test
    void tipo_delete_retornaDelete() {
        assertEquals(SqlValidator.TipoQuery.DELETE, SqlValidator.tipo("DELETE FROM libros WHERE id=1"));
    }

    @Test
    void tipo_create_retornaCreate() {
        assertEquals(SqlValidator.TipoQuery.CREATE, SqlValidator.tipo("CREATE TABLE libros (id INT)"));
    }

    @Test
    void tipo_drop_retornaDrop() {
        assertEquals(SqlValidator.TipoQuery.DROP, SqlValidator.tipo("DROP TABLE libros"));
    }

    @Test
    void tipo_alter_retornaAlter() {
        assertEquals(SqlValidator.TipoQuery.ALTER, SqlValidator.tipo("ALTER TABLE libros ADD COLUMN autor TEXT"));
    }

    // -------------------------------------------------------------------------
    // tipo() — casos DESCONOCIDO
    // -------------------------------------------------------------------------

    @Test
    void tipo_null_retornaDesconocido() {
        assertEquals(SqlValidator.TipoQuery.DESCONOCIDO, SqlValidator.tipo(null));
    }

    @Test
    void tipo_cadenaVacia_retornaDesconocido() {
        assertEquals(SqlValidator.TipoQuery.DESCONOCIDO, SqlValidator.tipo(""));
    }

    @Test
    void tipo_soloEspacios_retornaDesconocido() {
        assertEquals(SqlValidator.TipoQuery.DESCONOCIDO, SqlValidator.tipo("   "));
    }

    @Test
    void tipo_explain_retornaDesconocido() {
        assertEquals(SqlValidator.TipoQuery.DESCONOCIDO, SqlValidator.tipo("EXPLAIN SELECT * FROM libros"));
    }

    // -------------------------------------------------------------------------
    // esLectura()
    // -------------------------------------------------------------------------

    @Test
    void esLectura_conSelect_retornaTrue() {
        assertTrue(SqlValidator.esLectura("SELECT 1"));
    }

    @Test
    void esLectura_conInsert_retornaFalse() {
        assertFalse(SqlValidator.esLectura("INSERT INTO libros (id) VALUES (1)"));
    }

    @Test
    @DisplayName("Los comandos DDL no deben considerarse consultas de lectura")
    void esLectura_conDDL_retornaFalse() {
        assertFalse(SqlValidator.esLectura("DROP TABLE libros"));
        assertFalse(SqlValidator.esLectura("CREATE TABLE libros (id INT)"));
        assertFalse(SqlValidator.esLectura("ALTER TABLE libros ADD COLUMN autor TEXT"));
    }

    // -------------------------------------------------------------------------
    // esDML() y esDDL()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe detectar comandos DML (INSERT, UPDATE, DELETE)")
    void esDML_paraInsertUpdateDelete_retornaTrue() {
        assertTrue(SqlValidator.esDML("INSERT INTO libros (id) VALUES (1)"));
        assertTrue(SqlValidator.esDML("UPDATE libros SET titulo='a' WHERE id=1"));
        assertTrue(SqlValidator.esDML("DELETE FROM libros WHERE id=1"));
    }

    @Test
    void esDML_paraSelectYDDL_retornaFalse() {
        assertFalse(SqlValidator.esDML("SELECT * FROM libros"));
        assertFalse(SqlValidator.esDML("CREATE TABLE libros (id INT)"));
        assertFalse(SqlValidator.esDML("DROP TABLE libros"));
        assertFalse(SqlValidator.esDML("ALTER TABLE libros ADD COLUMN autor TEXT"));
    }

    @Test
    @DisplayName("DDL como DROP o CREATE no deben ser detectados como SELECT o DML")
    void esDDL_paraCreateDropAlter_retornaTrue() {
        assertTrue(SqlValidator.esDDL("CREATE TABLE libros (id INT)"));
        assertTrue(SqlValidator.esDDL("DROP TABLE libros"));
        assertTrue(SqlValidator.esDDL("ALTER TABLE libros ADD COLUMN autor TEXT"));
    }

    @Test
    void esDDL_paraSelectYDML_retornaFalse() {
        assertFalse(SqlValidator.esDDL("SELECT * FROM libros"));
        assertFalse(SqlValidator.esDDL("INSERT INTO libros (id) VALUES (1)"));
        assertFalse(SqlValidator.esDDL("UPDATE libros SET titulo='a' WHERE id=1"));
        assertFalse(SqlValidator.esDDL("DELETE FROM libros WHERE id=1"));
    }

    // -------------------------------------------------------------------------
    // esDestructiva()
    // -------------------------------------------------------------------------

    @Test
    void esDestructiva_delete_retornaTrue() {
        assertTrue(SqlValidator.esDestructiva("DELETE FROM users"));
    }

    @Test
    void esDestructiva_drop_retornaTrue() {
        assertTrue(SqlValidator.esDestructiva("DROP TABLE users"));
    }

    @Test
    void esDestructiva_updateSinWhere_retornaTrue() {
        assertTrue(SqlValidator.esDestructiva("UPDATE users SET nombre='a'"));
    }

    @Test
    void esDestructiva_updateConWhere_retornaFalse() {
        assertFalse(SqlValidator.esDestructiva("UPDATE users SET nombre='a' WHERE id=1"));
    }

    @Test
    void esDestructiva_updateConWhereCaseInsensitive_retornaFalse() {
        assertFalse(SqlValidator.esDestructiva("update USERS set nombre='a' where id=1"));
    }

    @Test
    void esDestructiva_select_retornaFalse() {
        assertFalse(SqlValidator.esDestructiva("SELECT * FROM users"));
    }

    @Test
    void esDestructiva_insert_retornaFalse() {
        assertFalse(SqlValidator.esDestructiva("INSERT INTO users (id) VALUES (1)"));
    }

    // -------------------------------------------------------------------------
    // Casos borde integrados desde el archivo eliminado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Manejo de casos borde: nulos y vacíos")
    void testCasosBorde() {
        assertFalse(SqlValidator.esLectura(""), "La cadena vacía no es lectura");
        assertFalse(SqlValidator.esDML(""), "La cadena vacía no es DML");
        assertFalse(SqlValidator.esLectura(null), "Null no debe lanzar excepción y ser false");
        assertFalse(SqlValidator.esDML(null), "Null no debe lanzar excepción y ser false");
    }
}
