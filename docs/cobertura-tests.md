# Cobertura de Tests

| Clase probada | Archivo de prueba | Paquete |
| :--- | :--- | :--- |
| `DBConfig` | `DBConfigTest.java` | `edu.sisinf.estante.config` |
| `ConnectionFlow` | `ConnectionFlowTest.java` | `edu.sisinf.estante.core` |
| `ErrorEstante` | `ErrorEstanteTest.java` | `edu.sisinf.estante.core` |
| `ConexionDAOMySQL` | `ConexionDAOMySQLTest.java` | `edu.sisinf.estante.dao` |
| `ConexionDAOPostgreSQL` | `ConexionDAOPostgreSQLTest.java` | `edu.sisinf.estante.dao` |
| `ConexionDAOSQLite` | `ConexionDAOSQLiteTest.java` | `edu.sisinf.estante.dao` |
| `QueryResult` | `QueryResultTest.java` | `edu.sisinf.estante.dto` |
| `ColumnaInfo` | `ColumnaInfoTest.java` | `edu.sisinf.estante.modelo` |
| `Conexion` | `ConexionTest.java` | `edu.sisinf.estante.modelo` |
| `FavoritoQuery` | `FavoritoQueryTest.java` | `edu.sisinf.estante.modelo` |
| `EjecutorQuery` | `EjecutorQueryTest.java` | `edu.sisinf.estante.servicio` |
| `ExportadorCSV` | `ExportadorCSVTest.java` | `edu.sisinf.estante.servicio` |
| `ExportadorJSON` | `ExportadorJSONTest.java` | `edu.sisinf.estante.servicio` |
| `GeneradorCreateTable` | `GeneradorCreateTableTest.java` | `edu.sisinf.estante.servicio` |
| `GeneradorSQL` | `GeneradorSQLTest.java` | `edu.sisinf.estante.servicio` |
| `HistorialQuerys` | `HistorialQuerysTest.java` | `edu.sisinf.estante.servicio` |
| `ImportadorCSV` | `ImportadorCSVTest.java` | `edu.sisinf.estante.servicio` |
| `ImportadorJSON` | `ImportadorJSONTest.java` | `edu.sisinf.estante.servicio` |
| `SqlValidator` | `SqlValidatorTest.java` | `edu.sisinf.estante.util` |
| `StringUtils` | `StringUtilsTest.java` | `edu.sisinf.estante.util` |

## Clases con cobertura menor al 50%

No fue posible determinar este dato debido a que no se encuentra disponible un reporte JaCoCo generado y publicado en el repositorio.

## Observaciones

Para obtener los porcentajes reales de cobertura se requiere generar un reporte JaCoCo mediante:

```bash
mvn test jacoco:report
```

