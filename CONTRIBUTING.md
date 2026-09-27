# Cómo contribuir a este proyecto

[#cómo-contribuir-a-este-proyecto](#cómo-contribuir-a-este-proyecto)

Gracias por tu interés en contribuir. Este proyecto usa
el **Forking Workflow**. Lee este documento antes de empezar.

---

## Flujo de trabajo

[#flujo-de-trabajo](#flujo-de-trabajo)

### 1. Haz fork del repositorio

[#1-haz-fork-del-repositorio](#1-haz-fork-del-repositorio)

Botón **Fork** en la esquina superior derecha de GitHub.

### 2. Clona tu fork

[#2-clona-tu-fork](#2-clona-tu-fork)

```
git clone https://github.com/TU-USUARIO/PROYECTO.git

cd PROYECTO
```

### 3. Agrega el repo original como upstream

[#3-agrega-el-repo-original-como-upstream](#3-agrega-el-repo-original-como-upstream)

```
git remote add upstream https://github.com/sis-inf/estante.git
```

### 4. Sincroniza antes de trabajar

[#4-sincroniza-antes-de-trabajar](#4-sincroniza-antes-de-trabajar)

```
git checkout dev

git pull upstream dev
```

### 5. Crea tu rama de trabajo

[#5-crea-tu-rama-de-trabajo](#5-crea-tu-rama-de-trabajo)

```
git checkout -b fix/descripcion o feat/descripcion
```

#### Convención de nombres de ramas

[#convención-de-nombres-de-ramas](#convención-de-nombres-de-ramas)

Las ramas deben usar el formato `fix/descripcion` o `feat/descripcion` según corresponda:

**Tipos de ramas:**

- `feat/` → nueva funcionalidad
- `fix/` → corrección de errores
- `docs/` → documentación
- `test/` → pruebas
- `chore/` → tareas generales o configuración
- `refactor/` → mejoras internas
- `security/` → mejoras de seguridad
- `data/` → análisis de datos

**Ejemplos de ramas:**

- feat/endpoint-metricas-cpu
- docs/readme-instalacion
- fix/calculo-ram-incorrecto
- test/pruebas-unitarias-cpu
- chore/configurar-github-actions
- security/analisis-dependencias

### 6. Trabaja y haz commits pequeños

[#6-trabaja-y-haz-commits-pequeños](#6-trabaja-y-haz-commits-pequeños)

```
git add .

git commit -m "tipo: descripción corta en presente - Closes #N"
```

Note

Siempre referencia el issue correspondiente en el commit: Closes #N

### 7. Sube tu rama a tu fork

[#7-sube-tu-rama-a-tu-fork](#7-sube-tu-rama-a-tu-fork)

```
git push origin fix/descripcion o feat/descripcion
```

### 8. Abre un Pull Request

[#8-abre-un-pull-request](#8-abre-un-pull-request)

- Base: `sis-inf/estante` → rama `dev` (Los PRs deben abrirse contra la rama dev, no contra main)
- Compare: `TU-USUARIO/PROYECTO` → tu rama

#### Recomendaciones para el PR

[#recomendaciones-para-el-pr](#recomendaciones-para-el-pr)

- Explica claramente los cambios realizados
- Referencia el issue correspondiente (`Closes #N`)
- Mantén el PR enfocado en un solo objetivo
- Añade evidencia (logs, capturas, etc.) si aplica

---

## Convención de commits

[#convención-de-commits](#convención-de-commits)

| Tipo        | Cuándo usarlo                     |
| ----------- | --------------------------------- |
| `feat:`     | Nueva funcionalidad               |
| `fix:`      | Corrección de error               |
| `docs:`     | Documentación                     |
| `test:`     | Pruebas                           |
| `chore:`    | Configuración o CI/CD             |
| `refactor:` | Mejora sin cambiar comportamiento |
| `security:` | Mejora de seguridad               |
| `data:`     | Análisis de datos                 |

### Ejemplos

[#ejemplos](#ejemplos)

feat: agregar endpoint /metrics para CPU
fix: corregir cálculo de porcentaje de RAM
docs: agregar guía de instalación en Windows
test: agregar pruebas unitarias para módulo de disco
chore: configurar GitHub Actions para CI

---

## Reglas importantes

[#reglas-importantes](#reglas-importantes)

- ❌ Nunca hagas push directo a `main` o `dev`
- ❌ Nunca trabajes directamente en `main` o `dev`
- ❌ Nunca abras un Pull Request (PR) contra la rama `main` (Todos van contra `dev`)
- ✅ Un issue = una rama = un PR
- ✅ Todo PR debe referenciar su issue con `Closes #N`
- ✅ El PR debe pasar el CI antes de ser mergeado
- ✅ Todo PR necesita al menos una revisión

### Convención de Paquetes Java

[#convención-de-paquetes-java](#convención-de-paquetes-java)

Los directorios de paquetes deben seguir estrictamente las convenciones canónicas de Java para el proyecto. Todo el código fuente nuevo o modificado debe organizarse bajo el paquete raíz: `edu.sisinf.estante`

Utilizando el subpaquete correcto según su propósito en el sistema:

- `edu.sisinf.estante.core`
- `edu.sisinf.estante.util`
- `edu.sisinf.estante.config`

---

## Agregar soporte para un nuevo motor de base de datos

[#agregar-soporte-para-un-nuevo-motor-de-base-de-datos](#agregar-soporte-para-un-nuevo-motor-de-base-de-datos)

Agregar soporte a un nuevo motor (por ejemplo Oracle o SQL Server) es una tarea que cualquier
contribuidor puede realizar siguiendo estos pasos en orden:

1. **Agregar la constante del motor**
   Agrega el nuevo motor como constante en `TipoMotor.java`.

2. **Agregar el driver al `pom.xml`**
   Incluye la dependencia del driver JDBC correspondiente al nuevo motor en `pom.xml`.

3. **Crear la clase de conexión**
   Crea `ConexionDAONuevoMotor.java` implementando la interfaz `IConexionDAO`.

4. **Registrar el motor en la factory**
   Registra la nueva clase de conexión en la factory correspondiente para que el sistema
   pueda instanciarla.

5. **Agregar el motor al controlador de UI**
   Agrega la nueva opción en `DialogoNuevaConexionController` para que el motor aparezca
   en la interfaz de creación de conexiones.

6. **Agregar tests**
   Escribe pruebas unitarias que cubran la nueva clase de conexión y su registro en la factory.

7. **Documentar en `docs/dao-guia.md`**
   Actualiza `docs/dao-guia.md` describiendo el nuevo motor agregado y cualquier
   particularidad de su implementación.

> ✅ Sigue siempre la convención de paquetes Java (`edu.sisinf.estante.*`) y la convención
> de commits/ramas descrita en este documento al implementar estos pasos.

---

## Ramas del proyecto

[#ramas-del-proyecto](#ramas-del-proyecto)

| Rama      | Propósito                                               |
| --------- | ------------------------------------------------------- |
| `main`    | Versión estable — solo recibe merges desde `dev`        |
| `dev`     | Rama de desarrollo principal (Destino de todos los PRs) |
| `feat/*`  | Nuevas funcionalidades                                  |
| `fix/*`   | Correcciones                                            |
| `docs/*`  | Documentación                                           |
| `test/*`  | Pruebas                                                 |
| `chore/*` | Configuración                                           |

---

## ¿No sabes por dónde empezar?

[#no-sabes-por-dónde-empezar](#no-sabes-por-dónde-empezar)

1. Revisa los issues abiertos con la etiqueta `good first issue`
2. Comenta en el issue que quieres trabajar
3. Espera confirmación antes de empezar
4. Sigue los pasos de este documento

---

## Configuración de Calidad de Código (Pre-commit)

[#configuración-de-calidad-de-código-pre-commit](#configuración-de-calidad-de-código-pre-commit)

Para mantener la consistencia del código y la estructura del proyecto, se utilizan hooks de `pre-commit`. Antes de realizar tu primer commit, es obligatorio instalar los hooks ejecutando el siguiente comando en la raíz del proyecto:

```
pre-commit install
```
## skip-issue-check

 Utilizar esta etiqueta en Pull Requests de mantenimiento ( actualización de dependencias, formateo de código) que no requieren un Issue asociado. Omite la verificación de CI que bloquea PRs sin referencia a Issues.
 
**¡Gracias por contribuir al proyecto!**