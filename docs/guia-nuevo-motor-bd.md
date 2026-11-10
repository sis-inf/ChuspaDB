# Guía para agregar soporte a un nuevo motor de base de datos

## Introducción

ChuspaDB permite conectarse a distintos motores de bases de datos a través de una arquitectura basada en DAOs (Data Access Objects, u Objetos de Accesi a Datos). Actualmente cuenta con implementaciones para MySQL, PostgreSQL y SQLite.

Esta guía describe el proceso recomendado para incorporar soporte a un nuevo motor de base de datos, siguiendo el patrón utilizado por las implementaciones existentes y evitando los errores de configuración más comunes.

---

# Requisitos previos

Antes de comenzar, se recomienda:

- Tener configurado el entorno de desarrollo.
- Conocer el funcionamiento de JDBC.
- Revisar las implementaciones existentes de MySQL, PostgreSQL o SQLite como referencia.

---

# Paso 1. Agregar el driver JDBC

El primer paso consiste en agregar la dependencia correspondiente al nuevo motor en el archivo `pom.xml`.

Por ejemplo, para Oracle o SQL Server se debe incluir el driver JDBC oficial proporcionado por el fabricante.

Después de modificar el archivo, ejecutar:

```bash
mvn clean install
```

para descargar automáticamente las nuevas dependencias.

---

# Paso 2. Agregar el nuevo valor al enum TipoMotor

El siguiente paso consiste en registrar el nuevo motor dentro del enum `TipoMotor`.

Esto permitirá que el sistema reconozca el nuevo tipo de base de datos y pueda seleccionarlo desde la aplicación.

Ejemplo:

```java
MYSQL,
POSTGRESQL,
SQLITE,
ORACLE
```

---

# Paso 3. Implementar IConexionDAO

Crear una nueva implementación de `IConexionDAO` siguiendo el mismo patrón utilizado por los DAOs existentes.

La nueva clase debe encargarse de:

- Abrir conexiones.
- Cerrar conexiones.
- Ejecutar consultas.
- Manejar errores propios del motor.

Se recomienda mantener la misma estructura y organización que utilizan los demás DAOs del proyecto.

---

# Paso 4. Registrar la implementación en App.java (Paso crítico)

> **⚠️ Este es el paso más importante del proceso.**

Una vez creada la implementación del DAO, es obligatorio registrarla en el mapa de DAOs de `App.java`.

Si este paso se omite, el nuevo motor nunca será utilizado por la aplicación, aunque todo el código compile correctamente.

## Advertencia

Durante el desarrollo del proyecto ocurrió un error con PostgreSQL: la implementación existía, pero nunca fue registrada en `App.java`.

Como consecuencia:

- La aplicación no podía utilizar PostgreSQL.
- El motor no aparecía disponible.
- Fue necesario corregir el registro manualmente.

Por esta razón, siempre debe verificarse que el nuevo DAO esté correctamente registrado antes de finalizar el desarrollo.

---

# Verificación

Antes de considerar terminado el soporte para un nuevo motor, comprobar que:

- El proyecto compila correctamente.
- El driver JDBC fue descargado.
- El nuevo valor aparece en `TipoMotor`.
- La implementación de `IConexionDAO` funciona correctamente.
- El DAO fue registrado en `App.java`.
- La aplicación puede establecer una conexión exitosa.

---

# Buenas prácticas

Se recomienda:

- Reutilizar la estructura de los DAOs existentes.
- Evitar duplicar código.
- Documentar cualquier comportamiento específico del motor.
- Probar la conexión antes de abrir un Pull Request.

---

# Conclusión

Agregar soporte para un nuevo motor de base de datos consiste principalmente en incorporar el driver JDBC correspondiente, registrar el nuevo tipo de motor, implementar el DAO respectivo y, especialmente, registrar dicha implementación en `App.java`. Documentar este procedimiento ayuda a evitar errores de configuración -como el ocurrido anteriormente con PostgreSQL- y facilita la incorporación de nuevos motores en el futuro.