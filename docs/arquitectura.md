# Arquitectura del Sistema - Proyecto Estante

## Propósito de este documento

Este documento es la **fuente principal de arquitectura** del proyecto. Resume la estructura general del sistema, las responsabilidades de sus capas y el flujo de datos entre ellas.

Para detalles específicos consulta:

- [Arquitectura MVC](arquitectura-mvc.md): relación entre vistas FXML, controladores y flujo de interacción.
- [Arquitectura de paquetes Java](arquitectura-paquetes.md): responsabilidades de los paquetes y reglas de dependencia.

## Descripción general

Estante es un gestor de bases de datos con interfaz gráfica JavaFX. Permite conectarse a SQLite, MySQL y PostgreSQL, explorar esquemas, ejecutar consultas SQL y exportar resultados.

La aplicación separa presentación, lógica de negocio, acceso a datos y modelo para reducir el acoplamiento y facilitar mantenimiento, pruebas y evolución.

## Arquitectura general

```text
Usuario
  |
  v
Presentación (JavaFX / FXML / Controllers)
  |
  v
Servicios
  |
  v
Acceso a datos (DAO / repositorios)
  |
  v
SQLite / MySQL / PostgreSQL
```

## Capas del sistema

### 1. Presentación

Responsable de la interacción con el usuario.

Componentes reales destacados:

- `App`: punto de entrada y coordinación general.
- `VentanaPrincipalController`: ventana principal.
- `PanelArbolConexionesController`: navegación de conexiones, esquemas y tablas.
- `PanelEditorSQLController`: edición y ejecución de SQL.
- `PanelResultadoQueryController`: presentación de resultados.
- `DialogoNuevaConexionController`: creación y edición de conexiones.

La relación detallada entre FXML y controladores está documentada en [arquitectura-mvc.md](arquitectura-mvc.md).

### 2. Servicios

Contiene la lógica de aplicación que utilizan los controladores.

Ejemplos reales:

- `EjecutorQuery`: ejecución de consultas SQL.
- `EjecutorQueryAsync`: ejecución asíncrona.
- `ExploradorEsquemas`: exploración mediante metadatos JDBC.
- `GeneradorSQL` y `GeneradorCreateTable`: generación de SQL.
- `ImportadorCSV`: importación de datos CSV.
- `ExportadorCSV` y `ExportadorJSON`: exportación de resultados.
- `GestorFavoritos`: gestión de consultas favoritas.
- `HistorialQuerys`: historial de consultas.
- `SqlValidator`: validación de SQL.

### 3. Acceso a datos

Encapsula la comunicación con motores y la persistencia local.

- `IConexionDAO`: contrato de acceso a motores.
- `ConexionDAOMySQL`: implementación MySQL.
- `ConexionDAOPostgreSQL`: implementación PostgreSQL.
- `ConexionDAOSQLite`: implementación SQLite.
- `IRepositorioConexiones`: contrato de persistencia de conexiones.
- `RepositorioConexionesJSON`: persistencia local de conexiones en JSON.

### 4. Modelo

Representa los datos manejados por el sistema.

- `Conexion`
- `TipoMotor`
- `ResultadoQuery`
- `Esquema`
- `Tabla`
- `Columna`
- `ColumnaInfo`
- `EntradaHistorial`
- `ImportacionResultado`
- `FavoritoQuery`

La organización exacta por paquetes y sus dependencias se encuentra en [arquitectura-paquetes.md](arquitectura-paquetes.md).

## Flujo general de datos

1. El usuario interactúa con una vista JavaFX.
2. El controlador recibe la acción.
3. El controlador delega la operación a un servicio.
4. El servicio utiliza un DAO o repositorio cuando necesita acceder a datos.
5. El DAO se comunica con SQLite, MySQL o PostgreSQL mediante JDBC.
6. El resultado se transforma en objetos del modelo.
7. El controlador actualiza la vista.

Para el recorrido detallado de una consulta SQL, consulta [arquitectura-mvc.md](arquitectura-mvc.md).

## Tecnologías principales

| Tecnología | Uso |
|---|---|
| Java | Lenguaje principal |
| JavaFX / FXML | Interfaz gráfica |
| JDBC | Acceso a bases de datos |
| SQLite | Motor embebido |
| MySQL | Motor relacional |
| PostgreSQL | Motor relacional |
| Maven | Compilación y dependencias |
| Jackson | Persistencia JSON |
| SLF4J | Logging |
| JUnit / Mockito | Pruebas |

## Principios arquitectónicos

- Separación de responsabilidades entre presentación, servicios, DAO y modelo.
- Dependencias dirigidas desde capas superiores hacia capas inferiores.
- Acceso a motores encapsulado detrás de `IConexionDAO`.
- Persistencia de conexiones encapsulada detrás de `IRepositorioConexiones`.
- Uso de modelos de dominio para transportar información entre componentes.
- Evitar que las vistas FXML contengan lógica de negocio.

## Extensibilidad

La arquitectura permite incorporar nuevos motores de base de datos mediante nuevas implementaciones de `IConexionDAO`, así como nuevos servicios de importación, exportación y análisis sin concentrar toda la lógica en los controladores.
