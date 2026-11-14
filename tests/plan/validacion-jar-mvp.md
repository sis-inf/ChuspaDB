# Validación de compilación Maven y generación del JAR — MVP v0.1.0

- Issue: #1340
- Fecha de ejecución: 2026-11-13
- Responsable: David Moisés Ramírez (`dr442918-sys`)
- Rama de trabajo: `test/release-validacion-jar-mvp`
- Commits probados: `e024af0` y `c73978f` de la rama `dev` del repositorio oficial
- Resultado global: no aprobado. La compilación falla y no se genera el JAR.

## 1. Objetivo

Verificar que ChuspaDB se compila con Maven (`mvn clean package`) y que se genera un JAR ejecutable, como paso previo al MVP v0.1.0. El documento registra el procedimiento, los errores y los resultados obtenidos.

## 2. Resumen del resultado

| Criterio | Resultado | Detalle |
| --- | --- | --- |
| Procedimiento de compilación documentado | Cumple | Sección 4 |
| Compilación sin errores | No cumple | 6 errores de compilación en las dos ejecuciones |
| Generación del JAR | No cumple | No existe ningún archivo `target/*.jar` |
| Ejecución del JAR | No realizada | No hay JAR que ejecutar |
| Registro de errores y evidencias | Cumple | Secciones 5 y 10 |

## 3. Entorno

| Elemento | Valor |
| --- | --- |
| Sistema operativo | Windows 10 (10.0), amd64 |
| JDK | OpenJDK Temurin 17.0.21+9 (Eclipse Adoptium) |
| Maven | Apache Maven 3.10.0 |
| Terminal | Git Bash |
| Codificación de la plataforma | Cp1252 (configuración regional es_BO) |
| Requisitos del proyecto | JDK 17 o superior y Maven 3.8 o superior (README); `release 17` en `pom.xml` |

Antes de la prueba el equipo tenía instalado solo Java 1.8.0_503 y no tenía Maven. Se instalaron el JDK 17 y Maven 3.10.0 para cumplir los requisitos del README.

## 4. Procedimiento

Todos los comandos se ejecutaron en Git Bash. La primera ejecución descarga las dependencias desde Maven Central y necesita conexión a internet.

1. Clonar el fork y agregar el repositorio oficial como `upstream`.
2. Crear la rama `test/release-validacion-jar-mvp` desde `upstream/dev`.
3. Activar el JDK 17 y Maven en la sesión de Git Bash y comprobar las versiones.
4. Ejecutar `mvn clean package` guardando la salida en un archivo.
5. Comprobar si se generó el JAR en `target/`.
6. Si existe el JAR, ejecutarlo. Si no existe, registrar el resultado.

```bash
git clone https://github.com/dr442918-sys/ChuspaDB.git
cd ChuspaDB
git remote add upstream https://github.com/sis-inf/ChuspaDB.git
git fetch upstream
git checkout -b test/release-validacion-jar-mvp upstream/dev

export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.21.9-hotspot"
export PATH="$JAVA_HOME/bin:$HOME/apache-maven-3.10.0/bin:$PATH"
java -version
mvn -version

mvn clean package 2>&1 | tee evidencia-mvn-package.txt
ls -la target/*.jar
```

## 5. Resultados de la compilación

### 5.1 Primera ejecución sobre el commit e024af0

