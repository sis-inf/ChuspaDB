# Estilo de Commits para ChuspaDB

## Introducción

El proyecto *ChuspaDB* utiliza la convención *Conventional Commits* para mantener un historial de cambios claro, consistente y fácil de revisar.

## Formato

Todos los commits deben seguir el siguiente formato:

text
tipo(ambito): descripción breve


### Ejemplos

text
feat(dao): agregar búsqueda de libros por autor
fix(servicio): corregir validación de préstamos
docs(docs): actualizar guía de instalación


---

## Tipos permitidos

| Tipo     | Descripción                                        |
| -------- | -------------------------------------------------- |
| feat     | Nueva funcionalidad                                |
| fix      | Corrección de errores                              |
| docs     | Cambios en documentación                           |
| test     | Agregar o modificar pruebas                        |
| refactor | Reestructuración interna sin cambiar funcionalidad |
| chore    | Tareas de mantenimiento                            |
| ci       | Cambios en integración continua                    |

---

## Ámbitos recomendados para ChuspaDB

| Ámbito     | Uso                   |
| ---------- | --------------------- |
| dao        | Acceso a datos        |
| modelo     | Entidades y modelos   |
| servicio   | Lógica de negocio     |
| controller | Controladores JavaFX  |
| fxml       | Archivos FXML         |
| ui         | Interfaz gráfica      |
| pom        | Configuración Maven   |
| ci         | Integración continua  |
| tests      | Pruebas automatizadas |
| docs       | Documentación         |

---

## Ejemplos correctos

1. feat(dao): implementar consulta de usuarios
2. feat(modelo): agregar entidad Prestamo
3. fix(servicio): corregir cálculo de multas
4. fix(controller): validar campos vacíos
5. chore(pom): actualizar dependencias Maven
6. docs(docs): agregar manual de usuario
7. test(servicio): crear pruebas para préstamos
8. ci(ci): actualizar workflow de GitHub Actions

---

## Ejemplos incorrectos y corrección

### Incorrecto

text
arreglos


### Correcto

text
fix(servicio): corregir validación de usuarios


### Incorrecto

text
nuevo codigo


### Correcto

text
feat(modelo): agregar entidad Libro


### Incorrecto

text
documentacion


### Correcto

text
docs(docs): actualizar README


### Incorrecto

text
cambios varios


### Correcto

text
refactor(controller): simplificar navegación


### Incorrecto

text
test


### Correcto

text
test(servicio): agregar pruebas de devolución


---

## Recomendaciones

* Utilizar siempre Conventional Commits.
* Seleccionar el ámbito adecuado para cada cambio.
* Mantener descripciones breves y claras.
* Evitar mensajes genéricos como "cambios", "arreglos" o "actualización".
* Escribir la descripción en infinitivo.
*
