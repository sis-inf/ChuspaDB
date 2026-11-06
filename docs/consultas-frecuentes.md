# Guía de Operaciones y Consultas del DBMS - ChuspaDB

Este documento detalla las operaciones fundamentales de manipulación de datos (DML) que el administrador puede ejecutar sobre el motor de base de datos de **ChuspaDB**.

## 1. Inserción de Registros (Crear)
**Descripción:** Operación para persistir un nuevo registro en la tabla de datos bibliográficos.
- **Entrada esperada:** Atributos del objeto (Título, Autor, ISBN único).
- **Resultado esperado:** Asignación de un ID autoincremental y confirmación de escritura en el archivo de base de datos.

## 2. Recuperación de Información por ID (Leer)
**Descripción:** Acceso directo a un registro específico mediante su clave primaria.
- **Entrada esperada:** Identificador numérico (PK).
- **Resultado esperado:** Retorno de todos los campos asociados al puntero del registro solicitado.

## 3. Actualización de Atributos (Actualizar)
**Descripción:** Modificación de valores en campos específicos de un registro existente, manteniendo la integridad.
- **Entrada esperada:** ID del registro y el nuevo valor para el campo (ej: `stock` o `email`).
- **Resultado esperado:** Actualización de los datos en disco y mensaje de "Registro actualizado con éxito".

## 4. Eliminación de Registros (Eliminar)
**Descripción:** Remoción de una entrada de la base de datos, verificando que no existan restricciones de integridad referencial.
- **Entrada esperada:** ID del registro a eliminar.
- **Resultado esperado:** Eliminación física del registro y actualización de los índices del sistema.

## 5. Búsqueda por Índices Secundarios (Buscar)
**Descripción:** Consulta filtrada utilizando campos indexados para optimizar el tiempo de respuesta.
- **Entrada esperada:** Cadena de búsqueda (ej: ISBN o DNI).
- **Resultado esperado:** Conjunto de resultados que coinciden exactamente con el criterio de búsqueda indexado.

## 6. Listado General de Tablas (Listar)
**Descripción:** Operación de escaneo completo para visualizar todos los registros almacenados en una entidad.
- **Entrada esperada:** Comando de selección de tabla (ej: `Listar Libros`).
- **Resultado esperado:** Volcado de datos de todos los registros activos en la tabla seleccionada.

## 7. Consultas Frecuentes en PostgreSQL

### ¿Cómo listar todas las bases de datos?
Esta consulta permite ver todas las bases de datos disponibles en el servidor PostgreSQL.

```sql
SELECT datname
FROM pg_database
WHERE datistemplate = false;
```

**Resultado esperado:** Lista con los nombres de las bases de datos creadas por los usuarios.

### ¿Cómo listar las tablas de una base de datos?
Permite visualizar todas las tablas pertenecientes al esquema público.

```sql
SELECT tablename
FROM pg_tables
WHERE schemaname = 'public';
```

**Resultado esperado:** Lista con los nombres de las tablas existentes.

### ¿Cómo ver la estructura de una tabla?
Muestra las columnas, tipos de datos y restricciones básicas de una tabla.

```sql
SELECT
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'nombre_tabla';
```

**Resultado esperado:** Información detallada de cada columna de la tabla seleccionada.

## 8. Consultas de Diagnóstico

### ¿Cómo ver las conexiones activas en PostgreSQL?
Permite conocer qué usuarios o aplicaciones están conectados actualmente al servidor.

```sql
SELECT
    pid,
    usename,
    application_name,
    state
FROM pg_stat_activity;
```

**Resultado esperado:** Lista de procesos y conexiones activas.

### ¿Cómo consultar el tamaño de una tabla?
Permite identificar cuánto espacio ocupa una tabla en la base de datos.

```sql
SELECT
    relname AS tabla,
    pg_size_pretty(pg_total_relation_size(relid)) AS tamano
FROM pg_catalog.pg_statio_user_tables;
```

**Resultado esperado:** Nombre de la tabla y tamaño ocupado en disco.

## 9. Diferencias entre MySQL y PostgreSQL

### ¿Cómo listar bases de datos?

**MySQL**

```sql
SHOW DATABASES;
```

**PostgreSQL**

```sql
SELECT datname
FROM pg_database
WHERE datistemplate = false;
```

**Diferencia:** MySQL utiliza un comando directo, mientras que PostgreSQL consulta información almacenada en tablas del sistema.

### ¿Cómo listar tablas?

**MySQL**

```sql
SHOW TABLES;
```

**PostgreSQL**

```sql
SELECT tablename
FROM pg_tables
WHERE schemaname = 'public';
```

**Diferencia:** PostgreSQL organiza la información mediante esquemas y vistas del sistema.

### ¿Cómo describir una tabla?

**MySQL**

```sql
DESCRIBE nombre_tabla;
```

**PostgreSQL**

```sql
SELECT
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'nombre_tabla';
```

**Diferencia:** PostgreSQL utiliza el estándar `information_schema` para consultar metadatos.
