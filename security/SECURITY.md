# Política de Seguridad

## Reportar una vulnerabilidad

Si encuentras una vulnerabilidad de seguridad:

1. **No** crees un issue público
2. Contacta directamente al mantenedor del proyecto
3. Incluye descripción detallada del problema
4. Espera confirmación antes de divulgar

## Análisis de seguridad

Este proyecto ejecuta análisis automático de seguridad
en cada PR mediante GitHub Actions.

## Versiones soportadas

| Versión | Soportada |
|---|---|
| latest | ✅ |

## Seguridad en el Acceso a Datos y Drivers JDBC

### Drivers JDBC como superficie de ataque

Los drivers JDBC (como **MySQL Connector/J** y **PostgreSQL JDBC Driver**) son componentes que permiten que la aplicación se comunique con la base de datos. Como estos drivers establecen conexiones de red y procesan información proveniente de sistemas externos, es importante considerarlos parte de la seguridad de la aplicación.

Si un driver tiene errores o vulnerabilidades conocidas, la aplicación podría verse afectada. Por esta razón, es importante utilizar versiones mantenidas y actualizadas.

### Recomendaciones

* Mantener siempre actualizados los drivers JDBC utilizados por el proyecto.
* Revisar periódicamente si existen nuevas versiones que corrijan problemas de seguridad.
* Configurar tiempos de espera (*timeouts*) para evitar que conexiones o consultas queden bloqueadas indefinidamente.
* Utilizar conexiones seguras mediante SSL/TLS cuando la base de datos se encuentre en otro equipo o servidor.

#### MySQL

Se recomienda habilitar SSL utilizando parámetros como:

```text
sslMode=VERIFY_CA
```

o

```text
sslMode=VERIFY_IDENTITY
```

#### PostgreSQL

Se recomienda habilitar SSL utilizando parámetros como:

```text
ssl=true
sslmode=verify-ca
```

o

```text
sslmode=verify-full
```

### Prevención de SQL Injection con PreparedStatement

La inyección SQL (*SQL Injection*) es un ataque que ocurre cuando datos proporcionados por un usuario son insertados directamente dentro de una consulta SQL.

Por ejemplo, construir consultas mediante concatenación de texto puede ser peligroso:

```java
String sql = "SELECT * FROM usuarios WHERE nombre = '" + nombre + "'";
```

Para evitar este problema, el proyecto utiliza **PreparedStatement**.

```java
String sql = "SELECT * FROM usuarios WHERE nombre = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, nombre);
```

### ¿Por qué es más seguro?

Con `PreparedStatement`, los datos enviados por el usuario se manejan como valores y no como parte del código SQL. Esto evita que un atacante pueda modificar la consulta agregando instrucciones maliciosas.

Por esta razón, siempre se recomienda:

* Utilizar `PreparedStatement` en lugar de concatenar cadenas.
* Validar los datos recibidos cuando sea necesario.
* Evitar construir consultas SQL directamente con información proporcionada por los usuarios.
