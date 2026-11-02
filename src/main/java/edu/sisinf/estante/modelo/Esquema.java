package edu.sisinf.chuspadb.modelo;

import java.util.List;

public class Esquema {
    private String nombre;
    private List<Tabla> tablas;

    //Constructor sin argumentos requerido por Jackson
    public Esquema() {}

    //Cosntructor con todos los campos
    public Esquema(String nombre, List<Tabla> tablas) {
        this.nombre = nombre;
        this.tablas = tablas;
    }

    //Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Tabla> getTablas() { return tablas; }
    public void setTablas(List<Tabla> tablas) { this.tablas = tablas; }
}