| Dato | Valor |
| --- | --- |
| Commit | `e024af0` (Merge pull request #1367) |
| Comando | `mvn clean package` |
| Finalizó | 2026-11-13 20:51:10 (UTC-04:00) |
| Duración | 49,912 s (incluye la descarga de dependencias) |
| Fase que falla | `maven-compiler-plugin:3.16.0:compile` |
| Archivos compilados | 51 archivos fuente, `release 17`, ruta de módulos |
| Resultado | `BUILD FAILURE`, 6 errores |
| JAR generado | No |

### 5.2 Segunda ejecución sobre el commit c73978f

Antes de repetir la prueba se actualizó la rama al último `dev`: habían entrado 10 commits nuevos.

| Dato | Valor |
| --- | --- |
| Commit | `c73978f` (fix(servicio): escapar caracteres especiales en encabezados CSV, #1368) |
| Comando | `mvn clean package` |
| Finalizó | 2026-11-13 21:08:33 (UTC-04:00) |
| Duración | 18,304 s |
| Fase que falla | `maven-compiler-plugin:3.16.0:compile` |
| Archivos compilados | 51 archivos fuente, `release 17`, ruta de módulos |
| Resultado | `BUILD FAILURE`, 6 errores |
| JAR generado | No |

Ninguno de los 10 commits nuevos modificó `module-info.java`, `ErrorEstante.java`, `HistorialQuerys.java`, `EntradaHistorial.java`, `PanelEditorSQLController.java` ni `pom.xml`, y los 6 errores se repitieron idénticos.

### 5.3 Errores de compilación

Las rutas son relativas a `src/main/java/edu/sisinf/estante/`. La causa se identificó leyendo el código, sin modificarlo.

| N.º | Archivo | Línea | Mensaje del compilador | Causa identificada |
| --- | --- | --- | --- | --- |
| 1 | `core/ErrorEstante.java` | 7 | `class ErrorChuspaDB is public, should be declared in a file named ErrorChuspaDB.java` | El archivo declara `public class ErrorChuspaDB`. El commit `ae54513` (#1154) renombró la clase pero no el archivo. |
| 2 | `controller/PanelEditorSQLController.java` | 12 | `package org.fxmisc.richtext is not visible` | `module-info.java` no declara ningún `requires` para RichTextFX, aunque `pom.xml` incluye `richtextfx` 0.11.2. |
| 3 | `controller/PanelEditorSQLController.java` | 13 | `package org.fxmisc.richtext.model is not visible` | La misma causa que el error 2. |
| 4 | `controller/PanelEditorSQLController.java` | 14 | `package org.fxmisc.richtext.model is not visible` | La misma causa que el error 2. |
| 5 | `servicio/HistorialQuerys.java` | 90 | `cannot find symbol: method getBaseDatos()` | `EntradaHistorial` es un `record` con los componentes `baseDatos` y `exitosa`. Sus métodos de acceso son `baseDatos()` y `exitosa()`. |
| 6 | `servicio/HistorialQuerys.java` | 106 | `cannot find symbol: method isExitosa()` | La misma causa que el error 5. |

Las carpetas del código fuente siguen llamándose `estante`, mientras que `module-info.java` y los mensajes del compilador usan el paquete `edu.sisinf.chuspadb`. Esto sugiere que la migración de nombres #1154 está incompleta en `dev`.

### 5.4 Advertencias

Las advertencias no impiden compilar.

| Advertencia | Significado |
| --- | --- |
| `Required filename-based automodules detected: [mysql-connector-j-26.10.0.jar, commons-math3-3.6.1.jar]` | Dos dependencias se usan como módulos automáticos. Maven recomienda no publicar el proyecto en un repositorio público de artefactos. |
| `6 problems were encountered while building the effective model for org.openjfx` | Problemas al leer el modelo de `javafx-controls` y `javafx-fxml` 21.0.2 durante la resolución de dependencias. |

### 5.5 Verificación en el CI del repositorio

| Dato | Valor |
| --- | --- |
| Workflow | Despliegue / compilación y despliegue (push), asociado al último commit de `main` en ese momento |
| Comando | `mvn -B test` |
| Entorno | Servidor Linux de GitHub Actions, JDK Temurin 17.0.21+9 |
| Resultado | `BUILD FAILURE` en 8,034 s. El check falló a los 14 s. |
| Finalizó | 2026-11-13T23:36:59Z |
| Errores | Los mismos 6 errores de las secciones 5.1 y 5.2 |

El mismo fallo en un entorno distinto (Linux, servidor de GitHub) descarta que el problema se deba a la instalación local. El estado del CI sobre la rama `dev` no se verificó en esta prueba.

## 6. Verificación de la generación del JAR

```bash
ls -la target/*.jar
```

```text
ls: cannot access 'target/*.jar': No such file or directory
```

Resultado: el JAR no se generó en ninguna de las dos ejecuciones. El proceso se detiene en la fase de compilación, antes de las fases de pruebas y de empaquetado.

## 7. Prueba de ejecución del JAR

Resultado: no realizada, porque no existe ningún JAR que ejecutar.

Para completar esta prueba cuando la compilación pase:

1. Ejecutar `mvn clean package` hasta obtener `BUILD SUCCESS`.
2. Comprobar el archivo generado con `ls -la target/*.jar`.
3. Ejecutar el JAR y comprobar que la aplicación inicia y abre su ventana principal.
4. Registrar el resultado y los errores en este documento.

## 8. Observaciones adicionales

Tras clonar el repositorio y cambiar de rama, Git marcó 25 archivos como modificados sin que se hubieran editado. El comando `git diff --ignore-cr-at-eol` no mostró diferencias de contenido, por lo que lo más probable es que se trate de diferencias de saltos de línea (CRLF) frente a la regla `eol=lf` de `.gitattributes`. Esto no afecta a la compilación.

## 9. Conclusión y recomendaciones

Con el último `dev` probado (`c73978f`) el proyecto no compila, por lo que no se puede generar el JAR ni validar su ejecución. El MVP v0.1.0 no está listo para empaquetarse hasta resolver los problemas de la sección 5.3.

Correcciones sugeridas, fuera del alcance de este issue, que solo documenta la validación:

1. Renombrar `ErrorEstante.java` a `ErrorChuspaDB.java`, o ajustar el nombre de la clase.
2. Declarar en `module-info.java` la lectura de RichTextFX.
3. Usar `baseDatos()` y `exitosa()` en `HistorialQuerys`, o agregar los métodos que espera al `record` `EntradaHistorial`.
4. Repetir esta validación y completar las secciones 6 y 7 cuando la compilación pase.

## 10. Evidencias

Extracto de la segunda ejecución (commit `c73978f`), con las rutas abreviadas:

```text
[INFO] Compiling 51 source files with javac [debug release 17 module-path] to target\classes
[ERROR] COMPILATION ERROR :
[ERROR] core/ErrorEstante.java:[7,8] class ErrorChuspaDB is public, should be declared in a file named ErrorChuspaDB.java
[ERROR] controller/PanelEditorSQLController.java:[12,18] package org.fxmisc.richtext is not visible
[ERROR] controller/PanelEditorSQLController.java:[13,27] package org.fxmisc.richtext.model is not visible
[ERROR] controller/PanelEditorSQLController.java:[14,27] package org.fxmisc.richtext.model is not visible
[ERROR] servicio/HistorialQuerys.java:[90,55] cannot find symbol: method getBaseDatos()
[ERROR] servicio/HistorialQuerys.java:[106,24] cannot find symbol: method isExitosa()
[INFO] 6 errors
[ERROR] BUILD FAILURE
[INFO] Total time:  18.304 s
[INFO] Finished at: 2026-11-13T21:08:33-04:00
```

Los registros completos de las dos ejecuciones y del entorno se conservaron localmente.
