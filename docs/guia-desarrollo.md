# Guía de Desarrollo — ChuspaDB

Esta guía describe las herramientas y flujos de trabajo recomendados para contribuir al desarrollo de ChuspaDB.

---

## Editar FXMLs con Scene Builder

Los archivos FXML del proyecto definen las interfaces gráficas de la aplicación y se ubican en `src/main/resources/edu/sisinf/ChuspaDB/`. Aunque pueden editarse a mano, se recomienda usar **Scene Builder** para evitar errores de estructura o atributos mal formados.

### Instalación de Scene Builder

1. Descarga Scene Builder desde el sitio oficial de Gluon:
   [https://gluonhq.com/products/scene-builder/](https://gluonhq.com/products/scene-builder/)
2. Selecciona el instalador correspondiente a tu sistema operativo (Windows, macOS o Linux).
3. Sigue el asistente de instalación. No requiere configuración adicional.

> Scene Builder es compatible con los archivos FXML generados por JavaFX 21, que es la versión que usa este proyecto.

### Abrir un FXML del proyecto

1. Abre Scene Builder.
2. Ve a **File → Open** y navega hasta la carpeta:
   ```
   src/main/resources/edu/sisinf/ChuspaDB/
   ```
3. Selecciona el archivo `.fxml` que deseas editar y haz clic en **Open**.

### Agregar un componente nuevo

1. En el panel **Library** (lado izquierdo), busca el componente que necesitas (por ejemplo, `Button`, `Label`, `TextField`).
2. Arrástralo al área de diseño o al panel **Document** para ubicarlo dentro del contenedor correcto.
3. En el panel **Inspector** (lado derecho), configura sus propiedades: texto, tamaño, estilo, etc.
4. Guarda los cambios con **File → Save** (`Ctrl+S` / `Cmd+S`).

### Conectar un componente al controlador con `fx:id`

Para que el controlador Java pueda referenciar un componente del FXML, debes asignarle un `fx:id`:

1. Selecciona el componente en el área de diseño.
2. En el panel **Inspector → Code**, localiza el campo **fx:id**.
3. Escribe el nombre del campo tal como está declarado en la clase controladora (por ejemplo, `btnGuardar`, `lblError`).
4. Verifica que el campo exista en el controlador con la anotación `@FXML`:
   ```java
   @FXML
   private Button btnGuardar;
   ```

> Si el `fx:id` no coincide exactamente con el nombre del campo en el controlador, la aplicación lanzará un error al cargar la vista.

### ⚠️ Qué NO cambiar: el atributo `fx:controller`

Cada archivo FXML tiene un atributo `fx:controller` en el elemento raíz que vincula la vista con su clase controladora:

```xml
<AnchorPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="edu.sisinf.ChuspaDB.controller.MiController">
```

**No modifiques este atributo.** Cambiarlo rompe el vínculo entre la vista y su controlador, lo que genera errores en tiempo de ejecución que pueden ser difíciles de rastrear. Si necesitas cambiar el controlador asociado a un FXML, consulta primero con el equipo en el issue correspondiente.

---

## Convención de paquetes

Todo el código fuente sigue la convención de paquetes del proyecto bajo la raíz `edu.sisinf.ChuspaDB`. Consulta `CONTRIBUTING.md` para más detalles.

---

## Documentación adicional

- Ver `CONTRIBUTING.md` para el flujo de trabajo con ramas y PRs.
- Ver `docs/instalacion.md` para configurar el entorno local.
