# Despliegue del Proyecto ChuspaDB

## Requisitos

Para ejecutar el proyecto necesitas:

- Java JDK
- Maven
- Git

El motor de base de datos es configurable según la necesidad del usuario.

Motores soportados:

- SQLite
- MySQL
- PostgreSQL

PostgreSQL no es un requisito obligatorio. Solo debe utilizarse si el usuario selecciona esa configuración específica de base de datos.

---

## Clonar el repositorio

```bash
git clone https://github.com/sis-inf/ChuspaDB.git
cd ChuspaDB
```

---

## Compilación, empaquetado JAR y ejecución

Para compilar el proyecto, ejecutar las pruebas y generar el paquete `.jar` de `chuspadb` con Maven:

```bash
mvn clean package
```

El artefacto generado se ubica en el directorio `target/` bajo el nombre `chuspadb` (por ejemplo, `target/chuspadb-1.0-SNAPSHOT.jar`).

Para ejecutar la aplicación directamente con Maven:

```bash
mvn clean javafx:run
```

Para ejecutar el archivo `.jar` empaquetado:

```bash
java -jar target/chuspadb-1.0-SNAPSHOT.jar
```

Los pasos adicionales para configurar el entorno se encuentran detallados en la siguiente guía:

[Guía de instalación](instalacion.md)

Esta guía contiene los requisitos adicionales, configuración del entorno y comandos necesarios para iniciar la aplicación correctamente.
