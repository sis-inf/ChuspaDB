# Persistencia de Datos

## Introducción

ChuspaDB almacena información del usuario para conservar configuraciones y consultas favoritas entre distintas sesiones. La persistencia se realiza mediante archivos JSON almacenados en el directorio personal del usuario.

---

# Directorio de datos

Los datos persistentes de ChuspaDB se almacenan en:

```text
~/.ChuspaDB/
```

Estructura típica:

```text
~/.ChuspaDB/
├── conexiones.json
└── favoritos.json
```

Descripción:

* `conexiones.json`: almacena las conexiones registradas por el usuario.
* `favoritos.json`: almacena las consultas SQL marcadas como favoritas.

---

# conexiones.json

Este archivo almacena las conexiones configuradas por el usuario.

## Campos

| Campo     | Descripción                                |
| --------- | ------------------------------------------ |
| id        | Identificador único de la conexión         |
| nombre    | Nombre de la conexión                      |
| host      | Dirección del servidor                     |
| puerto    | Puerto de conexión                         |
| basedatos | Nombre de la base de datos                 |
| usuario   | Usuario de acceso                          |
| password  | Contraseña de acceso                       |
| tipoMotor | Motor de base de datos                     |
| usarSSL   | Indica si la conexión utiliza SSL          |
| etiquetas | Lista de etiquetas asociadas a la conexión |

## Ejemplo

```json
[
  {
    "id": "a1b2c3d4",
    "nombre": "Produccion",
    "host": "db.ejemplo.com",
    "puerto": 5432,
    "basedatos": "ventas",
    "usuario": "admin",
    "password": "secreto",
    "tipoMotor": "POSTGRESQL",
    "usarSSL": true,
    "etiquetas": [
      "produccion",
      "ventas"
    ]
  }
]
```

---

# favoritos.json

Este archivo almacena las consultas SQL guardadas como favoritas.

## Campos

| Campo         | Descripción                     |
| ------------- | ------------------------------- |
| nombre        | Nombre del favorito             |
| sql           | Consulta SQL                    |
| motor         | Motor de base de datos asociado |
| fechaCreacion | Fecha y hora de creación        |

## Ejemplo

```json
[
  {
    "nombre": "Clientes activos",
    "sql": "SELECT * FROM clientes WHERE activo = true;",
    "motor": "POSTGRESQL",
    "fechaCreacion": "2026-01-01T10:30:00"
  }
]
```

---

# Qué NO se persiste

La aplicación mantiene un historial temporal de consultas ejecutadas mediante memoria interna.

Actualmente NO se persiste:

* Historial de consultas ejecutadas.
* Resultados de consultas.
* Estado temporal de la interfaz.
* Información almacenada únicamente durante la ejecución.

El historial de consultas se pierde al cerrar la aplicación.

---

# Backup y restauración

Para realizar una copia de seguridad basta con copiar el directorio completo:

```bash
cp -r ~/.ChuspaDB respaldo-ChuspaDB
```

Para restaurar los datos:

```bash
cp -r respaldo-ChuspaDB ~/.ChuspaDB
```

Después de restaurar los archivos se recomienda reiniciar la aplicación.

---

# Migración a otro equipo

Para mover conexiones y favoritos a otro equipo:

1. Cerrar ChuspaDB.
2. Copiar el directorio `~/.ChuspaDB/`.
3. Transferir el directorio al nuevo equipo.
4. Colocarlo dentro del directorio personal del usuario.
5. Iniciar ChuspaDB nuevamente.

De esta forma se conservan las conexiones configuradas y las consultas favoritas.
