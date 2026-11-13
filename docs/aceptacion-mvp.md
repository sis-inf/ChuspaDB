# Criterios de Aceptación del MVP v0.1.0 - Proyecto ChuspaDB

Este documento define los escenarios funcionales que deben verificarse antes de publicar el primer release de ChuspaDB (versión `0.1.0` en `pom.xml`). Cada escenario indica el requisito que cubre, el procedimiento, el resultado esperado y la evidencia necesaria para darlo por aprobado.

Un escenario solo se marca como aprobado cuando se adjunta su evidencia. Sin evidencia, el escenario queda pendiente.

## 1. Preparación del entorno

- Java 17 y Maven instalados (ver `docs/instalacion.md`).
- Código actualizado desde la rama `dev`.
- Una base de datos de prueba por cada motor soportado (`TipoMotor`: `SQLITE`, `MYSQL`, `POSTGRESQL`), con al menos una tabla con datos.
- Comando de arranque: `mvn javafx:run`.

## 2. Escenarios

### E-01: Inicio de la aplicación
- **Requisito:** base para todos los demás escenarios.
- **Procedimiento:**
  1. Ejecutar `mvn clean compile`.
  2. Ejecutar `mvn javafx:run`.
- **Resultado esperado:** la compilación termina sin errores y se abre la ventana principal con el título «ChuspaDB», el menú «Archivo» (con «Nueva conexión...» y «Salir»), el menú «Ayuda», el panel de conexiones, el editor SQL con el texto «Sin conexión» y el panel de resultados con el estado «Listo».
- **Evidencia:** salida de consola de ambos comandos y captura de la ventana principal recién abierta.

### E-02: Conexión a bases de datos
- **Requisito:** RF-001 (PostgreSQL) y RF-009 (seleccionar diferentes bases de datos).
- **Procedimiento:**
  1. Abrir «Archivo > Nueva conexión...».
  2. Completar Nombre, Motor, Host, Puerto, Base de datos, Usuario y Password.
  3. Pulsar «Probar conexión».
  4. Pulsar «Guardar» y hacer doble clic sobre la conexión en el panel de conexiones.
  5. Repetir para SQLite, MySQL y PostgreSQL.
- **Resultado esperado:** «Probar conexión» muestra «✅ Conexión exitosa (N ms)»; la conexión guardada aparece en el panel de conexiones y, al abrirla, el editor SQL muestra el nombre de la conexión activa en lugar de «Sin conexión».
- **Evidencia:** una captura por motor del mensaje de conexión exitosa y del editor con la conexión activa.

### E-03: Exploración de tablas
- **Requisito:** RF-002 (mostrar las tablas disponibles) y RF-013 (información de columnas y tablas).
- **Procedimiento:**
  1. Abrir una conexión con doble clic en el panel de conexiones.
  2. Expandir el nodo de la conexión y luego un esquema.
  3. Seleccionar una tabla.
- **Resultado esperado:** debajo de la conexión se listan sus esquemas; dentro de cada esquema, las tablas de la base de prueba; al seleccionar una tabla se ven sus columnas con su tipo.
- **Evidencia:** captura del árbol expandido mostrando esquemas y tablas, y captura de la información de columnas de una tabla.

### E-04: Ejecución de consultas SQL
- **Requisito:** RF-003 (ejecutar consultas SQL).
- **Procedimiento:**
  1. Con una conexión activa, escribir en el editor `SELECT * FROM <tabla_de_prueba>;`.
  2. Ejecutar con el botón «Ejecutar (Ctrl+Enter)» o con Ctrl+Enter.
  3. Ejecutar una sentencia de escritura sobre la base de prueba (por ejemplo, un `UPDATE` sobre una fila de prueba).
- **Resultado esperado:** la consulta de lectura llena el panel de resultados; la sentencia de escritura informa «N filas afectadas (T ms)». Sin conexión activa, la aplicación muestra el aviso «No existe conexión activa» y no ejecuta nada.
- **Evidencia:** captura de cada ejecución con el editor y el panel de resultados visibles, y captura del aviso sin conexión activa.

### E-05: Visualización de resultados
- **Requisito:** RF-004 (resultados en formato tabular) y RF-008 (mensajes claros ante errores en consultas).
- **Procedimiento:**
  1. Ejecutar un `SELECT` que devuelva varias filas.
  2. Ejecutar una consulta con un error de sintaxis.
- **Resultado esperado:** el resultado se muestra en una tabla con una columna por cada columna de la consulta, con paginación cuando hay más filas que las de una página, y el estado «N filas (T ms)». La consulta con error muestra «Error: <mensaje>» y deja deshabilitado el botón de exportar.
- **Evidencia:** captura de la tabla de resultados con el estado visible y captura del mensaje de error.

### E-06: Exportación CSV/JSON
- **Requisito:** RF-012 (exportar a CSV); la exportación a JSON la cubre `ExportadorJSON`.
- **Procedimiento:**
  1. Ejecutar un `SELECT` con resultados.
  2. Pulsar «Exportar CSV...» y elegir «Exportar a CSV...»; guardar el archivo.
  3. Repetir eligiendo «Exportar a JSON...».
  4. Abrir ambos archivos con un editor de texto.
- **Resultado esperado:** se generan los dos archivos; el CSV tiene una fila de encabezados con los nombres de las columnas y una fila por registro; el JSON contiene las mismas columnas y filas. La cantidad de filas coincide con la mostrada en el panel de resultados.
- **Evidencia:** los dos archivos generados (o su contenido) y la captura del panel de resultados del que salieron.

### E-07: Manejo de errores de conexión
- **Requisito:** RF-008 (mensajes claros ante errores).
- **Procedimiento:**
  1. En «Nueva conexión», ingresar un host, puerto o password incorrecto y pulsar «Probar conexión».
  2. Guardar una conexión con datos incorrectos y abrirla con doble clic.
- **Resultado esperado:** la prueba muestra «❌ No se pudo conectar: <motivo>»; al abrir la conexión se muestra la ventana «Error de conexión» con el texto «No se pudo abrir la conexión» y el motivo. La aplicación sigue abierta y usable.
- **Evidencia:** captura de ambos mensajes de error.

## 3. Condiciones para aprobar el MVP v0.1.0

El MVP v0.1.0 se aprueba solo si se cumplen todas estas condiciones:

- Los siete escenarios (E-01 a E-07) están aprobados con su evidencia adjunta.
- E-02, E-03, E-04 y E-05 se verificaron en los tres motores soportados (SQLite, MySQL y PostgreSQL).
- Se cumplen los criterios de salida de `tests/plan/criterios-de-salida.md`, incluida la cobertura mínima de 80% en pruebas unitarias que fija `tests/plan/plan-de-pruebas.md`.
- No queda abierto ningún bug de severidad Crítica o Alta relacionado con estos escenarios.

## 4. Registro de verificación

| Escenario | SQLite | MySQL | PostgreSQL | Evidencia | Estado |
|-----------|--------|-------|------------|-----------|--------|
| E-01 Inicio de la aplicación | - | - | - | | Pendiente |
| E-02 Conexión a bases de datos | | | | | Pendiente |
| E-03 Exploración de tablas | | | | | Pendiente |
| E-04 Ejecución de consultas SQL | | | | | Pendiente |
| E-05 Visualización de resultados | | | | | Pendiente |
| E-06 Exportación CSV/JSON | | | | | Pendiente |
| E-07 Manejo de errores de conexión | | | | | Pendiente |
