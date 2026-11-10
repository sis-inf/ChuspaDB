# Referencia SQL para Motores Compatibles

## Introducción

ChuspaDB permite trabajar con distintos motores de bases de datos mediante JDBC.

Actualmente los motores soportados son:

- MySQL
- PostgreSQL
- SQLite

Aunque los tres utilizan SQL, existen diferencias de sintaxis y comportamiento que pueden afectar la ejecución de consultas. Este documento resume las diferencias más comunes.

---

## Tipos de datos

| Propósito | MySQL | PostgreSQL | SQLite |
|------------|--------|------------|---------|
| Texto corto | VARCHAR(n) | VARCHAR(n) | TEXT |
| Texto largo | TEXT | TEXT | TEXT |
| Entero | INTEGER / INT | INTEGER | INTEGER |
| Entero grande | BIGINT | BIGINT | INTEGER |
| Decimal | DECIMAL(p,s) | NUMERIC(p,s) | REAL |
| Fecha | DATE | DATE | TEXT o DATE |
| Fecha y hora | DATETIME | TIMESTAMP | TEXT |
| Booleano | BOOLEAN | BOOLEAN | INTEGER |

### Ejemplo

MySQL:

```sql
CREATE TABLE usuarios (
    id INT,
    nombre VARCHAR(100)
);
```

PostgreSQL:

```sql
CREATE TABLE usuarios (
    id INTEGER,
    nombre VARCHAR(100)
);
```

SQLite:

```sql
CREATE TABLE usuarios (
    id INTEGER,
    nombre TEXT
);
```

---

## Funciones de fecha y hora

Las funciones de fecha pueden variar según el motor utilizado.

| Operación | MySQL | PostgreSQL | SQLite |
|------------|--------|------------|---------|
| Fecha y hora actual | NOW() | NOW() | CURRENT_TIMESTAMP |
| Fecha actual | CURDATE() | CURRENT_DATE | CURRENT_DATE |
| Hora actual | CURTIME() | CURRENT_TIME | CURRENT_TIME |

### Ejemplos

MySQL:

```sql
SELECT NOW();
```

PostgreSQL:

```sql
SELECT NOW();
```

SQLite:

```sql
SELECT CURRENT_TIMESTAMP;
```

---

## Auto incremento

Cada motor utiliza una estrategia diferente para generar claves primarias automáticas.

| Motor | Sintaxis |
|---------|-----------|
| MySQL | AUTO_INCREMENT |
| PostgreSQL | SERIAL |
| SQLite | INTEGER PRIMARY KEY AUTOINCREMENT |

### MySQL

```sql
CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY
);
```

### PostgreSQL

```sql
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY
);
```

### SQLite

```sql
CREATE TABLE clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT
);
```

---

## Paginación de resultados

La paginación permite limitar la cantidad de filas recuperadas.

### LIMIT

Compatible con MySQL, PostgreSQL y SQLite.

```sql
SELECT *
FROM clientes
LIMIT 100;
```

### OFFSET

Compatible con los tres motores.

```sql
SELECT *
FROM clientes
LIMIT 100 OFFSET 50;
```

### FETCH FIRST

FETCH FIRST forma parte del estándar SQL y es soportado por PostgreSQL.

Para maximizar la compatibilidad entre MySQL, PostgreSQL y SQLite se recomienda utilizar LIMIT.
---

## Comentarios SQL

### Comentario de una línea

Compatible con todos los motores.

```sql
-- Comentario
SELECT * FROM clientes;
```

### Comentario multilínea

Compatible con todos los motores.

```sql
/*
Comentario
multilínea
*/
SELECT * FROM clientes;
```

---

## Diferencias importantes entre motores

| Característica | MySQL | PostgreSQL | SQLite |
|----------------|--------|------------|---------|
| AUTO_INCREMENT | Sí | No | No |
| SERIAL | No | Sí | No |
| AUTOINCREMENT | No | No | Sí |
| NOW() | Sí | Sí | No |
| CURRENT_TIMESTAMP | Sí | Sí | Sí |
| FETCH FIRST | Parcial | Sí | No |
| LIMIT | Sí | Sí | Sí |

---

## Validaciones realizadas por ChuspaDB

Actualmente ChuspaDB valida y clasifica consultas SQL mediante la clase `SqlValidator`.

Las sentencias reconocidas son:

- SELECT
- INSERT
- UPDATE
- DELETE
- CREATE
- DROP
- ALTER

Además, ChuspaDB identifica algunas operaciones potencialmente destructivas, por ejemplo:

- DELETE
- DROP
- UPDATE sin cláusula WHERE

Actualmente ChuspaDB no realiza conversiones automáticas de sintaxis entre MySQL, PostgreSQL y SQLite ni muestra advertencias específicas sobre incompatibilidades entre motores.

Por este motivo, el usuario debe utilizar la sintaxis adecuada para el motor seleccionado.
---

## Recomendaciones

Para obtener la mayor compatibilidad posible entre motores:

- Utilizar `LIMIT` para paginar resultados.
- Preferir tipos estándar como `INTEGER` y `VARCHAR`.
- Verificar las funciones de fecha antes de ejecutar consultas.
- Revisar la sintaxis de auto incremento según el motor utilizado.
- Probar scripts SQL en el motor correspondiente antes de ejecutarlos sobre datos importantes.

Estas recomendaciones ayudan a reducir problemas de compatibilidad entre MySQL, PostgreSQL y SQLite.
