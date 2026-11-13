# Matriz de Trazabilidad del MVP v0.1.0 - Proyecto ChuspaDB

Este documento relaciona cada requisito funcional de `docs/requerimientos.md` con los casos de prueba que ya existen en el repositorio, los issues relacionados, su estado de verificación y la evidencia que hace falta para darlo por aprobado.

Fuentes usadas:

- Requisitos: `docs/requerimientos.md` (RF-001 a RF-015).
- Casos de prueba funcionales: `tests/casos/funcionales/casos-prueba-funcionales.md` (CP-001 a CP-010).
- Pruebas unitarias JUnit: `src/test/java/edu/sisinf/estante/`.
- Resultados de ejecución: issue #1341 (validación de PostgreSQL).

Funcionalidades principales del MVP: los requisitos con prioridad Alta en `docs/requerimientos.md`, que son conectar a PostgreSQL (RF-001), mostrar las tablas (RF-002), ejecutar consultas SQL (RF-003), mostrar los resultados en formato tabular (RF-004) y validar errores en consultas con mensajes claros (RF-008). El resto de los requisitos (prioridad Media o Baja) también se incluyen en la matriz.

## 1. Estados de verificación

| Estado | Significado |
|---|---|
| Pendiente | No hay evidencia de ejecución que permita aprobar el requisito, o falta un caso de prueba. |
| Aprobado | El caso de prueba se ejecutó, se adjuntó la evidencia y el resultado coincide con lo esperado. |
| Fallido | El caso de prueba se ejecutó y el resultado no coincide con lo esperado. |

Ningún requisito se marca como aprobado sin evidencia. Las pruebas JUnit existentes cuentan como caso de prueba asociado, pero no como evidencia: en la rama `dev` el proyecto no compila, por lo que `mvn test` no se puede ejecutar (ver sección 3).

## 2. Matriz

| Requisito | Funcionalidad | Caso funcional | Pruebas JUnit | Issue relacionado | Estado | Evidencia requerida |
|---|---|---|---|---|---|---|
| RF-001 | Conectar a una base de datos PostgreSQL mediante JDBC | CP-001 | `ConexionDAOPostgreSQLTest` | #1341, #1339 | Pendiente | Captura de «Probar conexión» con el mensaje de conexión exitosa en la interfaz. En #1341 la conexión se abrió correctamente usando las clases de la aplicación, sin interfaz. |
| RF-002 | Mostrar las tablas disponibles en la base de datos | CP-002 | Ninguna para `ExploradorEsquemas` | #1341, #1339 | Fallido | En #1341 (caso PG-02), con PostgreSQL las tablas de la base conectada aparecen repetidas dentro de otras bases del servidor que no tienen tablas. Se necesita una nueva ejecución con el defecto corregido. |
| RF-003 | Ejecutar consultas SQL ingresadas por el usuario | CP-003 | `EjecutorQueryTest`, `SqlValidatorTest`, `ConnectionFlowTest` | #1341, #1342, #1343, #1339 | Pendiente | Captura del editor SQL con un `SELECT` ejecutado en cada motor. En #1341 el `SELECT` funcionó en PostgreSQL sin interfaz. |
| RF-004 | Mostrar los resultados de las consultas en formato tabular | CP-004 | `ResultadoQueryTest`, `EjecutorQueryTest` | #1341, #1339 | Pendiente | Captura de la tabla de resultados con columnas, filas y el estado «N filas (T ms)». En #1341 se obtuvieron las columnas y filas correctas sin interfaz. |
| RF-005 | Permitir insertar nuevos registros en las tablas | CP-005 | `EjecutorQueryTest` (casos `insert`), `GeneradorSQLTest` | - | Pendiente | Captura del `INSERT` ejecutado y de la consulta que muestra el registro nuevo. |
| RF-006 | Permitir actualizar registros existentes | CP-006 | `EjecutorQueryTest` (casos `update`), `GeneradorSQLTest` | - | Pendiente | Captura del `UPDATE` ejecutado y de la consulta con el valor modificado. |
| RF-007 | Permitir eliminar registros en la base de datos | CP-007 | `EjecutorQueryTest` (casos `delete`), `GeneradorSQLTest` | - | Pendiente | Captura del `DELETE` ejecutado y de la consulta sin el registro. |
| RF-008 | Validar errores en consultas SQL y mostrar mensajes claros | CP-008 | `EjecutorQueryTest` (casos de error), `ErrorEstanteTest` | #1339 | Pendiente | Captura del mensaje «Error: ...» al ejecutar una consulta sobre una tabla inexistente. |
| RF-009 | Permitir seleccionar diferentes bases de datos | CP-009 | `ConexionDAOMySQLTest`, `ConexionDAOSQLiteTest`, `ConexionDAOPostgreSQLTest`, `TipoMotorTest` | #1341, #1342, #1343, #1339 | Pendiente | Captura de una conexión abierta por cada motor (SQLite, MySQL y PostgreSQL). |
| RF-010 | Guardar historial de consultas realizadas | CP-010 | `HistorialQuerysTest`, `EntradaHistorialTest` | - | Pendiente | Captura del panel de historial después de ejecutar varias consultas. |
| RF-011 | Permitir guardar consultas SQL como favoritas | Sin caso funcional | `FavoritoQueryTest` | - | Pendiente | Falta redactar el caso funcional; después, captura de una consulta guardada como favorita. |
| RF-012 | Permitir exportar resultados a formato CSV | Sin caso funcional | `ExportadorCSVTest` | #1339 | Pendiente | Falta redactar el caso funcional; después, el archivo CSV exportado y la captura del resultado de origen. |
| RF-013 | Mostrar información detallada de columnas y tablas | Sin caso funcional | `ColumnaInfoTest`, `TablaTest` | #1339 | Pendiente | Falta redactar el caso funcional; después, captura de la información de columnas de una tabla. |
| RF-014 | Permitir importar datos desde archivos CSV | Sin caso funcional | `ImportadorCSVTest` | - | Pendiente | Falta redactar el caso funcional; después, captura de la tabla con los datos importados desde `tests/datos/entrada/`. |
| RF-015 | Mostrar estadísticas básicas de la base de datos | Sin caso funcional | Ninguna | - | Pendiente | Falta redactar el caso funcional y crear pruebas; después, captura del panel de estadísticas. |

Issues relacionados (todos abiertos al redactar este documento):

| Issue | Tema |
|---|---|
| #1339 | Criterios de aceptación del MVP v0.1.0 (`docs/aceptacion-mvp.md`) |
| #1340 | Compilación Maven y generación del JAR |
| #1341 | Validación de conexión y consultas en PostgreSQL |
| #1342 | Validación de conexión y consultas en MySQL |
| #1343 | Validación de conexión y consultas en SQLite |

## 3. Resumen

| Estado | Requisitos |
|---|---|
| Aprobado | 0 |
| Fallido | 1 (RF-002) |
| Pendiente | 14 |

Observaciones:

- En `dev` el proyecto no compila (`mvn compile` falla en `core/ErrorEstante.java`, `controller/PanelEditorSQLController.java` y `servicio/HistorialQuerys.java`). Mientras eso no se corrija, no se pueden ejecutar las pruebas JUnit ni abrir la interfaz para obtener evidencias.
- RF-011 a RF-015 tienen pruebas JUnit (salvo RF-015) pero no tienen caso funcional en `tests/casos/funcionales/casos-prueba-funcionales.md`.
- RF-002 y RF-013 no tienen pruebas JUnit de `ExploradorEsquemas`, la clase que lista tablas y columnas.
