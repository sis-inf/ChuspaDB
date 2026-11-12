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
## Componentes involucrados

### ConnectionProvider

Responsable de obtener una conexión válida a la base de datos utilizando la configuración activa. Actúa como punto de acceso para los componentes que necesitan ejecutar consultas.

### EjecutorQuery

Realiza la ejecución efectiva de la sentencia SQL mediante JDBC. Procesa la consulta recibida y genera el resultado correspondiente.

### EjecutorQueryAsync

Ejecuta consultas en segundo plano para evitar bloqueos de la interfaz gráfica. Coordina la ejecución asíncrona y la entrega de resultados al usuario.

### QueryResult

Contiene el resultado de la ejecución de una consulta, incluyendo datos devueltos, metadatos de columnas e información asociada a la operación realizada.

### PanelEditorSQLController

Obtiene la consulta escrita por el usuario e inicia el flujo de validación y ejecución.

### PanelResultadoQueryController

Se encarga de renderizar los datos obtenidos y presentarlos en la interfaz gráfica.

## Recolección de metadatos

Tras la ejecución de la consulta, se recopilan los metadatos asociados al resultado obtenido.

Entre los elementos recopilados se encuentran:

- Nombres de columnas.
- Tipos de datos.
- Cantidad de registros obtenidos.
- Información necesaria para la representación en la interfaz.

Estos datos son almacenados dentro de `QueryResult` para su posterior visualización.

## Manejo de excepciones y cancelación

Durante la ejecución pueden producirse errores de validación, conexión o ejecución SQL.

Ante una excepción:

1. El error es detectado por los componentes de ejecución.
2. El evento puede registrarse en el historial.
3. Se notifica al usuario mediante la interfaz.
4. La barra de estado actualiza el resultado de la operación.

Las consultas de larga duración pueden ser canceladas o interrumpidas según los mecanismos de control implementados por la aplicación.

## Control del tiempo de ejecución

La ejecución asíncrona permite supervisar consultas que pueden requerir tiempos prolongados.

Si una operación excede el tiempo esperado:

- La ejecución puede ser interrumpida.
- El usuario recibe una notificación.
- La aplicación mantiene la capacidad de respuesta de la interfaz.

## Resumen

1. El usuario escribe una consulta SQL.
2. La consulta es validada por `SqlValidator`.
3. Si la operación corresponde a una instrucción DML, se solicita confirmación mediante `DialogoConfirmacionDML`.
4. La ejecución se realiza mediante `EjecutorQueryAsync` en un hilo de segundo plano.
5. La consulta se registra en el historial mediante `HistorialQuerys.agregar()`.
6. Los resultados se muestran en `PanelResultadoQueryController`.
7. `BarraEstadoController` actualiza el estado de la operación.
8. Ante errores, se informa al usuario y se actualiza el estado correspondiente.