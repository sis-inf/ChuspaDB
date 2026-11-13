# Flujo de ejecución de una query

Este documento describe el ciclo de vida de una consulta SQL desde que el usuario la ejecuta hasta que visualiza los resultados.

## Flujo principal

```text
┌──────────────────────┐
│ Usuario escribe SQL  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ SqlValidator verifica│
│ la consulta          │
└──────────┬───────────┘
           │
           ▼
     ¿Es una DML?
      /        \
    Sí          No
    │            │
    ▼            │
┌──────────────────────┐
│ DialogoConfirmacion  │
│ DML solicita         │
│ confirmación         │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ EjecutorQueryAsync   │
│ ejecuta en segundo   │
│ plano                │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ HistorialQuerys      │
│ .agregar()           │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ PanelResultadoQuery  │
│ Controller muestra   │
│ resultados           │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ BarraEstadoController│
│ actualiza estado     │
└──────────────────────┘
```

## Flujo de error

```text
┌──────────────────────┐
│ Usuario ejecuta SQL  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ SqlValidator o       │
│ EjecutorQueryAsync   │
│ detecta un error     │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Se registra el error │
│ en el historial      │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Se muestra mensaje   │
│ al usuario           │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ BarraEstadoController│
│ informa el fallo     │
└──────────────────────┘
```

## Resumen

1. El usuario escribe una consulta SQL.
2. La consulta es validada por `SqlValidator`.
3. Si la operación corresponde a una instrucción DML, se solicita confirmación mediante `DialogoConfirmacionDML`.
4. La ejecución se realiza mediante `EjecutorQueryAsync` en un hilo de segundo plano.
5. La consulta se registra en el historial mediante `HistorialQuerys.agregar()`.
6. Los resultados se muestran en `PanelResultadoQueryController`.
7. `BarraEstadoController` actualiza el estado de la operación.
8. Ante errores, se informa al usuario y se actualiza el estado correspondiente.

## Etapas del ciclo de vida

Recorrido de una consulta desde el editor JavaFX hasta que su resultado llega a la interfaz, según el código actual de `App`, `PanelEditorSQLController` y el paquete `servicio`.

### 1. Validación preliminar

- `PanelEditorSQLController.disparar()` toma el texto del editor y no hace nada si está vacío.
- Si `SqlValidator.esDestructiva(sql)` detecta una sentencia destructiva (cualquier `DELETE` o `DROP`, o un `UPDATE` sin `WHERE`), se pide confirmación con `DialogoConfirmacionDML.confirmar(sql)`; si el usuario cancela, la consulta no se ejecuta.
- `App.ejecutarSQL(sql)` comprueba que exista una conexión activa (si no, muestra «Sin conexión» y oculta el spinner) y repite la confirmación de sentencias destructivas antes de delegar.
- Dentro de `EjecutorQuery`, `SqlValidator.tipo(sql)` decide si la sentencia es de lectura (`SELECT`) o de escritura.

### 2. Obtención de la conexión

- No hay pool de conexiones: `ConexionDAOPostgreSQL` lo indica de forma explícita («No implementa pooling de conexiones») y ninguna clase del proyecto implementa uno.
- Cada ejecución abre una conexión JDBC nueva con `IConexionDAO.abrir(conexion)` (implementado por `ConexionDAOMySQL`, `ConexionDAOPostgreSQL` y `ConexionDAOSQLite` con `DriverManager.getConnection`).
- La conexión se abre dentro de un try-with-resources en `EjecutorQueryAsync`, así que se cierra siempre al terminar, aunque haya error o cancelación.

### 3. Ejecución asíncrona con `Statement`

- `EjecutorQueryAsync.ejecutar(...)` crea un `javafx.concurrent.Task` y lo lanza en un hilo daemon, para que la interfaz no se bloquee y el hilo no impida cerrar la aplicación.
- La conexión se envuelve en un proxy que guarda el `Statement` creado (`statementActivo`), para poder cancelarlo desde el hilo de la interfaz.
- `EjecutorQuery.ejecutar(connection, sql)` crea el `Statement` con `createStatement()` y usa `executeQuery` para `SELECT` o `executeUpdate` para escrituras.

### 4. Recolección de metadatos y armado del resultado

- Para `SELECT`, `EjecutorQuery` lee `ResultSetMetaData`: `getColumnCount()` y `getColumnLabel(i)` dan los nombres de columna, y cada fila se arma con `rs.getObject(i)`.
- El tiempo se mide con `System.currentTimeMillis()` antes y después de ejecutar.
- El resultado se devuelve como `ResultadoQuery`: `deLectura(columnas, filas, tiempoMs)` o `deEscritura(filasAfectadas, tiempoMs)`.
- `QueryResult` (paquete `dto`) es el equivalente que devuelve `ConnectionProvider.executeSelect(connection, sql)`: solo columnas (con `getColumnName`) y filas, sin tiempo. El flujo del editor no lo usa; trabaja con `ResultadoQuery`.

