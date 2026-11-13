# Validación de PostgreSQL — MVP v0.1.0

## 1. Objetivo
Verificar que ChuspaDB se conecta a una base de datos PostgreSQL de prueba, lista sus tablas, ejecuta una consulta `SELECT`, muestra los resultados y maneja los errores de conexión, como parte de los criterios de aceptación del MVP v0.1.0.

## 2. Datos de la ejecución

| Dato | Valor |
|---|---|
| Fecha | 2026-11-11 |
| Versión probada | rama `dev`, commit `c2decb0` (versión `0.1.0` en `pom.xml`) |
| Ejecutado por | Gabriel Menacho |
| Tipo de prueba | Integración (funcional) |
| Motor | PostgreSQL 17.11 en Docker (imagen `postgres:17`) |
| Driver JDBC | `org.postgresql:postgresql` 42.7.0 (el de `pom.xml`) |
| Java | OpenJDK 17.0.20.1 |

## 3. Preparación del entorno

### 3.1 Base de datos de prueba
```bash
docker run -d --name chuspadb-pg-prueba \
  -e POSTGRES_USER=chuspa -e POSTGRES_PASSWORD=chuspa123 \
  -e POSTGRES_DB=chuspadb_prueba -p 55432:5432 postgres:17
```

Datos cargados con `psql` dentro del contenedor:
```sql
CREATE TABLE clientes (id SERIAL PRIMARY KEY, nombre VARCHAR(50) NOT NULL, ciudad VARCHAR(50));
CREATE TABLE pedidos (id SERIAL PRIMARY KEY, cliente_id INT REFERENCES clientes(id), total NUMERIC(10,2));
INSERT INTO clientes (nombre, ciudad) VALUES ('Ana Quispe','La Paz'),('Luis Mamani','Cochabamba'),('Rosa Flores','Santa Cruz');
INSERT INTO pedidos (cliente_id, total) VALUES (1, 150.50),(2, 89.90);
```

### 3.2 Datos de conexión

| Campo | Valor |
|---|---|
| Motor | PostgreSQL |
| Host | localhost |
| Puerto | 55432 |
| Base de datos | chuspadb_prueba |
| Usuario | chuspa |
| Password | chuspa123 |

Pasos para conectarse desde la aplicación (cuando `dev` compile):

1. Ejecutar `mvn javafx:run`.
2. Abrir «Archivo > Nueva conexión...».
3. Elegir el motor PostgreSQL y completar Host, Puerto, Base de datos, Usuario y Password con los valores de la tabla.
4. Pulsar «Probar conexión»: debe mostrar «Conexión exitosa».
5. Pulsar «Guardar» y hacer doble clic sobre la conexión en el panel de conexiones para abrirla.

### 3.3 Cómo se ejecutó
En `dev` el proyecto no compila (ver sección 6), así que la aplicación no se puede abrir con `mvn javafx:run`. Las pruebas se ejecutaron llamando directamente a las clases reales de la aplicación que usa la interfaz:

- `ConexionDAOPostgreSQL.abrir()` para la conexión (la misma que usa `App.abrirConexion`).
- `ExploradorEsquemas.listarEsquemas()` y `listarTablas()` para la exploración.
- `EjecutorQuery.ejecutar()` para la consulta y `ResultadoQuery` para los resultados.

Para compilar esas clases fuera del repositorio se copió `src/main/java` a una carpeta temporal y, solo en esa copia, se renombró `core/ErrorEstante.java` a `core/ErrorChuspaDB.java`. El repositorio no se modificó.

Los datos de conexión se cargaron en el mismo orden que `DialogoNuevaConexionController`: primero el motor y después el puerto.

## 4. Casos de prueba

| ID | Caso | Pasos | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|
| PG-01 | Conexión a PostgreSQL | Abrir la conexión con los datos de 3.2 | La conexión se abre sin errores | Conexión abierta en 650 ms contra PostgreSQL 17.11, URL `jdbc:postgresql://localhost:55432/chuspadb_prueba` | Aprobado |
| PG-02 | Visualización de tablas | Listar los esquemas de la conexión y las tablas de cada uno | Se ven `clientes` y `pedidos` dentro de `chuspadb_prueba` | Se ven `clientes` y `pedidos` dentro de `chuspadb_prueba`, pero también aparecen repetidas dentro de `postgres` y `template1`, que no tienen tablas | Fallido |
| PG-03 | Ejecución de `SELECT` | Ejecutar `SELECT id, nombre, ciudad FROM clientes ORDER BY id` | La consulta se ejecuta como lectura | Tipo `LECTURA`, ejecutada en 5 ms | Aprobado |
| PG-04 | Visualización de resultados | Revisar columnas y filas del resultado de PG-03 | Columnas `id`, `nombre`, `ciudad` y las 3 filas cargadas | Columnas `[id, nombre, ciudad]` y las 3 filas con sus valores (ver evidencia) | Aprobado (capa de servicio) |
| PG-05 | Error de conexión | Intentar conectar con password incorrecta, con un puerto sin servidor y con password vacía | Se informa el motivo del error y no se abre la conexión | Password incorrecta: `password authentication failed for user`; puerto sin servidor: `Connection to localhost:55499 refused`; password vacía: `La contraseña de PostgreSQL no puede estar vacía.` | Aprobado (capa de servicio) |

