
package edu.sisinf.chuspadb.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportadorJSONTest {

    @TempDir
    Path directorioTemp;

    private final ExportadorJSON exportador = new ExportadorJSON();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void exportar_generaJsonValidoYParseable() throws Exception {

        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(
                        List.of(1, "Ana"),
                        List.of(2, "Luis")
                ),
                10
        );

        File archivo = directorioTemp.resolve("resultado.json").toFile();
        exportador.exportar(resultado, archivo);

        JsonNode root = mapper.readTree(archivo);

        assertTrue(root.isArray());
        assertEquals(2, root.size());

        assertEquals(1, root.get(0).get("id").asInt());
        assertEquals("Ana", root.get(0).get("nombre").asText());

        assertEquals(2, root.get(1).get("id").asInt());
        assertEquals("Luis", root.get(1).get("nombre").asText());
    }

    @Test
    void exportar_conservaEstructuraEsperada() throws Exception {

        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("codigo", "activo"),
                List.of(List.of(100, true)),
                5
        );

        File archivo = directorioTemp.resolve("estructura.json").toFile();
        exportador.exportar(resultado, archivo);

        JsonNode root = mapper.readTree(archivo);

        assertEquals(1, root.size());

        JsonNode fila = root.get(0);

        assertTrue(fila.has("codigo"));
        assertTrue(fila.has("activo"));

        assertTrue(fila.get("codigo").isNumber());
        assertEquals(100, fila.get("codigo").asInt());

        assertTrue(fila.get("activo").isBoolean());
        assertTrue(fila.get("activo").asBoolean());
    }

    @Test
    void exportar_debeSerializarNumerosSinComillas() throws Exception {

        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("entero", "decimal"),
                List.of(List.of(150, 25.75)),
                5
        );

        File archivo = directorioTemp.resolve("numeros.json").toFile();
        exportador.exportar(resultado, archivo);

        JsonNode root = mapper.readTree(archivo);
        JsonNode fila = root.get(0);

        assertTrue(fila.get("entero").isNumber());
        assertEquals(150, fila.get("entero").asInt());

        assertTrue(fila.get("decimal").isNumber());
        assertEquals(25.75, fila.get("decimal").asDouble(), 0.001);
    }

    @Test
    void exportar_debeSerializarValoresNulosComoNull() throws Exception {

        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("id", "nombre"),
                List.of(Arrays.asList(1, null)),
                5
        );

        File archivo = directorioTemp.resolve("nulos.json").toFile();
        exportador.exportar(resultado, archivo);

        JsonNode root = mapper.readTree(archivo);
        JsonNode fila = root.get(0);

        assertTrue(fila.get("id").isNumber());
        assertEquals(1, fila.get("id").asInt());

        assertTrue(fila.get("nombre").isNull());
    }
}
