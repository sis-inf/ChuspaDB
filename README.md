# ChuspaDB

Gestor de base de datos con interfaz gráfica desarrollado en Java y JavaFX.

## Características

🗄️ **Motores soportados**

* MySQL
* SQLite
* PostgreSQL

📤 **Exportación**

* CSV
* JSON

✍️ **Editor SQL**

* Historial de consultas
* Favoritos
* Resaltado de sintaxis
* Generador de SQL

🛡️ **Seguridad y ejecución**

* Validación de operaciones DML
* Ejecución asíncrona de consultas

## ¿Qué es?

ChuspaDB es una aplicación que permite conectarse, explorar y administrar bases de datos utilizando motores como MySQL, SQLite y PostgreSQL mediante una interfaz gráfica desarrollada con Java y JavaFX.

## ¿Para quién es?

ChuspaDB está dirigido a:

- Estudiantes de ingeniería
- Administradores de bases de datos (DBAs)
- Desarrolladores que trabajan con MySQL, SQLite y PostgreSQL

## ¿Qué problema resuelve?

ChuspaDB ayuda a resolver:

- Tareas comunes de exploración y gestión de bases de datos
- Simplificar la administración de bases de datos para usuarios con menos experiencia técnica
- Visualizar datos de forma clara e intuitiva

## Instalación

### Requisitos del sistema

- JDK 17 o superior
- Maven 3.8 o superior
- Un motor de base de datos configurado

JavaFX no se instala por separado: Maven lo descarga como dependencia.

Para comprobar que tienes los requisitos, ejecuta:

```bash
java -version
mvn -version
```

### Motores soportados oficialmente

- MySQL
- PostgreSQL
- SQLite

MySQL y PostgreSQL necesitan un servidor en ejecución al que conectarte. SQLite trabaja con un archivo local y no necesita servidor.

### Configuración opcional

ChuspaDB no necesita variables de entorno. Las conexiones guardadas se almacenan en `conexiones.json`, dentro de la carpeta `.ChuspaDB` del directorio del usuario.

Modo portable: si ejecutas la aplicación desde un archivo `.jar` y colocas un archivo vacío llamado `portable.flag` junto al `.jar`, la carpeta `.ChuspaDB` se crea junto al ejecutable en lugar del directorio del usuario.

### Pasos

```bash
# 1. Clona el repositorio:
git clone https://github.com/sis-inf/ChuspaDB.git
cd ChuspaDB

# 2. Compila el proyecto (borra la compilación anterior y compila desde cero):
mvn clean compile

# 3. Ejecuta la aplicación (abre la ventana de ChuspaDB):
mvn javafx:run
```

## Uso rápido
- Abre ChuspaDB y completa los datos de conexión (host, puerto, usuario y contraseña)
- Selecciona la base de datos que deseas explorar
- Navega por los esquemas y tablas desde el panel lateral
- Ejecuta consultas SQL desde el editor integrado y visualiza los resultados en la tabla de resultados

## Documentación
Ver la carpeta [docs/](docs/)

## Contribuir
Ver [CONTRIBUTING.md](CONTRIBUTING.md)

## Licencia
MIT — ver [LICENSE](LICENSE)
