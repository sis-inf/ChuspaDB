# Estrategia de Pruebas

## Enfoque general
La calidad del MVP v0.1.0 se evalúa en tres niveles: pruebas unitarias automatizadas sobre las clases Java, pruebas de integración contra los motores de base de datos soportados (SQLite, MySQL y PostgreSQL) y pruebas funcionales manuales de la interfaz. Las pruebas automatizadas se ejecutan en el CI (`.github/workflows/ci.yml`) en cada Pull Request hacia `dev` y `main`, con Java 17 y 21 en Ubuntu, Windows y macOS. Los casos funcionales manuales están en `tests/casos/funcionales/casos-prueba-funcionales.md` y los criterios para integrar están en `tests/plan/criterios-de-salida.md`.

## Niveles de prueba

### Pruebas unitarias
- Herramienta: JUnit Jupiter 6.1.3 (`junit-jupiter`) y Mockito 5.24.0 (`mockito-core`, `mockito-junit-jupiter`), ejecutadas con `mvn test`; cobertura medida con JaCoCo (`jacoco-maven-plugin`).
- Cobertura mínima: 80% en pruebas unitarias, según `tests/plan/plan-de-pruebas.md`. La regla `check` de JaCoCo en `pom.xml` hoy exige 60% de instrucciones cubiertas, por lo que el 80% se verifica en el reporte de JaCoCo hasta que esa regla se actualice.
- Responsable: Automatización — Equipo de Desarrollo / DevOps (según `tests/plan/plan-de-pruebas.md`).

### Pruebas de integración
- Herramienta: JUnit Jupiter con `sqlite-jdbc` para SQLite; Docker con un servidor real para MySQL y PostgreSQL.
- Alcance: conexión, listado de esquemas y tablas, ejecución de consultas SQL y manejo de errores de conexión en los tres motores. SQLite se prueba con una base real dentro de JUnit (`ConexionDAOSQLiteTest`, `EjecutorQueryTest`). Las pruebas JUnit de MySQL y PostgreSQL (`ConexionDAOMySQLTest`, `ConexionDAOPostgreSQLTest`) simulan el driver con Mockito, así que la conexión real a esos motores se valida con un servidor en Docker.
- Responsable: Automatización — Equipo de Desarrollo / DevOps para las pruebas en JUnit; Ejecución manual — RODRIGO BALTAZAR BORRAS para las validaciones contra MySQL y PostgreSQL en Docker (según `tests/plan/plan-de-pruebas.md`).

### Pruebas manuales
- Frecuencia: una ejecución completa de los casos funcionales en cada sprint y antes de pasar de `dev` a `main`, como exige `tests/plan/criterios-de-salida.md` («Todos los casos de prueba del sprint ejecutados»).
- Responsable: Diseño de casos, ejecución manual y reporte — RODRIGO BALTAZAR BORRAS (según `tests/plan/plan-de-pruebas.md`).

## Criterios de priorización de bugs

| Severidad | Descripción | Tiempo de resolución |
|---|---|---|
| Crítico | El sistema no funciona | Inmediato |
| Alto | Funcionalidad principal afectada | 24 horas |
| Medio | Funcionalidad secundaria afectada | 1 semana |
| Bajo | Problema cosmético | Siguiente sprint |