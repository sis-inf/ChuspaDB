# Requerimientos del Proyecto

## Requerimientos Funcionales

| ID     | Descripción                                                                  | Prioridad | Estado    |
|--------|------------------------------------------------------------------------------|-----------|-----------|
| RF-001 | Conectar a una base de datos PostgreSQL mediante JDBC                        | Alta      | Pendiente |
| RF-002 | Mostrar las tablas disponibles en la base de datos                           | Alta      | Pendiente |
| RF-003 | Ejecutar consultas SQL ingresadas por el usuario                             | Alta      | Pendiente |
| RF-004 | Mostrar los resultados de las consultas en formato tabular                   | Alta      | Pendiente |
| RF-005 | Permitir insertar nuevos registros en las tablas                             | Media     | Pendiente |
| RF-006 | Permitir actualizar registros existentes                                     | Media     | Pendiente |
| RF-007 | Permitir eliminar registros en la base de datos                              | Media     | Pendiente |
| RF-008 | Validar errores en consultas SQL y mostrar mensajes claros                   | Alta      | Pendiente |
| RF-009 | Permitir seleccionar diferentes bases de datos                               | Baja      | Pendiente |
| RF-010 | Guardar historial de consultas realizadas                                    | Baja      | Pendiente |
| RF-011 | Permitir guardar consultas SQL como favoritas                                | Media     | Pendiente |
| RF-012 | Permitir exportar resultados a formato CSV                                   | Media     | Pendiente |
| RF-013 | Mostrar información detallada de columnas y tablas                           | Media     | Pendiente |
| RF-014 | Permitir importar datos desde archivos CSV                                   | Baja      | Pendiente |
| RF-015 | Mostrar estadísticas básicas de la base de datos                             | Baja      | Pendiente |

---
## Requerimientos No Funcionales

| ID      | Descripción                                                                                  | Categoría    | Estado    |
|---------|----------------------------------------------------------------------------------------------|--------------|-----------|
| RNF-001 | El sistema debe mostrar resultados de consultas SQL en un tiempo máximo de 2 segundos        | Rendimiento  | Pendiente |
| RNF-002 | La interfaz debe permitir realizar una consulta básica en menos de 4 clics                   | Usabilidad   | Pendiente |
| RNF-003 | El sistema debe procesar hasta 1000 registros sin bloqueos visuales                          | Rendimiento  | Pendiente |
| RNF-004 | Los mensajes de error deben ser comprensibles para el usuario                                | Usabilidad   | Pendiente |
| RNF-005 | El sistema debe garantizar la integridad de los datos durante múltiples consultas            | Fiabilidad   | Pendiente |
| RNF-006 | La aplicación debe cargar su interfaz principal en un máximo de 3 segundos                   | Rendimiento  | Pendiente |
| RNF-007 | El historial de consultas debe ser de fácil acceso                                           | Usabilidad   | Pendiente |
| RNF-008 | La interfaz debe mantener un diseño consistente en todas sus ventanas                        | Usabilidad   | Pendiente |
| RNF-009 | El sistema debe permitir ejecutar consultas concurrentes sin superar 3 segundos por consulta | Rendimiento  | Pendiente |
| RNF-010 | Las consultas deben ejecutarse de forma asíncrona manteniendo la interfaz responsiva durante el 100% de la ejecución | Rendimiento | Pendiente |
| RNF-011 | El sistema debe soportar al menos 3 motores de base de datos diferentes                      | Compatibilidad | Pendiente |
| RNF-012 | El historial de consultas debe permanecer disponible durante el 100% de la sesión activa     | Persistencia | Pendiente |
| RNF-013 | El sistema debe permitir exportar hasta 10.000 filas sin errores de memoria                  | Capacidad    | Pendiente |
| RNF-014 | El visor de resultados debe mostrar correctamente hasta 10.000 registros simultáneamente     | Capacidad    | Pendiente |

---
## Requerimientos de Sistema

| ID     | Descripción                                                                 |
|--------|-----------------------------------------------------------------------------|
| RS-001 | Compatible con Windows 10/11 y distribuciones de Linux                      |
| RS-002 | Requiere JDK 17 o superior                                                  |
| RS-003 | El instalador no debe exceder los 100 MB                                    |
| RS-004 | Requiere mínimo 2 núcleos y 4 GB de RAM                                     |
| RS-005 | Resolución mínima de 1280x720                                               |
| RS-006 | Acceso a puertos de red para conexión con PostgreSQL                        |
| RS-007 | Al menos 200 MB libres para logs y archivos temporales                      |
