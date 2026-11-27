package edu.sisinf.chuspadb.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Esquema {
    private String nombre;
    private List<Tabla> tablas;

    // Constructor sin argumentos requerido por Jackson
    public Esquema() {
    }

    // Constructor con todos los campos e invariantes validadas
    public Esquema(String nombre, List<Tabla> tablas) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del esquema no puede ser nulo ni vacío");
        }
        this.nombre = nombre;
        this.tablas = (tablas != null) ? new ArrayList<>(tablas) : new ArrayList<>();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Tabla> getTablas() {
        return tablas;
    }

    public void setTablas(List<Tabla> tablas) {
        this.tablas = tablas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Esquema)) {
            return false;
        }
        Esquema esquema = (Esquema) o;
        return Objects.equals(nombre, esquema.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        int totalTablas = (tablas != null) ? tablas.size() : 0;
        return "Esquema{" +
                "nombre='" + nombre + '\'' +
                ", tablas=" + totalTablas +
                '}';
    }
}
