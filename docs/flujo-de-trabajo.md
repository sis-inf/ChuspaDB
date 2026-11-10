# Flujo de Trabajo con Git - Forking Workflow para Estante

Este documento describe paso a paso el Forking Workflow utilizado en el proyecto **Estante**.

---

## 1. Cómo hacer fork con captura de pantalla

El fork es una copia del repositorio original en tu cuenta personal de GitHub.

### Pasos:

1. Ve al repositorio original en GitHub: `https://github.com/sis-inf/estante`
2. Haz clic en el botón **Fork** (esquina superior derecha).

   ![Fork button en GitHub](./img/fork-button.png)
   *Captura: Botón Fork en la página principal del repositorio*

3. Selecciona tu cuenta personal como destino del fork.
4. Espera a que GitHub termine de crear la copia. Serás redirigido automáticamente a `https://github.com/TU-USUARIO/estante`.

   ![Fork creado exitosamente](./img/fork-creado.png)
   *Captura: Repositorio forkeado en tu cuenta personal*

5. Clona tu fork en tu máquina local:

```bash
git clone https://github.com/TU-USUARIO/estante.git
cd estante
```

6. Agrega el repositorio original como remote `upstream`:

```bash
git remote add upstream https://github.com/sis-inf/estante.git
git remote -v  # Verifica que aparezcan origin y upstream
```

---
### Configuración inicial

Instala los hooks de pre-commit antes de comenzar a trabajar:

```bash
pre-commit install


---

### 2. Agregar verificaciones antes del PR

En la sección **"Cómo hacer commit y push"**, después de `git status` o antes del commit, agrega:

```md
### Verificaciones obligatorias antes de abrir un PR

Ejecuta las siguientes verificaciones:

```bash
mvn checkstyle:check
mvn test


---

### 3. Actualizar el flujo de ramas

En la sección **"Resumen del flujo completo"**, reemplaza:

```text
Fork del repo → Clonar localmente → Crear rama → Hacer cambios
→ git add → git commit → git push → Abrir Pull Request

## 2. Cómo crear una rama con captura de pantalla

Siempre trabaja en una rama específica, nunca directamente en `main`.

### Pasos:

1. Asegúrate de tener `main` actualizado antes de crear la rama:

```bash
git checkout main
git pull upstream main
```

2. Crea la rama con el nombre indicado en el issue. Para este issue, la rama es:

```bash
git checkout -b docs/flujo-de-trabajo
```

   ![Creación de rama en terminal](./img/crear-rama.png)
   *Captura: Comando para crear y moverse a la nueva rama*

3. Verifica que estás en la rama correcta:

```bash
git branch
# Debería mostrar * docs/flujo-de-trabajo
```

   ![Verificación de rama activa](./img/verificar-rama.png)
   *Captura: Confirmación de la rama activa con `git branch`*

---

## 3. Cómo hacer commit y push con captura de pantalla

Una vez realizados los cambios, guárdalos con un commit descriptivo.

### Pasos:

1. Verifica qué archivos cambiaste:

```bash
git status
```

2. Agrega los archivos al área de staging:

```bash
git add docs/flujo-de-trabajo.md
# O para agregar todos los cambios:
git add .
```

3. Realiza el commit con un mensaje descriptivo siguiendo la convención del proyecto:

```bash
git commit -m "docs: agregar guía del flujo de trabajo con Git"
```

   ![Commit realizado exitosamente](./img/commit.png)
   *Captura: Resultado del comando `git commit`*

4. Sube los cambios a tu fork en GitHub:

```bash
git push origin docs/flujo-de-trabajo
```

   ![Push exitoso](./img/push.png)
   *Captura: Resultado del comando `git push`*

---

## 4. Cómo abrir un Pull Request con captura de pantalla

Un Pull Request (PR) permite proponer tus cambios al repositorio original.

### Pasos:

1. Ve a tu fork en GitHub: `https://github.com/TU-USUARIO/estante`
2. GitHub mostrará un banner amarillo sugiriendo crear un PR desde tu rama recién subida. Haz clic en **"Compare & pull request"**.

   ![Banner de PR en GitHub](./img/pr-banner.png)
   *Captura: Notificación de GitHub para crear un Pull Request*

3. Completa el formulario del PR:
   - **Título:** `docs: agregar guía del flujo de trabajo con Git`
   - **Descripción:** Explica brevemente qué incluye el PR y referencia el issue con `Closes #9`
   - **Base:** `dev` del repositorio `sis-inf/estante`
   - **Compare:** `docs/flujo-de-trabajo` de tu fork

   ![Formulario del Pull Request](./img/pr-formulario.png)
   *Captura: Formulario de creación del Pull Request con los campos completados*

4. Haz clic en **"Create pull request"**.

   ![PR creado exitosamente](./img/pr-creado.png)
   *Captura: Pull Request abierto y visible en el repositorio original*

---

## 5. Errores comunes y cómo resolverlos

### Error 1: `rejected - non-fast-forward`

**Descripción:** Ocurre cuando tu rama local está desactualizada respecto al repositorio remoto.

```
! [rejected] docs/flujo-de-trabajo -> docs/flujo-de-trabajo (non-fast-forward)
error: failed to push some refs to 'https://github.com/TU-USUARIO/estante.git'
```

**Solución:**

```bash
# Actualiza tu rama con los cambios del upstream
git fetch upstream
git rebase upstream/main

# Luego vuelve a hacer push
git push origin docs/flujo-de-trabajo
```

---

### Error 2: `CONFLICT (content): Merge conflict in ...`

**Descripción:** Ocurre cuando dos personas modificaron las mismas líneas de un archivo.

```
Auto-merging archivo.txt
CONFLICT (content): Merge conflict in archivo.txt
Automatic merge failed; fix conflicts and then commit the result.
```

**Solución:**

1. Abre el archivo con conflicto. Verás marcas como estas:

```
<<<<<<< HEAD
Tu versión del código
=======
Versión del upstream
>>>>>>> upstream/main
```

2. Edita el archivo manualmente y elige qué versión conservar (o combina ambas).
3. Elimina las marcas de conflicto (`<<<<<<<`, `=======`, `>>>>>>>`).
4. Agrega y commitea los cambios resueltos:

```bash
git add archivo.txt
git commit -m "fix: resolver conflicto de merge en archivo.txt"
```

---

```
## Resumen del flujo completo

Fork
  ↓
Crear rama feature
  ↓
Realizar cambios
  ↓
git add
  ↓
git commit
  ↓
git push
  ↓
Pull Request hacia dev
  ↓
Revisión e integración en dev
  ↓
Merge hacia main
```

| Paso | Comando / Acción |
|------|-----------------|
| Clonar fork | `git clone https://github.com/TU-USUARIO/estante.git` |
| Agregar upstream | `git remote add upstream https://github.com/sis-inf/estante.git` |
| Actualizar main | `git pull upstream main` |
| Crear rama | `git checkout -b nombre-de-la-rama` |
| Ver cambios | `git status` |
| Agregar cambios | `git add .` |
| Commit | `git commit -m "tipo: descripción"` |
| Push | `git push origin nombre-de-la-rama` |
| Pull Request | Desde GitHub web |
| Instalar hooks | `pre-commit install` |
| Checkstyle | `mvn checkstyle:check` |
| Ejecutar pruebas | `mvn test` |
