package edu.sisinf.chuspadb.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tabla {
    private String nombre;
    private String esquema;
    private List<ColumnaInfo> columnas;

    // Constructor sin argumentos requerido por Jackson
    public Tabla() {
    }

    // Constructor con todos los campos e invariantes validadas
    public Tabla(String nombre, String esquema, List<ColumnaInfo> columnas) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la tabla no puede ser nulo ni vacío");
        }
        if (esquema == null || esquema.trim().isEmpty()) {
            throw new IllegalArgumentException("El esquema de la tabla no puede ser nulo ni vacío");
        }
        this.nombre = nombre;
        this.esquema = esquema;
        this.columnas = (columnas != null) ? new ArrayList<>(columnas) : new ArrayList<>();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEsquema() {
        return esquema;
    }

    public void setEsquema(String esquema) {
        this.esquema = esquema;
    }

    public List<ColumnaInfo> getColumnas() {
        return columnas;
    }

    public void setColumnas(List<ColumnaInfo> columnas) {
        this.columnas = columnas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tabla)) {
            return false;
        }
        Tabla tabla = (Tabla) o;
        return Objects.equals(nombre, tabla.nombre) &&
                Objects.equals(esquema, tabla.esquema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, esquema);
    }

    @Override
    public String toString() {
        int totalColumnas = (columnas != null) ? columnas.size() : 0;
        return "Tabla{" +
                "nombre='" + nombre + '\'' +
                ", esquema='" + esquema + '\'' +
                ", columnas=" + totalColumnas +
                '}';
    }
}
