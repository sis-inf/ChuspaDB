# API - Proyecto ChuspaDB

## 1. Descripción

Este documento describe los principales componentes de la API de ChuspaDB, sus responsabilidades, métodos disponibles y ejemplos de uso.

ChuspaDB es una aplicación desarrollada en Java que permite gestionar conexiones con diferentes motores de bases de datos y ejecutar consultas SQL.

## 2. Información del proyecto

Los identificadores actuales del proyecto son:

| Propiedad | Valor |
|---|---|
| Nombre del proyecto | ChuspaDB |
| Grupo Maven | `edu.sisinf` |
| Artefacto Maven | `chuspadb` |
| Versión | `0.1.0` |
| Paquete principal | `edu.sisinf.chuspadb` |

## 3. Clase ConnectionProvider

La clase `ConnectionProvider` pertenece al paquete:

`edu.sisinf.chuspadb.core`

Es responsable de gestionar conexiones JDBC y ejecutar sentencias SQL sobre una conexión activa.

### 3.1. Método abrir(DBConfig config)

Abre una conexión JDBC utilizando los datos proporcionados mediante `DBConfig`.

- Parámetro: configuración de conexión.
- Retorno: una conexión JDBC activa.
- Excepciones: `IllegalArgumentException` y `SQLException`.

### 3.2. Método executeSelect(Connection connection, String sql)

Ejecuta una sentencia SQL de tipo SELECT.

- Parámetros: conexión JDBC y sentencia SQL.
- Retorno: un objeto `QueryResult` con las columnas y filas obtenidas.
- Validación: solamente permite sentencias de lectura.
- Excepciones: `IllegalArgumentException` y `ErrorQuery`.

### 3.3. Método executeUpdate(Connection connection, String sql)

Ejecuta sentencias SQL de escritura.

Admite operaciones como:

- INSERT
- UPDATE
- DELETE

- Parámetros: conexión JDBC y sentencia SQL.
- Retorno: cantidad de filas afectadas.
- Validación: no permite sentencias SELECT.
- Excepciones: `IllegalArgumentException` y `ErrorQuery`.

## 4. Ejemplos de uso

Los siguientes ejemplos representan los comandos Maven utilizados para trabajar con el proyecto.

### 4.1. Limpiar y compilar

```bash
mvn clean compile
```

### 4.2. Ejecutar las pruebas

```bash
mvn test
```

### 4.3. Generar el paquete

```bash
mvn package
```

### 4.4. Ejecutar la aplicación

```bash
mvn javafx:run
```

## 5. Identificación del artefacto Maven

El artefacto del proyecto se identifica mediante las siguientes coordenadas:

```xml
<groupId>edu.sisinf</groupId>
<artifactId>chuspadb</artifactId>
<version>0.1.0</version>
```

Estas coordenadas corresponden a la configuración actual del proyecto.

## 6. Consideraciones

- Los nombres de paquetes deben utilizar la raíz `edu.sisinf.chuspadb`.
- Las operaciones de lectura deben ejecutarse mediante `executeSelect`.
- Las operaciones de escritura deben ejecutarse mediante `executeUpdate`.
- Los comandos Maven deben mantenerse sincronizados con la configuración del proyecto.
- La documentación debe actualizarse cuando cambie la implementación de la API.
