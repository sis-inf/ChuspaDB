 # Diseño de Clases - Sprint Actual

Este documento detalla la estructura y las nuevas clases incorporadas al sistema durante el desarrollo del sprint actual para la gestión, persistencia y exportación de datos.

## Nuevas Clases del Sprint

### 1. Módulo de Historial y Consultas
* **HistorialQuerys**: Encargada de gestionar la colección completa de búsquedas y consultas realizadas por los usuarios.
* **EntradaHistorial**: Representa un elemento individual dentro del historial, almacenando los datos específicos de una consulta.
* **FavoritoQuery**: Estructura de datos para almacenar y modelar las consultas marcadas como favoritas.
* **GestorFavoritos**: Componente de lógica de negocio que administra las acciones sobre los favoritos (agregar, eliminar, listar).

### 2. Módulo de Motor SQL y Validación
* **SqlValidator**: Componente encargado de verificar la sintaxis y seguridad de las sentencias SQL antes de su ejecución.
* **GeneradorSQL**: Abstracción para la construcción dinámica de consultas SQL de manera segura.
* **ConexionTester**: Utilidad para verificar la disponibilidad, latencia y estado de las conexiones a las bases de datos.

### 3. Módulo de Importación y Exportación
* **ImportadorCSV**: Clase especializada en la lectura y procesamiento de archivos planos en formato CSV para su carga en el sistema.
* **ExportadorJSON**: Se encarga de serializar las estructuras de datos nativas a formato JSON.
* **ExportadorExcel**: Permite la generación y descarga de reportes formateados en hojas de cálculo compatibles con Excel.

### 4. Módulo de Persistencia (Acceso a Datos)
* **ConexionDAOPostgreSQL**: Implementación específica del patrón DAO para gestionar el ciclo de vida de las conexiones y transacciones hacia el motor de base de datos PostgreSQL.

---

## Diagrama de Clases (Módulos Actuales)

```mermaid
classDiagram
    direction TB
    
    class HistorialQuerys
    class EntradaHistorial
    class FavoritoQuery
    class GestorFavoritos
    
    class SqlValidator
    class GeneradorSQL
    class ConexionTester
    
    class ImportadorCSV
    class ExportadorJSON
    class ExportadorExcel
    
    class ConexionDAOPostgreSQL

    HistorialQuerys "1" --> "*" EntradaHistorial
    GestorFavoritos "1" --> "*" FavoritoQuery
    GeneradorSQL --> SqlValidator
    ConexionDAOPostgreSQL ..> ConexionTester