### 5. Retorno a la interfaz y renderizado

- Al terminar el `Task`, `setOnSucceeded` o `setOnFailed` llaman a los callbacks con `Platform.runLater`, es decir, en el hilo de JavaFX.
- `App.manejarResultadoQuery(resultado)` pasa el resultado a `PanelResultadoQueryController.mostrar(resultado)`, que según el tipo arma las columnas de la `TableView` y pagina las filas (50 por página al inicio), muestra las filas afectadas o muestra «Error: ...».
- `BarraEstadoController` recibe el tiempo (`setTiempo`) y la cantidad de filas (`setFilas`) o el mensaje de error (`setMensaje`).
- Por último se llama a `editorController.ocultarSpinner()`, que rehabilita el botón Ejecutar.

## Rol de cada clase

| Clase | Paquete | Rol en la ejecución |
|---|---|---|
| `PanelEditorSQLController` | `controller` | Toma el SQL del editor, pide confirmación de sentencias destructivas y muestra u oculta el spinner. |
| `App` | raíz | Conecta los controladores; en `ejecutarSQL` verifica la conexión activa y delega en `EjecutorQueryAsync`; maneja el resultado y los errores. |
| `SqlValidator` | `util` | Clasifica la sentencia (`tipo`, `esLectura`) y detecta sentencias destructivas (`esDestructiva`). |
| `DialogoConfirmacionDML` | `controller` | Diálogo modal que confirma o cancela una sentencia destructiva. |
| `IConexionDAO` y sus implementaciones | `dao` | Abren la conexión JDBC del motor elegido (MySQL, PostgreSQL o SQLite). |
| `EjecutorQueryAsync` | `servicio` | Ejecuta en segundo plano, registra el `Statement` activo, permite cancelar y devuelve el resultado al hilo de la interfaz. |
| `EjecutorQuery` | `servicio` | Ejecuta la sentencia con `Statement`, lee los metadatos y arma el `ResultadoQuery`; convierte las `SQLException` en `ErrorQuery`. |
| `ResultadoQuery` | `modelo` | Resultado que viaja a la interfaz: tipo (`LECTURA`, `ESCRITURA`, `ERROR`), columnas, filas, filas afectadas, tiempo y mensaje. |
| `QueryResult` | `dto` | Resultado simple de `ConnectionProvider.executeSelect` (columnas y filas). |
| `ConnectionProvider` | `core` | Abre conexiones desde un `DBConfig` y ofrece `executeSelect` y `executeUpdate` sincrónicos; no participa en el flujo del editor (`DialogoNuevaConexionController` lo usa solo para probar la conexión). |
| `PanelResultadoQueryController` | `controller` | Muestra el resultado en la tabla paginada o el mensaje correspondiente. |
| `BarraEstadoController` | `controller` | Muestra tiempo, filas y mensajes de estado. |
| `HistorialQuerys` | `servicio` | Guarda en memoria las consultas ejecutadas (`agregar`, `obtenerTodos`, etc.). En el código actual, `App.ejecutarSQL` no lo invoca. |
| `ErrorQuery` | `core` | Excepción de una consulta que falla (sintaxis, tabla inexistente, permisos); hereda de `ErrorChuspaDB`. |

## Manejo de excepciones y cancelación

### Excepciones en tiempo de ejecución

- Cualquier `SQLException` al ejecutar o leer resultados se registra con el logger de `EjecutorQuery` y se relanza como `ErrorQuery` con el mismo mensaje.
- Un error al abrir la conexión (`SQLException` o `ErrorConexion` del DAO) sale del `Task` sin pasar por `EjecutorQuery`.
- En ambos casos el `Task` falla y `setOnFailed` entrega la excepción a `App.manejarErrorQuery`, que la convierte en `ResultadoQuery.deError(mensaje, 0)` y la muestra igual que un resultado. Si la excepción no tiene mensaje, se muestra «Ocurrió un error inesperado. Consulte los logs para más detalles.»
- El `Statement` y la conexión se cierran siempre: el primero en `liberarStatement()` (bloque `finally`) y la segunda por el try-with-resources.

### Cancelación

- `EjecutorQueryAsync.cancelarConsultaActiva()` marca `cancelacionSolicitada` y llama a `cancel()` sobre el `Statement` activo. Devuelve `false` si todavía no hay `Statement` o si ya terminó.
- Al cancelar, el driver lanza una `SQLException`. Como cada driver la redacta distinto, se reconoce por la bandera y no por el mensaje, y se entrega a `onError` como `EjecutorQueryAsync.ConsultaCancelada` («Consulta cancelada por el usuario»).
- Si el driver devuelve un resultado aunque se haya pedido cancelar, el resultado se descarta y también se lanza `ConsultaCancelada`.
- En el código actual ningún controlador llama a `cancelarConsultaActiva()`: la cancelación está disponible en el servicio, pero la interfaz todavía no tiene un botón que la use.