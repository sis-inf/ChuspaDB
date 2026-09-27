# Diccionario de Datos - Sistema Estante

| Información | Valor |
|---|---|
| Proyecto | Estante |
| Versión | 1.0 |
| Motores de Base de Datos soportados | SQLite, MySQL, PostgreSQL (`TipoMotor`) |
| Tecnología | Java + JavaFX |
| Última actualización | 2026-06-13 |

---

# Descripción General

Este documento describe las estructuras de datos reales utilizadas por el sistema **Estante**, un gestor visual de bases de datos desarrollado en Java y JavaFX que soporta tres motores: SQLite, MySQL y PostgreSQL (enum `TipoMotor`).

El sistema no mantiene tablas SQL propias: persiste las conexiones y las consultas favoritas en archivos JSON, y mantiene el historial de consultas ejecutadas únicamente en memoria durante la sesión. El objetivo de este diccionario es definir de manera clara esos archivos JSON, records y reglas de negocio utilizados por el sistema para la administración de conexiones, la ejecución de consultas SQL y el registro temporal del historial de operaciones.

---

# Convenciones Utilizadas

| Convención | Descripción |
|---|---|
| PK | Primary Key |
| FK | Foreign Key |
| NOT NULL | Campo obligatorio |
| UNIQUE | Valor único |
| AUTOINCREMENT | Valor incremental automático |
| DEFAULT | Valor por defecto |
| INDEX | Índice para optimización de búsquedas |

---

# conexiones.json

Almacena los perfiles de conexión configurados por el usuario. Permite guardar conexiones frecuentes y reutilizarlas desde la interfaz gráfica del sistema.

| Campo       | Descripción                                                           |
| ----------- | ---------------------------------------------------------------------- |
| `id`        | Identificador único de la conexión                                    |
| `nombre`    | Nombre descriptivo de la conexión                                     |
| `host`      | Dirección del servidor                                                |
| `puerto`    | Puerto de conexión                                                    |
| `basedatos` | Nombre de la base de datos                                            |
| `usuario`   | Usuario de acceso                                                     |
| `password`  | Contraseña de acceso                                                  |
| `tipoMotor` | Motor de base de datos (`TipoMotor`: `SQLITE`, `MYSQL`, `POSTGRESQL`) |
| `usarSSL`   | Indica si la conexión utiliza SSL                                     |
| `etiquetas` | Lista de etiquetas asociadas a la conexión                            |

---

# favoritos.json

Almacena las consultas SQL marcadas como favoritas por el usuario para reutilizarlas rápidamente. Corresponde al record `FavoritoQuery` (`edu.sisinf.estante.modelo.FavoritoQuery`).

| Campo           | Descripción                                  |
| --------------- | ---------------------------------------------- |
| `nombre`        | Nombre descriptivo de la consulta favorita     |
| `sql`           | Sentencia SQL almacenada                       |
| `motor`         | Motor de base de datos asociado (`TipoMotor`)  |
| `fechaCreacion` | Fecha y hora de creación del favorito          |

---

# Historial de consultas (no persistente)

El historial de consultas ejecutadas **no se persiste**: se mantiene únicamente en memoria interna durante la sesión y se pierde al cerrar la aplicación.

Mientras la aplicación está en ejecución, cada consulta genera un `EntradaHistorial` (`edu.sisinf.estante.modelo.EntradaHistorial`) con la siguiente forma:

| Nombre Campo  | Tipo de Dato | Descripción                                       | Restricciones |
| ------------- | ------------ | -------------------------------------------------- | ------------- |
| `timestamp`   | BIGINT       | Momento de ejecución de la query (epoch en milisegundos) | NOT NULL      |
| `query`       | TEXT         | Consulta SQL ejecutada                             | NOT NULL      |
| `base_datos`  | VARCHAR(100) | Base de datos o conexión activa al ejecutar la consulta | NOT NULL      |
| `duracion_ms` | BIGINT       | Tiempo de ejecución en milisegundos                | NOT NULL      |
| `exitosa`     | BOOLEAN      | Indica si la ejecución fue exitosa                 | NOT NULL      |

---

# Record: COLUMNA_INFO

Contiene información descriptiva sobre una columna de una tabla de base de datos.

| Nombre Campo    | Tipo de Dato | Descripción                                | Restricciones |
| --------------- | ------------ | ------------------------------------------ | ------------- |
| `nombre`        | VARCHAR(100) | Nombre de la columna                       | NOT NULL      |
| `tipo_sql`      | VARCHAR(50)  | Tipo de dato SQL de la columna             | NOT NULL      |
| `nullable`      | BOOLEAN      | Indica si la columna admite valores nulos  | NOT NULL      |
| `valor_default` | VARCHAR(255) | Valor por defecto definido para la columna | NULL          |

---

# Record: IMPORTACION_RESULTADO

Representa el resultado de una operación de importación de datos desde archivos externos.

| Nombre Campo      | Tipo de Dato   | Descripción                                          | Restricciones |
| ------------------ | -------------- | ----------------------------------------------------- | ------------- |
| `filasInsertadas`  | INTEGER        | Número de filas insertadas exitosamente                | NOT NULL      |
| `filasFallidas`    | INTEGER        | Número de filas que fallaron                            | NOT NULL      |
| `errores`          | List\<String\> | Lista de mensajes de error, uno por fila fallida        | NULL          |

---

# Reglas de Negocio

- No se puede registrar una conexión sin especificar host, puerto, base de datos y usuario.
- El puerto por defecto para conexiones MySQL es el 3306.
- Las contraseñas almacenadas deben mantenerse cifradas.
- El sistema debe mostrar mensajes de error comprensibles para el usuario final.
- El historial de consultas debe ser accesible desde la interfaz principal.
- Las consultas SQL ejecutadas deben registrarse en el historial de la sesión junto con su tiempo de respuesta.
- El sistema debe permitir reutilizar conexiones previamente guardadas.
- El tiempo máximo recomendado de respuesta para consultas simples es de 2 segundos.

---

# Consideraciones Técnicas

- Se recomienda utilizar consultas parametrizadas para prevenir ataques de SQL Injection.
- Las contraseñas no deben almacenarse en texto plano.
- El sistema debe validar previamente la conectividad antes de guardar un perfil de conexión.

---

# Escalabilidad Futura

El modelo de datos podrá ampliarse en futuras versiones para incorporar:

- Gestión de usuarios autenticados.
- Consultas SQL favoritas.
- Exportación de resultados.
- Historial de conexiones recientes.
- Configuración avanzada de conexiones.
- Registro de actividad del usuario.