PG-04 y PG-05 quedan aprobados solo en la capa de servicio: falta verlos en la interfaz (tabla de resultados y ventana «Error de conexión»), lo que hoy no se puede hacer porque la aplicación no compila.

## 5. Evidencias

Salida literal de la ejecución (sin las líneas de aviso de SLF4J):

```text
== P1 Conexion ==
URL: jdbc:postgresql://localhost:55432/chuspadb_prueba
Conexion abierta: true (650 ms)
Servidor: PostgreSQL 17.11 (Debian 17.11-1.pgdg13+2)
== P2 Esquemas y tablas ==
Esquema: chuspadb_prueba
  Tabla: clientes
  Tabla: pedidos
Esquema: postgres
  Tabla: clientes
  Tabla: pedidos
Esquema: template1
  Tabla: clientes
  Tabla: pedidos
== P3/P4 SELECT y resultados ==
Tipo: LECTURA
Columnas: [id, nombre, ciudad]
  Fila: [1, Ana Quispe, La Paz]
  Fila: [2, Luis Mamani, Cochabamba]
  Fila: [3, Rosa Flores, Santa Cruz]
Total filas: 3 (5 ms)
== P5 Errores de conexion ==
Password incorrecta -> PSQLException: FATAL: password authentication failed for user "chuspa"
Puerto sin servidor -> PSQLException: Connection to localhost:55499 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Password vacia -> ErrorConexion: La contraseña de PostgreSQL no puede estar vacía.
```

Comprobación de que `postgres` y `template1` no tienen tablas (PG-02):

```text
$ docker exec chuspadb-pg-prueba psql -U chuspa -d postgres -c '\dt'
Did not find any relations.
$ docker exec chuspadb-pg-prueba psql -U chuspa -d template1 -c '\dt'
Did not find any relations.
```

## 6. Defectos y observaciones

| ID | Descripción | Severidad | Evidencia |
|---|---|---|---|
| OBS-01 | `dev` no compila: `core/ErrorEstante.java` declara la clase pública `ErrorChuspaDB`; además fallan `PanelEditorSQLController.java` (paquete `org.fxmisc.richtext` no visible) y `HistorialQuerys.java` (símbolos no encontrados). Bloquea abrir la aplicación. | Bloqueante | Salida de `mvn compile` |
| OBS-02 | En PostgreSQL, `ExploradorEsquemas.listarEsquemas()` devuelve todas las bases del servidor (`getCatalogs`) y `listarTablas()` devuelve las tablas de la base conectada para cualquiera de ellas, por lo que las tablas aparecen repetidas en bases que no las tienen. | Media | PG-02 y sección 5 |
| OBS-03 | `Conexion.setTipoMotor()` reinicia el puerto (lo deja en `null` para PostgreSQL). Si el puerto se asigna antes que el motor, se pierde y se usa el 5432. El diálogo de nueva conexión lo hace en el orden correcto. | Baja | `Conexion.java`, `setTipoMotor` |

Errores de `mvn compile` en `dev` (OBS-01):

```text
[ERROR] src/main/java/edu/sisinf/estante/core/ErrorEstante.java:[7,8] class ErrorChuspaDB is public, should be declared in a file named ErrorChuspaDB.java
[ERROR] src/main/java/edu/sisinf/estante/controller/PanelEditorSQLController.java:[12,18] package org.fxmisc.richtext is not visible
[ERROR] src/main/java/edu/sisinf/estante/controller/PanelEditorSQLController.java:[13,27] package org.fxmisc.richtext.model is not visible
[ERROR] src/main/java/edu/sisinf/estante/controller/PanelEditorSQLController.java:[14,27] package org.fxmisc.richtext.model is not visible
[ERROR] src/main/java/edu/sisinf/estante/servicio/HistorialQuerys.java:[90,55] cannot find symbol
[ERROR] src/main/java/edu/sisinf/estante/servicio/HistorialQuerys.java:[106,24] cannot find symbol
```

## 7. Resultado

| Casos | Aprobados | Fallidos |
|---|---|---|
| 5 | 4 (PG-04 y PG-05 solo en la capa de servicio) | 1 (PG-02) |

La conexión, la consulta `SELECT`, los resultados y los errores de conexión funcionan con PostgreSQL en la capa de servicio. La validación queda pendiente en la interfaz hasta que `dev` compile (OBS-01), y la exploración de tablas necesita corregir OBS-02 antes de aprobar el MVP v0.1.0.
