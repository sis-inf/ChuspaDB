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
│ SqlValidator detecta │
│ si es destructiva    │
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
│ EjecutorQuery        │
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

## Componentes involucrados

### EjecutorQueryAsync

Ejecuta la consulta en un hilo de segundo plano (`Task` de JavaFX sobre un
`Thread` daemon) para que la interfaz no se bloquee. En ese hilo abre la
conexión con `dao.abrir(conexion)` (`IConexionDAO`), delega la ejecución en
`EjecutorQuery` y cierra la conexión al terminar mediante try-with-resources.
El resultado se entrega a la interfaz con los callbacks `onSuccess` (recibe un
`ResultadoQuery`) y `onError` (recibe la excepción), ambos invocados en el hilo
de JavaFX con `Platform.runLater`. También ofrece `cancelarConsultaActiva()`.

### EjecutorQuery

Ejecuta la sentencia mediante JDBC sobre la conexión que recibe, sin abrirla ni
cerrarla. Usa `SqlValidator.tipo()` para decidir el camino: si es un `SELECT`
ejecuta `executeQuery` y lee columnas y filas; en otro caso ejecuta
`executeUpdate`. Mide el tiempo de ejecución y devuelve un `ResultadoQuery`. Si
ocurre una `SQLException`, la registra en el log y lanza un `ErrorQuery`.

### ResultadoQuery

Clase `modelo.ResultadoQuery`: es el resultado que participa en el flujo
principal. La producen `EjecutorQuery` y `EjecutorQueryAsync`, y la consumen
`PanelEditorSQLController` y `PanelResultadoQueryController` (también los
exportadores). Contiene:

- `tipo`: `LECTURA`, `ESCRITURA` o `ERROR`.
- `columnas` y `filas`, con `totalFilas` (solo en lecturas).
- `filasAfectadas` (solo en escrituras).
- `tiempoMs`: tiempo de ejecución en milisegundos.
- `mensaje`: texto de error (tipo `ERROR`).

No almacena los tipos SQL de las columnas.

### ConnectionProvider y dto.QueryResult

`ConnectionProvider` es una clase utilitaria con métodos estáticos. `abrir()`
carga el driver y obtiene la conexión con `DriverManager.getConnection()`, sin
pool de conexiones. Su método `executeSelect()` devuelve un `dto.QueryResult`,
un DTO que contiene únicamente nombres de columnas y filas (sin tipos SQL).

Estas dos clases **no participan en el flujo principal** descrito en este
documento: la ejecución desde el editor pasa por `EjecutorQueryAsync` y
`EjecutorQuery`, que trabajan con `ResultadoQuery`.

### PanelEditorSQLController

Obtiene la consulta escrita por el usuario. Al pulsar Ejecutar (o Ctrl+Enter)
ignora el texto vacío y, si `SqlValidator.esDestructiva()` lo indica, pide
confirmación con `DialogoConfirmacionDML`. Luego muestra un indicador de
progreso, deshabilita el botón y delega la ejecución en el callback `onEjecutar`, conectado por la aplicación a `EjecutorQueryAsync`. El resultado o el error vuelven por los callbacks `onResultado` y `onError`, y el indicador se
oculta con `ocultarSpinner()`.

### PanelResultadoQueryController

Se encarga de renderizar los datos obtenidos y presentarlos en la interfaz
gráfica.

## Recolección de metadatos

`EjecutorQuery` recopila los datos del resultado mientras ejecuta la consulta:

- **Lecturas (`SELECT`):** nombres de columna (con `getColumnLabel`, es decir,
  el alias si existe), las filas leídas con `getObject`, el total de filas y el
  tiempo de ejecución.
- **Escrituras:** número de filas afectadas y tiempo de ejecución.

Todo se guarda en un `ResultadoQuery`. No se almacenan los tipos SQL de las
columnas.

## Obtención de la conexión

`EjecutorQueryAsync` obtiene la conexión con `dao.abrir(conexion)` en el hilo
de segundo plano y la cierra al terminar la ejecución (éxito, error o
cancelación). En el código documentado no se usa un pool de conexiones: cada
ejecución abre su propia conexión.

## Manejo de excepciones y cancelación

### Excepciones

1. `EjecutorQuery` captura la `SQLException`, la registra en el log y lanza un
   `ErrorQuery`.
2. `EjecutorQueryAsync` hace fallar el `Task` y entrega la excepción al
   callback `onError`, en el hilo de JavaFX.
3. La interfaz decide cómo informar al usuario.

### Cancelación manual

`cancelarConsultaActiva()` marca la cancelación como solicitada y llama a
`Statement.cancel()` sobre el statement en curso. Devuelve `true` si había un
statement activo.

Cuando se cancela, `onError` recibe una `ConsultaCancelada`, lo que permite a
la interfaz distinguir una cancelación de un fallo real. Si el driver devuelve
un resultado a pesar de la cancelación, ese resultado se descarta.

## Control del tiempo de ejecución

La ejecución asíncrona evita que la interfaz se congele durante consultas
largas. **No existe un timeout automático**: una consulta que tarda mucho
sigue ejecutándose hasta terminar, fallar o ser cancelada manualmente con
`cancelarConsultaActiva()`. Lo único que se mide es el tiempo total de
ejecución (`tiempoMs` en `ResultadoQuery`).

## Resumen

1. El usuario escribe una consulta SQL.
2. Al ejecutar, `PanelEditorSQLController` ignora el texto vacío y consulta a `SqlValidator` si la sentencia es destructiva.
3. Si lo es, se solicita confirmación mediante `DialogoConfirmacionDML`; si el usuario cancela, no se ejecuta.
4. La ejecución se realiza mediante `EjecutorQueryAsync` en un hilo de segundo plano: abre la conexión con `dao.abrir()`, delega en `EjecutorQuery` y entrega un `ResultadoQuery` por los callbacks `onSuccess` u `onError`.
5. La consulta se registra en el historial mediante `HistorialQuerys.agregar()`.
6. Los resultados se muestran en `PanelResultadoQueryController`.
7. `BarraEstadoController` actualiza el estado de la operación.
8. Ante errores, se informa al usuario y se actualiza el estado correspondiente. La consulta en curso puede cancelarse manualmente con `cancelarConsultaActiva()`; no existe un timeout automático.