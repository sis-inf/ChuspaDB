# Cobertura de Tests

| Clase probada | Archivo de prueba | Paquete |
| :--- | :--- | :--- |
| `DBConfig` | `DBConfigTest.java` | `edu.sisinf.chuspadb.config` |
| `ConnectionFlow` | `ConnectionFlowTest.java` | `edu.sisinf.chuspadb.core` |
| `ErrorChuspaDB` | `ErrorEstanteTest.java` | `edu.sisinf.chuspadb.core` |
| `ConexionDAOMySQL` | `ConexionDAOMySQLTest.java` | `edu.sisinf.chuspadb.dao` |
| `ConexionDAOPostgreSQL` | `ConexionDAOPostgreSQLTest.java` | `edu.sisinf.chuspadb.dao` |
| `ConexionDAOSQLite` | `ConexionDAOSQLiteTest.java` | `edu.sisinf.chuspadb.dao` |
| `QueryResult` | `QueryResultTest.java` | `edu.sisinf.chuspadb.dto` |
| `ColumnaInfo` | `ColumnaInfoTest.java` | `edu.sisinf.chuspadb.modelo` |
| `Conexion` | `ConexionTest.java` | `edu.sisinf.chuspadb.modelo` |
| `FavoritoQuery` | `FavoritoQueryTest.java` | `edu.sisinf.chuspadb.modelo` |
| `EjecutorQuery` | `EjecutorQueryTest.java` | `edu.sisinf.chuspadb.servicio` |
| `ExportadorCSV` | `ExportadorCSVTest.java` | `edu.sisinf.chuspadb.servicio` |
| `ExportadorJSON` | `ExportadorJSONTest.java` | `edu.sisinf.chuspadb.servicio` |
| `GeneradorCreateTable` | `GeneradorCreateTableTest.java` | `edu.sisinf.chuspadb.servicio` |
| `GeneradorSQL` | `GeneradorSQLTest.java` | `edu.sisinf.chuspadb.servicio` |
| `HistorialQuerys` | `HistorialQuerysTest.java` | `edu.sisinf.chuspadb.servicio` |
| `ImportadorCSV` | `ImportadorCSVTest.java` | `edu.sisinf.chuspadb.servicio` |
| `ImportadorJSON` | `ImportadorJSONTest.java` | `edu.sisinf.chuspadb.servicio` |
| `SqlValidator` | `SqlValidatorTest.java` | `edu.sisinf.chuspadb.util` |
| `StringUtils` | `StringUtilsTest.java` | `edu.sisinf.chuspadb.util` |

## Clases con cobertura menor al 50%

No fue posible determinar este dato debido a que no se encuentra disponible un reporte JaCoCo generado y publicado en el repositorio.

## Observaciones

Para obtener los porcentajes reales de cobertura se requiere generar un reporte JaCoCo mediante:

```bash
mvn clean verify
```

## Meta de cobertura

El proyecto ChuspaDB establece una cobertura minima del 60 % de instrucciones, configurada en pom.xml mediante JaCoCo.

Para ejecutar las pruebas, verificar la cobertura y generar el reporte:

```bash
mvn clean verify
```

El reporte HTML se genera en target/site/jacoco/index.html cuando la ejecucion finaliza correctamente.
