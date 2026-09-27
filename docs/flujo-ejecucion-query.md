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