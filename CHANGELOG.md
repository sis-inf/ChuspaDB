# Registro de Cambios (Changelog)

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [SemVer](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Añadido
- Documentación detallada de Casos de Uso del sistema.
- Diccionario de datos con la estructura de las tablas `libros`, `socios` y `prestamos`.
- Guía de instalación y configuración para entorno local.
- Módulo de persistencia y soporte para base de datos PostgreSQL.
- Componente `ExportadorJSON` para la extracción estructurada de datos.
- Componente `ExportadorExcel` para la exportación de resultados de consultas a archivos Excel (.xlsx) usando Apache POI.
- Panel de administración e historial de consultas mediante `HistorialQuerys`.
- Motor de validación semántica mediante `SqlValidator`.
- Herramienta de generación dinámica de sentencias mediante `GeneradorSQL`.

### Corregido
- Flujo de integración de Pull Requests apuntando correctamente a la rama `dev`.
- Limpieza y reestructuración de archivos sueltos y temporales en la raíz del proyecto.
- `getTablas()` en los 3 DAO de conexión (MySQL, PostgreSQL, SQLite) ahora reutiliza los datos reales de la última conexión abierta, en vez de valores codificados como `localhost`.

### Cambiado
- Refactorizaciones estructurales y optimizaciones de rendimiento aplicadas a lo largo del sprint actual.

## [0.1.0] - 2026-04-19

### Añadido
- Estructura inicial del proyecto Estante.
- Configuración básica del repositorio y rama de desarrollo.
- Creación de la carpeta de documentación (`docs/`).
