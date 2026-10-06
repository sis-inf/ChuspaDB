package edu.sisinf.estante.servicio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.sisinf.estante.config.ConfiguracionApp;
import edu.sisinf.estante.modelo.FavoritoQuery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de gestionar el ciclo de vida de las consultas favoritas,
 * manejando la persistencia local en un archivo JSON usando la librería Jackson básica.
 */
public class GestorFavoritos {

    private List<FavoritoQuery> favoritos;
    private final File archivoPersistencia;
    private final ObjectMapper mapper;

    public GestorFavoritos() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Utiliza ConfiguracionApp para centralizar la ubicación de almacenamiento
        ConfiguracionApp.asegurarDirectorioHome();

        this.archivoPersistencia =
                ConfiguracionApp.directorioHome()
                        .resolve("favoritos.json")
                        .toFile();

        this.favoritos = new ArrayList<>();

        this.favoritos = obtenerTodos();
    }

    public void agregar(FavoritoQuery favorito) {
        if (favorito == null) {
            throw new IllegalArgumentException("El favorito no puede ser nulo");
        }

        boolean duplicado = favoritos.stream()
                .anyMatch(f -> f.nombre().equalsIgnoreCase(favorito.nombre()));

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Ya existe una consulta guardada con el nombre: "
                            + favorito.nombre());
        }

        favoritos.add(favorito);
        guardarEnArchivo();
    }

    public void eliminar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }

        boolean removido = favoritos.removeIf(
                f -> f.nombre().equalsIgnoreCase(nombre));

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
        if (!archivoPersistencia.exists()
                || archivoPersistencia.length() == 0) {
            return new ArrayList<>();
        }

        try {
            this.favoritos = mapper.readValue(
                    archivoPersistencia,
                    new TypeReference<List<FavoritoQuery>>() {}
            );

            return new ArrayList<>(this.favoritos);

        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void guardarEnArchivo() {
        try {
            mapper.writeValue(archivoPersistencia, favoritos);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al guardar las consultas favoritas en el archivo JSON",
                    e);
        }
    }
}
