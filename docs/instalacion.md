# Guía de Instalación - DBMS Estante (Java Edition)

Este documento describe los pasos necesarios para configurar el entorno de desarrollo y ejecutar el Administrador de Base de Datos **Estante** de forma local.

## 1. Requisitos Previos
Para el despliegue del sistema, asegúrese de contar con:
- **Lenguaje de Programación:** [Java JDK 17](https://www.oracle.com/java/technologies/downloads/) o superior.
- **Gestor de Dependencias:** [Apache Maven](https://maven.apache.org/) 3.8+.
- **Sistema Operativo:** Windows 10/11, Linux o macOS.
- **Motor de BD Interno:** SQLite (Gestionado mediante JDBC).

## 2. Clonación del Repositorio
Obtenga el código fuente del motor de base de datos mediante Git:
```bash
git clone https://github.com/sis-inf/estante.git
cd estante
```

## 3. Instalación en Linux (Ubuntu)

### Instalar Java 17

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Instalar Maven

```bash
sudo apt install maven
```

### Verificar instalación

```bash
java --version
mvn --version
```

### Ejecutar el proyecto

```bash
mvn javafx:run
```

---

## 4. Instalación en Windows

### Instalar Java JDK 17

Descargue e instale Java JDK 17 desde el sitio oficial de Oracle o Eclipse Temurin.

### Configurar JAVA_HOME

Ejemplo:

```text
JAVA_HOME=C:\Program Files\Java\jdk-17
```

Agregue también `%JAVA_HOME%\bin` a la variable `PATH`.

### Instalar Maven

Descargue Maven desde:

https://maven.apache.org/download.cgi

Configure la variable:

```text
MAVEN_HOME=C:\apache-maven
```

Agregue también `%MAVEN_HOME%\bin` al `PATH`.

### Verificar instalación

```cmd
java --version
mvn --version
```

### Ejecutar el proyecto

```cmd
mvn javafx:run
```

---

## 5. Instalación en macOS

### Instalar Java 17

```bash
brew install java@17
```

### Instalar Maven

```bash
brew install maven
```

### Verificar instalación

```bash
java --version
mvn --version
```

### Ejecutar el proyecto

```bash
mvn javafx:run
```

---

## 6. Verificación de Instalación

Compruebe que Java y Maven estén correctamente configurados:

```bash
java --version
mvn --version
```

Si ambos comandos muestran información de versión sin errores, la instalación se realizó correctamente.

---

## 7. Ejecución de la Aplicación

Una vez configurado el entorno:

```bash
mvn javafx:run
```
