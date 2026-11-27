package edu.sisinf.chuspadb.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa los datos de conexion necesarios para establecer
 * una sesion de base de datos.
 */
public class Conexion {

    private String id;
    private String nombre;
    private String host;
    private Integer puerto;
    private String basedatos;
    private String usuario;
    private String password;
    private TipoMotor tipoMotor;
    private boolean usarSSL = false;

    /**
     * Lista de etiquetas para organizar conexiones por categoría.
     * Por defecto es una lista vacía para compatibilidad con versiones anteriores.
     */
    private List<String> etiquetas = new ArrayList<>();

    public Conexion() {  
    }

    public Conexion(String nombre, String host, Integer puerto,
                    String basedatos, String usuario, String password) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.host = Objects.requireNonNull(host, "El host no puede ser nulo");
        this.puerto = Objects.requireNonNull(puerto, "El puerto no puede ser nulo");
        this.basedatos = Objects.requireNonNull(basedatos, "La base de datos no puede ser nula");
        this.usuario = Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
        this.password = Objects.requireNonNull(password, "La contraseña no puede ser nula");
    }

    public String getId()                  { return id; }
    public void setId(String id)           { this.id = id; }

    public String getNombre()              { return nombre; }
    public void setNombre(String nombre)   { this.nombre = nombre; }

    public String getHost()                { return host; }
    public void setHost(String host)       { this.host = host; }

    public Integer getPuerto()             { return puerto; }
    public void setPuerto(Integer puerto)  { this.puerto = puerto; }

    public String getBasedatos()           { return basedatos; }
    public void setBasedatos(String bd)    { this.basedatos = bd; }

    public String getUsuario()             { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }

    public TipoMotor getTipoMotor()             { return tipoMotor; }
    public void setTipoMotor(TipoMotor motor)   
    { this.tipoMotor = motor;
      if(motor == TipoMotor.MYSQL)
      {
          this.puerto = 3306;
      }else
      {
          this.puerto = null;
      }
    }

    /**
     * Indica si la conexión debe usar SSL/TLS.
     * @return true si SSL está habilitado, false por defecto.
     */
    public boolean isUsarSSL()             { return usarSSL; }
    public void setUsarSSL(boolean ssl)    { this.usarSSL = ssl; }

    /**
     * Devuelve la lista de etiquetas de la conexión.
     * @return lista de etiquetas, nunca null.
     */
    public List<String> getEtiquetas() {
        return etiquetas;
    }

    /**
     * Establece la lista de etiquetas de la conexión.
     * @param etiquetas lista de etiquetas.
     */
    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas != null ? etiquetas : new ArrayList<>();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
    
        if (!(obj instanceof Conexion)) {
            return false;
        }
    
        Conexion otra = (Conexion) obj;
    
        return id != null && id.equals(otra.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    @Override
    public String toString() {
        return "Conexion{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", host='" + host + '\'' +
                ", puerto=" + puerto +
                ", basedatos='" + basedatos + '\'' +
                ", usuario='" + usuario + '\'' +
                ", tipoMotor=" + tipoMotor +
                ", usarSSL=" + usarSSL +
                '}';
    }
    /**
     * Crea una copia de la conexión actual con un nuevo nombre.
     * @param nuevoNombre El nombre que tendrá la nueva conexión clonada.
     * @return Una nueva instancia de Conexion con los mismos datos pero nombre diferente.
     * @throws IllegalArgumentException si el nuevoNombre es nulo o está vacío.
     */
    public Conexion copiar(String nuevoNombre) {
        // Criterio de aceptación: nuevoNombre null o vacío lanza IllegalArgumentException
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo nombre de la conexión no puede ser nulo o vacío.");
        }

        // Creamos la nueva instancia
        Conexion copia = new Conexion();
        
        // Mapeamos los campos usando los nombres exactos de tu clase
        copia.setNombre(nuevoNombre);
        copia.setHost(this.host);
        copia.setTipoMotor(this.tipoMotor);
        copia.setPuerto(this.puerto);
        copia.setBasedatos(this.basedatos);
        copia.setUsuario(this.usuario);
        copia.setPassword(this.password);
        copia.setUsarSSL(this.usarSSL);
        
        // Criterio de aceptación: No debe compartir estado mutable.
        // Creamos una nueva lista conteniendo los elementos de la lista original
        copia.setEtiquetas(new ArrayList<>(this.etiquetas));
        
        return copia;
    }
}
