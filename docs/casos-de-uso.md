# Casos de Uso del Administrador de BD - Proyecto ChuspaDB

Este documento describe las interacciones entre los actores y el motor de base de datos para la gestión técnica de la información.

## 1. Actores del Sistema
- **DBA (Database Administrator):** Actor con privilegios de nivel superior encargado de la definición de esquemas, mantenimiento de la integridad y gestión masiva de registros.
- **Gestor de Consultas (Query Engine):** Proceso o usuario que realiza operaciones de recuperación y manipulación de datos (DML) para el consumo de información.

## 2. Casos de Uso Principales

### CU-01: Inserción de Registros (Create)
- **Actor:** DBA
- **Descripción:** Persistir una nueva instancia de datos en la entidad (tabla) seleccionada.
- **Flujo Principal:**
  1. El DBA proporciona los atributos del registro.
  2. El sistema valida las restricciones de integridad (Unique/Not Null).
  3. El motor de BD escribe el registro en el archivo físico de almacenamiento.

### CU-02: Recuperación Selectiva de Información (Read)
- **Actor:** Gestor de Consultas
- **Descripción:** Consultar registros específicos basándose en criterios de filtrado o índices.
- **Flujo Principal:**
  1. Se ingresa el parámetro de búsqueda (ej: Clave Primaria).
  2. El motor realiza un escaneo de la tabla o índice.
  3. Se retorna el conjunto de resultados que cumplen la condición.

### CU-03: Actualización Atómica de Datos (Update)
- **Actor:** DBA
- **Descripción:** Modificar valores de campos en registros existentes sin comprometer la consistencia.
- **Flujo Principal:**
  1. Se identifica el registro mediante su ID.
  2. Se ingresan los nuevos valores para los atributos seleccionados.
  3. El sistema actualiza el registro y confirma la persistencia (Commit).

### CU-04: Eliminación con Integridad Referencial (Delete)
- **Actor:** DBA
- **Descripción:** Remover físicamente un registro de la base de datos.
- **Flujo Principal:**
  1. Se selecciona el registro para su remoción.
  2. El motor verifica que no existan dependencias (Foreign Keys) activas.
  3. Se elimina el registro y se actualizan los índices de la tabla.

### CU-05: Gestión de Transacciones de Préstamo
- **Actor:** DBA
- **Descripción:** Registrar el vínculo transaccional entre una entidad de recurso y una entidad de usuario.
- **Flujo Principal:**
  1. Se vinculan los identificadores de ambas entidades.
  2. El sistema genera un registro de auditoría con marcas de tiempo.
  3. Se actualiza el estado de disponibilidad en la tabla maestra.

### CU-06: Auditoría de Integridad del Sistema
- **Actor:** DBA / Gestor de Consultas
- **Descripción:** Revisar el estado de los registros para asegurar que los datos no estén corruptos.
- **Flujo Principal:**
  1. El sistema realiza un listado general de las tablas.
  2. Se comparan los contadores de registros con los índices.
  3. Se reporta el estado de salud de la base de datos.
## 3. Casos de Uso Agregados en el Sprint

### CU-Hist-01: Relanzamiento de Consulta desde Historial

* **Actor:** Gestor de Consultas
* **Precondición:** Existe al menos una consulta almacenada en el historial de ejecución.
* **Flujo Principal:**

  1. El usuario accede al módulo de historial.
  2. El sistema muestra las consultas ejecutadas anteriormente.
  3. El usuario selecciona una consulta del historial.
  4. El sistema carga la consulta seleccionada en el editor SQL.
  5. El usuario ejecuta nuevamente la consulta.
  6. El sistema procesa la consulta y muestra los resultados obtenidos.
* **Postcondición:** La consulta seleccionada es ejecutada nuevamente y sus resultados quedan disponibles para el usuario.

### CU-Fav-01: Guardar Consulta como Favorita

* **Actor:** Gestor de Consultas
* **Precondición:** El usuario dispone de una consulta SQL válida que desea conservar para uso posterior.
* **Flujo Principal:**

  1. El usuario selecciona la opción para guardar una consulta como favorita.
  2. El sistema solicita un nombre descriptivo para identificar la consulta.
  3. El usuario introduce el nombre deseado.
  4. El sistema verifica que no exista otro favorito registrado con el mismo nombre.
  5. El sistema almacena la consulta junto con su información asociada.
  6. El sistema confirma que la operación fue realizada correctamente.
* **Postcondición:** La consulta queda registrada en la lista de favoritos y puede recuperarse posteriormente.

### CU-Import-01: Importación de Datos desde Archivo CSV

* **Actor:** DBA
* **Precondición:** Existe una conexión activa a la base de datos, una tabla destino disponible y un archivo CSV válido.
* **Flujo Principal:**

  1. El usuario selecciona la opción de importar datos.
  2. El usuario indica el archivo CSV que desea cargar.
  3. El sistema lee las cabeceras del archivo.
  4. El usuario selecciona la tabla destino.
  5. El sistema procesa cada fila del archivo.
  6. El sistema inserta los registros válidos en la tabla correspondiente.
  7. El sistema registra cualquier error encontrado durante el proceso de importación.
  8. El sistema muestra un resumen de registros importados y registros fallidos.
* **Postcondición:** Los datos válidos del archivo CSV son incorporados a la tabla seleccionada y se genera un resultado de la importación.

### CU-Export-02: Exportación de Resultados para Análisis

* **Actor:** Analista
* **Precondición:** Existe un resultado de consulta disponible para exportación.
* **Flujo Principal:**

  1. El analista ejecuta una consulta sobre la base de datos.
  2. El sistema muestra los resultados obtenidos.
  3. El analista selecciona la opción de exportar resultados.
  4. El sistema genera un archivo con la información obtenida.
  5. El usuario selecciona la ubicación donde desea guardar el archivo.
  6. El sistema completa la operación de exportación.
* **Postcondición:** Los resultados de la consulta quedan almacenados en un archivo externo para su análisis posterior.
