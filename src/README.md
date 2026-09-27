

# 📂 Estructura de la carpeta `src`

```bash
src/
└── main/
    └── java/
        └── edu/sisinf/estante/
            ├── controller/
            ├── dao/
            ├── modelo/
            ├── servicio/
            ├── util/
            ├── core/
            ├── dto/
            ├── config/
            └── vista/
```

Este directorio contiene la organización del código fuente siguiendo una arquitectura por capas, separando la interfaz gráfica, y el acceso a datos.

---

## 📂 Descripción de Directorios

### 🔹 `controller/`

Gestiona la interacción entre la interfaz y la lógica del sistema.

Responsabilidades:

* Manejo de eventos
* Comunicación con la capa de servicios
* VentanaPrincipalController
* DialogoNuevaConexionController
* PanelEditorSQLController
* PanelResultadoQueryController
* PanelHistorialController
* BarraEstadoController

---

### 🔹dao/

Encapsula el acceso y persistencia de datos.

Clases principales:

* IConexionDAO
* ConexionDAOMySQL
* ConexionDAOSQLite
* ConexionDAOPostgreSQL
* RepositorioConexionesJSON

---

### 🔹 `modelo/`

Define las entidades del dominio del sistema.

Clases principales:

* Conexion
* TipoMotor
* ResultadoQuery
* FavoritoQuery
* Tabla
* Columna
* Esquema
Ejemplo:

* Atributos y métodos de acceso
* Clases de datos (`POJOs`)

> [!NOTE]
> Un **POJO** (Plain Old Java Object) es un objeto Java simple que no depende de frameworks, no extiende clases especiales ni implementa interfaces específicas, siguiendo solo las convenciones básicas del lenguaje.

---

### 🔹 `servicio/`

Implementa la lógica de negocio de la aplicación.

Clases principales:

* EjecutorQuery
* ExploradorEsquemas
* ExportadorCSV
* ExportadorJSON
* GeneradorSQL
* GeneradorCreateTable
* GestorFavoritos

Responsabilidades:

* Procesamiento de datos
* Aplicación de reglas de negocio
* Coordinación con la capa de acceso a datos

---

### 🔹 `util/`

Contiene funciones auxiliares reutilizables.

Clases principales:

* SqlValidator
* LoggerConsole
* StringUtils
* Responsabilidades:
* Validaciones
* Formateo
* Utilidades generales

---

### 🔹core/

Incluye componentes base y manejo interno del sistema.

Clases principales:

* ConnectionProvider
* HistorialConsultas
* QueryTimer
* ErrorConexion
* ErrorQuery
* ErrorPersistencia

---

### 🔹dto/

Define objetos de transferencia de datos.

Clases principales:

* QueryResult

---

### 🔹 config/

Gestiona configuración y parámetros de aplicación.

Clases principales:

* ConfiguracionApp
* DBConfig

---

### 🔹 vista/

Contiene recursos y componentes relacionados con la interfaz visual.

---

## 🧩 Principios de Diseño

* **Separación de responsabilidades**: Cada capa cumple una función específica dentro del sistema.
* **Modularidad**: El código se organiza en componentes independientes y reutilizables.
* **Escalabilidad**: La estructura permite agregar nuevas funcionalidades sin afectar las existentes.
* **Mantenibilidad**: Facilita la comprensión y modificación del código a lo largo del tiempo.