# Arquitectura de Paquetes Java

Este documento complementa la [arquitectura general](arquitectura.md) y describe cómo se organiza el código Java por paquetes. La aplicación del patrón MVC se detalla en [arquitectura-mvc.md](arquitectura-mvc.md).

## Paquetes principales

| Paquete | Responsabilidad | Ejemplos reales del proyecto |
|---|---|---|
| `modelo` | Entidades y estructuras de datos del dominio | `Conexion`, `ResultadoQuery`, `Tabla`, `TipoMotor` |
| `core` | Componentes centrales y errores de dominio | `ConnectionProvider`, `ErrorConexion`, `ErrorQuery`, `ErrorPersistencia` |
| `dao` | Acceso a motores y persistencia | `IConexionDAO`, `ConexionDAOMySQL`, `ConexionDAOPostgreSQL`, `ConexionDAOSQLite`, `RepositorioConexionesJSON` |
| `servicio` | Casos de uso y lógica de aplicación | `EjecutorQuery`, `EjecutorQueryAsync`, `ExploradorEsquemas`, `GeneradorSQL`, `GestorFavoritos` |
| `util` | Utilidades reutilizables | `SqlValidator` |
| `controller` | Coordinación entre vistas y lógica | `PanelEditorSQLController`, `PanelArbolConexionesController`, `DialogoNuevaConexionController` |
| `config` | Configuración de la aplicación | `ConfiguracionApp` |
| `dto` | Objetos de transferencia utilizados entre componentes | clases DTO definidas en el paquete `edu.sisinf.estante.dto` |

Las vistas se almacenan como recursos FXML bajo `src/main/resources/fxml`; no forman un paquete Java `vista`.

## Flujo de dependencias

```text
FXML
  |
  v
controller
  |
  v
servicio
  |
  +------> core
  |
  +------> modelo
  |
  v
dao
  |
  v
JDBC / archivos JSON
```

El diagrama representa el flujo habitual, no una regla de que todas las clases deban atravesar cada paquete en cada operación.

## Reglas de organización

1. Los controladores coordinan la interacción con la UI y delegan la lógica reutilizable.
2. Los servicios implementan casos de uso y pueden trabajar con modelos, componentes `core` y DAO.
3. Los DAO encapsulan JDBC y persistencia; la UI no debe acceder directamente a ellos.
4. Los modelos no deben depender de controladores ni vistas.
5. Las utilidades deben mantenerse independientes de detalles de la interfaz.
6. La configuración de la aplicación pertenece a `config`.
7. Los recursos FXML permanecen separados del código Java.

## Ejemplos de ubicación

| Si el componente... | Ubicación | Ejemplo real |
|---|---|---|
| Representa una conexión o resultado | `modelo` | `Conexion`, `ResultadoQuery` |
| Abre conexiones JDBC | `dao` | `ConexionDAOMySQL` |
| Persiste conexiones en JSON | `dao` | `RepositorioConexionesJSON` |
| Ejecuta una consulta | `servicio` | `EjecutorQuery` |
| Genera SQL | `servicio` | `GeneradorSQL` |
| Valida SQL reutilizable | `util` | `SqlValidator` |
| Atiende eventos del editor | `controller` | `PanelEditorSQLController` |
| Gestiona configuración | `config` | `ConfiguracionApp` |
| Define una vista | `src/main/resources/fxml` | `PanelEditorSQL.fxml` |

## Fuente principal

Las decisiones arquitectónicas generales, las capas y el flujo de datos se documentan en [arquitectura.md](arquitectura.md). Este archivo se limita a la estructura de paquetes para evitar que la misma arquitectura se mantenga duplicada en varios documentos.
