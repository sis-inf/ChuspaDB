# Accesibilidad en ChuspaDB

## Introducción

La accesibilidad permite que la aplicación pueda ser utilizada por personas con distintas capacidades y necesidades. Este documento resume las consideraciones actuales y futuras para mejorar la accesibilidad de ChuspaDB.

## Navegación por teclado

La aplicación debe permitir el uso completo mediante teclado.

### Recomendaciones

* Mantener un orden lógico de navegación con la tecla Tab.
* Permitir acceso a todos los controles sin utilizar el mouse.
* Evitar elementos inaccesibles desde el teclado.
* Verificar que los formularios sigan un orden natural de enfoque.

### Orden de navegación

Los paneles deben permitir recorrer:

1. Menú principal.
2. Formularios de entrada.
3. Tablas de datos.
4. Botones de acción.
5. Controles secundarios.

---

## Accesibilidad en JavaFX

JavaFX proporciona soporte para accesibilidad mediante atributos específicos.

### AccessibleAttribute

Permite exponer información útil para lectores de pantalla.

Ejemplo:

java
node.queryAccessibleAttribute(AccessibleAttribute.TEXT);


### Tooltips en botones

Todos los botones importantes deberían incluir Tooltips descriptivos.

Ejemplos:

* Guardar registro.
* Eliminar registro.
* Exportar CSV.
* Buscar libro.
* Actualizar datos.

Los Tooltips ayudan tanto a usuarios nuevos como a usuarios con dificultades visuales.

---

## Contraste de colores

El contraste adecuado facilita la lectura.

### Recomendaciones WCAG 2.1 AA

* Contraste mínimo de 4.5:1 para texto normal.
* Contraste mínimo de 3:1 para texto grande.
* Evitar combinaciones con poco contraste.
* No depender únicamente del color para transmitir información.

### Estado actual

El tema actual debe ser revisado periódicamente para verificar compatibilidad con WCAG 2.1 AA.

---

## Texto escalable

La aplicación debe respetar la configuración de tamaño de fuente del sistema operativo.

### Recomendaciones

* Evitar tamaños fijos demasiado pequeños.
* Permitir escalado de interfaz.
* Verificar visualización en pantallas de alta resolución.
* Mantener legibilidad al aumentar el tamaño de fuente.

---

## Mejoras planificadas

Las siguientes mejoras de accesibilidad pueden incorporarse en futuras versiones:

* Compatibilidad ampliada con lectores de pantalla.
* Atajos de teclado para acciones frecuentes.
* Temas de alto contraste.
* Ajuste dinámico del tamaño de fuente.
* Mejor soporte para navegación únicamente con teclado.
* Indicadores visuales de foco más visibles.
* Auditoría completa de accesibilidad.

---

## Recomendaciones generales

* Realizar pruebas con teclado.
* Validar contraste de colores.
* Incorporar Tooltips descriptivos.
* Considerar accesibilidad desde las primeras etapas de desarrollo.
* Revisar periódicamente el cumplimiento de WCAG.
*
