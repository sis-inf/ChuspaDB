package edu.sisinf.chuspadb.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sisinf.chuspadb.modelo.ResultadoQuery;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportadorJSONTest {

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

        File archivo = File.createTempFile("resultado", ".json");

        try {
            ExportadorJSON exportador = new ExportadorJSON();
            exportador.exportar(resultado, archivo);

            String contenido = Files.readString(archivo.toPath());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(contenido);

            assertTrue(root.isArray());
            assertEquals(2, root.size());

            assertEquals(1, root.get(0).get("id").asInt());
            assertEquals("Ana", root.get(0).get("nombre").asText());

            assertEquals(2, root.get(1).get("id").asInt());
            assertEquals("Luis", root.get(1).get("nombre").asText());

        } finally {
            archivo.delete();
        }
    }

    @Test
    void exportar_conservaEstructuraEsperada() throws Exception {

        ResultadoQuery resultado = ResultadoQuery.deLectura(
                List.of("codigo", "activo"),
                List.of(
                        List.of(100, true)
                ),
                5
        );

        File archivo = File.createTempFile("resultado", ".json");

        try {
            new ExportadorJSON().exportar(resultado, archivo);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(archivo);

            assertEquals(1, root.size());

            JsonNode fila = root.get(0);

            assertTrue(fila.has("codigo"));
            assertTrue(fila.has("activo"));

            assertEquals(100, fila.get("codigo").asInt());
            assertTrue(fila.get("activo").asBoolean());

        } finally {
            archivo.delete();
        }
    }
}
