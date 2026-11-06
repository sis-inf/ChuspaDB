# Integración Continua (CI)

## Introducción

La Integración Continua (CI) es una práctica que permite validar automáticamente los cambios realizados en un proyecto antes de integrarlos a las ramas principales. Su objetivo es detectar errores de forma temprana, verificar la calidad del código y facilitar la colaboración entre los contribuyentes.

En el proyecto ChuspaDB se utilizan workflows de GitHub Actions para validar Pull Requests, ejecutar verificaciones de seguridad y apoyar el proceso de despliegue.

---

## Workflows del proyecto

| Workflow      | Trigger                                      | Descripción                                                                   |
| ------------- | -------------------------------------------- | ----------------------------------------------------------------------------- |
| CI            | Pull Request hacia `dev` o `main`            | Verifica que el Pull Request tenga una descripción y que referencie un issue. |
| Deploy        | Push hacia `main`                            | Ejecuta el flujo de despliegue definido para el proyecto.                     |
| Security Scan | Push hacia `dev` y Pull Request hacia `main` | Realiza análisis de seguridad mediante CodeQL.                                |

---

## Flujo de un Pull Request

```text
Issue
  ↓
Crear rama desde dev
  ↓
Realizar cambios
  ↓
Commit
  ↓
Push al fork
  ↓
Crear Pull Request
  ↓
Ejecución del CI
  ↓
Revisión
  ↓
Merge a dev
```

---

## Cómo corregir errores del CI

### Errores de compilación

Los errores de compilación ocurren cuando existen problemas de sintaxis, clases inexistentes, dependencias faltantes o incompatibilidades en el código.

Ejemplo:

```text
Compilation failure
```

Para corregirlos:

1. Revisar el archivo indicado por Maven.
2. Corregir errores de sintaxis.
3. Verificar imports y dependencias.
4. Ejecutar nuevamente la compilación.

---

### Errores de Checkstyle

Checkstyle permite verificar que el código siga las convenciones y estándares definidos para el proyecto.

Ejemplo:

```text
Line is longer than allowed
```

Pasos para corregir:

1. Identificar el archivo reportado por Checkstyle.
2. Revisar el formato del código.
3. Corregir nombres de clases, métodos o variables si es necesario.
4. Ajustar la indentación o longitud de líneas.
5. Ejecutar nuevamente la validación.

Ejemplo:

```java
public class libro
```

Corrección:

```java
public class Libro
```

Comando de verificación:

```bash
mvn checkstyle:check
```

---

### Errores de Surefire

Surefire es el plugin encargado de ejecutar las pruebas automáticas del proyecto.

Ejemplo:

```text
Tests run: 10, Failures: 1
```

Pasos para corregir:

1. Identificar qué prueba falló.
2. Revisar el mensaje de error.
3. Corregir el código o la prueba correspondiente.
4. Ejecutar nuevamente las pruebas.

Comando:

```bash
mvn test
```

---

### Errores de SpotBugs

En algunos pipelines de integración continua puede utilizarse SpotBugs para realizar análisis estático del código y detectar posibles defectos antes de la ejecución de la aplicación.

Ejemplo:

```text
NP_NULL_ON_SOME_PATH
```


---

### Errores detectados por CodeQL

El workflow Security Scan utiliza CodeQL para identificar posibles vulnerabilidades de seguridad.

Pasos para corregir:

1. Revisar el reporte generado por GitHub.
2. Identificar el archivo afectado.
3. Analizar la advertencia mostrada.
4. Aplicar la corrección correspondiente.
5. Crear un nuevo commit y push.

---

## Ejecución local del CI

Antes de abrir un Pull Request se recomienda ejecutar las validaciones localmente.

Verificación completa:

```bash
mvn verify
```

Otras verificaciones útiles:

```bash
mvn compile
mvn test
mvn checkstyle:check
```

Estas verificaciones permiten detectar errores antes de enviar cambios al repositorio remoto.

---

## Conclusión

La integración continua permite mantener la calidad del proyecto mediante validaciones automáticas, ejecución de pruebas y análisis de seguridad. Aplicar estas verificaciones antes de abrir un Pull Request reduce errores, facilita la revisión del código y mejora la estabilidad del proyecto.
