package edu.sisinf.chuspadb.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.sisinf.chuspadb.config.ConfiguracionApp;
import edu.sisinf.chuspadb.modelo.FavoritoQuery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de gestionar las consultas favoritas,
 * permitiendo organizarlas por categoría y persistirlas en JSON.
 */
public class GestorFavoritos {

    private List<FavoritoQuery> favoritos;
    private final Map<String, String> categorias;
    private final File archivoPersistencia;
    private final ObjectMapper mapper;

    public GestorFavoritos() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ConfiguracionApp.asegurarDirectorioHome();

        this.archivoPersistencia = ConfiguracionApp.directorioHome()
                .resolve("favoritos.json")
                .toFile();

        this.favoritos = new ArrayList<>();
        this.categorias = new HashMap<>();

        obtenerTodos();
    }

    public void agregar(FavoritoQuery favorito) {
        agregar(favorito, "");
    }

    public void agregar(FavoritoQuery favorito, String categoria) {
        if (favorito == null) {
            throw new IllegalArgumentException(
                    "El favorito no puede ser nulo");
        }

        boolean duplicado = favoritos.stream()
                .anyMatch(f -> f.nombre().equalsIgnoreCase(favorito.nombre()));

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Ya existe una consulta guardada con el nombre: "
                            + favorito.nombre());
        }

        favoritos.add(favorito);

        if (categoria != null && !categoria.isBlank()) {
            categorias.put(favorito.nombre().toLowerCase(), categoria.trim());
        }

        guardarEnArchivo();
    }

    public void eliminar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }

        boolean removido = favoritos.removeIf(
                f -> f.nombre().equalsIgnoreCase(nombre));

        categorias.remove(nombre.toLowerCase());

        if (removido) {
            guardarEnArchivo();
        }
    }

    public FavoritoQuery buscar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        return favoritos.stream()
                .filter(f -> f.nombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }

    public List<FavoritoQuery> obtenerTodos() {
        favoritos.clear();
        categorias.clear();

        if (!archivoPersistencia.exists()
                || archivoPersistencia.length() == 0) {
            return new ArrayList<>();
        }

        try {
            JsonNode datos = mapper.readTree(archivoPersistencia);

            if (datos.isArray()) {
                for (JsonNode nodo : datos) {
                    String categoria = nodo.has("categoria")
                            ? nodo.get("categoria").asText("")
                            : "";

                    ObjectNode favoritoJson = nodo.deepCopy();
                    favoritoJson.remove("categoria");

                    FavoritoQuery favorito = mapper.treeToValue(
                            favoritoJson, FavoritoQuery.class);

                    favoritos.add(favorito);

                    if (!categoria.isBlank()) {
                        categorias.put(
                                favorito.nombre().toLowerCase(), categoria);
                    }
                }
            }

        } catch (IOException e) {
            favoritos.clear();
            categorias.clear();
        }

        return new ArrayList<>(favoritos);
    }

    public List<FavoritoQuery> obtenerPorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return new ArrayList<>();
        }

        return favoritos.stream()
                .filter(f -> categoria.equalsIgnoreCase(
                        categorias.get(f.nombre().toLowerCase())))
                .toList();
    }

    public String obtenerCategoria(FavoritoQuery favorito) {
        if (favorito == null) {
            return "";
        }

        return categorias.getOrDefault(
                favorito.nombre().toLowerCase(), "");
    }

    private void guardarEnArchivo() {
        try {
            ArrayNode datos = mapper.createArrayNode();

            for (FavoritoQuery favorito : favoritos) {
                ObjectNode nodo = mapper.valueToTree(favorito);

                nodo.put("categoria",
                        categorias.getOrDefault(
                                favorito.nombre().toLowerCase(), ""));

                datos.add(nodo);
            }

            mapper.writeValue(archivoPersistencia, datos);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al guardar las consultas favoritas en JSON", e);
        }
    }
}
