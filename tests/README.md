\# Estructura de Tests del Proyecto ChuspaDB



Este documento describe la organizacion, ejecucion y creacion de tests para el proyecto ChuspaDB, un gestor de bases de datos MySQL desarrollado en Java y JavaFX.



\---



\## Estructura de Carpetas



| Directorio | Descripcion | Contenido tipico |

|------------|-------------|------------------|

| `src/test/java/` | Tests automatizados con JUnit 5 | Clases `\*Test.java` |

| `tests/automatizados/e2e/` | Tests end-to-end | Flujos completos de usuario |

| `tests/automatizados/integration/` | Tests de integracion | Conexion con base de datos |

| `tests/automatizados/unit/` | Tests unitarios | Logica de negocio aislada |

| `tests/casos/funcionales/` | Casos de prueba funcionales | Escenarios de funcionalidad |

| `tests/casos/no-funcionales/rendimiento/` | Tests de rendimiento | Tiempos de respuesta |

| `tests/casos/no-funcionales/seguridad/` | Tests de seguridad | Validaciones, permisos |

| `tests/casos/regresion/` | Tests de regresion | Verificacion de bugs corregidos |

| `tests/datos/entrada/` | Datos de entrada para tests | CSV, SQL, JSON |

| `tests/datos/esperados/` | Resultados esperados | Archivos con salidas correctas |

| `tests/manuales/checklists/` | Listas de verificacion manuales | Pasos para testing exploratorio |

| `tests/manuales/evidencias/` | Evidencias de pruebas manuales | Capturas, videos, logs |

| `tests/plan/` | Documentacion del plan de pruebas | Estrategia, criterios de salida |

| `tests/reportes/` | Plantillas y reportes de ejecucion | Plantillas de reporte |



\---



\## Ejecutar Tests



\### Todos los tests



```bash

mvn test

