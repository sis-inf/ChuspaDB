# Estante

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

Estante es una aplicación que permite conectarse, explorar y administrar bases de datos utilizando motores como MySQL, SQLite y PostgreSQL mediante una interfaz gráfica desarrollada con Java y JavaFX.

## ¿Para quién es?

Estante está dirigido a:

- Estudiantes de ingeniería
- Administradores de bases de datos (DBAs)
- Desarrolladores que trabajan con MySQL

## ¿Qué problema resuelve?

Estante ayuda a resolver:

- Tareas comunes de exploración y gestión de bases de datos
- Simplificar la administración de bases de datos para usuarios con menos experiencia técnica
- Visualizar datos de forma clara e intuitiva

## Instalación

### Requisitos del sistema

- Java 17 o superior
- JavaFX SDK compatible con tu versión de Java
- Un motor de base de datos compatible configurado (MySQL, SQLite o PostgreSQL según la configuración utilizada)
  
## Pasos
```bash
# 1. Clona el repositorio:
git clone https://github.com/sis-inf/estante.git
cd estante

# 2. Compila el proyecto:
mvn compile

# 3. Ejecuta la aplicación:
mvn javafx:run
``` 

## Uso rápido
- Abre Estante y completa los datos de conexión (host, puerto, usuario y contraseña)
- Selecciona la base de datos que deseas explorar
- Navega por los esquemas y tablas desde el panel lateral
- Ejecuta consultas SQL desde el editor integrado y visualiza los resultados en la tabla de resultados

## Documentación
Ver la carpeta [docs/](docs/)

## Contribuir
Ver [CONTRIBUTING.md](CONTRIBUTING.md)

## Licencia
MIT — ver [LICENSE](LICENSE)



