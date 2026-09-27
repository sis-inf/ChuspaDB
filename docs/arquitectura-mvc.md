# Arquitectura MVC en Estante

Este documento complementa la [arquitectura general](arquitectura.md) y se concentra únicamente en cómo se aplica MVC en la interfaz JavaFX. La descripción completa de capas, servicios y acceso a datos se mantiene en `arquitectura.md` para evitar duplicación.

## Responsabilidades MVC

### Modelo

Los modelos representan la información que circula por la aplicación. Entre las clases reales se encuentran:

- `Conexion`
- `ResultadoQuery`
- `Esquema`
- `Tabla`
- `ColumnaInfo`
- `FavoritoQuery`
- `EntradaHistorial`
- `ImportacionResultado`
- `TipoMotor`

### Vista

Las vistas FXML definen la estructura visual. No deben contener lógica de negocio ni acceso directo a datos.

| Vista FXML | Controlador |
|---|---|
| `VentanaPrincipal.fxml` | `VentanaPrincipalController` |
| `DialogoNuevaConexion.fxml` | `DialogoNuevaConexionController` |
| `PanelArbolConexiones.fxml` | `PanelArbolConexionesController` |
| `PanelEditorSQL.fxml` | `PanelEditorSQLController` |
| `PanelEstadisticas.fxml` | `PanelEstadisticasController` |
| `PanelHistorial.fxml` | `PanelHistorialController` |
| `PanelInfoTabla.fxml` | `PanelInfoTablaController` |
| `PanelResultadoQuery.fxml` | `PanelResultadoQueryController` |
| `BarraEstado.fxml` | `BarraEstadoController` |

### Controlador

Los controladores reciben eventos de la vista y coordinan servicios y modelos. Por ejemplo:

- `PanelEditorSQLController` recibe la solicitud de ejecutar SQL.
- `PanelResultadoQueryController` presenta un `ResultadoQuery`.
- `PanelArbolConexionesController` coordina la navegación de conexiones y tablas.
- `DialogoNuevaConexionController` gestiona los datos introducidos para una conexión.

Los controladores no deben reemplazar a la capa de servicios ni implementar directamente la persistencia.

## Flujo de una consulta SQL

```text
Usuario
  |
  v
PanelEditorSQL.fxml
  |
  v
PanelEditorSQLController
  |
  v
SqlValidator / EjecutorQuery
  |
  v
IConexionDAO
  |
  v
Motor de base de datos
  |
  v
ResultadoQuery
  |
  v
PanelResultadoQueryController
  |
  v
PanelResultadoQuery.fxml
```

Este flujo muestra la separación entre presentación, servicios y acceso a datos. La descripción de esas capas se mantiene en [arquitectura.md](arquitectura.md).

## Relación con la estructura de paquetes

MVC describe responsabilidades de interacción; la organización física del código se documenta por separado en [arquitectura-paquetes.md](arquitectura-paquetes.md). De este modo, este documento no repite las reglas de dependencia entre paquetes.
