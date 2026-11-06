# Componentes JavaFX Utilizados en ChuspaDB

## Introducción

Este documento describe los principales componentes JavaFX utilizados en ChuspaDB y proporciona ejemplos básicos para facilitar la incorporación de nuevos contribuidores al proyecto.

## Componentes Utilizados

| Componente | Ubicación principal | Uso en ChuspaDB |
|------------|------------|------------|
| TreeView | PanelArbolConexiones | Navegación jerárquica de conexiones, bases de datos y objetos. |
| TableView | PanelResultadoQuery, PanelInfoTabla | Visualización de resultados de consultas y metadatos de tablas. |
| TextArea | DialogoConfirmacionDML | Visualización de sentencias SQL antes de ejecutar operaciones DML. |
| CodeArea | PanelEditorSQL | Editor SQL con soporte de resaltado de sintaxis mediante RichTextFX. |
| SplitPane | VentanaPrincipal | División de la ventana principal en paneles redimensionables. |
| ListView | PanelHistorial | Historial de consultas ejecutadas durante la sesión. |
| ProgressIndicator | No utilizado actualmente | Puede emplearse para indicar operaciones de larga duración o cargas en progreso. |

---

## Cargar un archivo FXML

JavaFX utiliza archivos FXML para definir la estructura visual de las vistas.

Ejemplo básico:

```java
FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/fxml/PanelEditorSQL.fxml")
);

Parent root = loader.load();
```

Para obtener una referencia al controlador asociado:

```java
FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/fxml/PanelEditorSQL.fxml")
);

Parent root = loader.load();

PanelEditorSQLController controller =
    loader.getController();
```

---

## Pasar datos entre controladores

Una forma común de compartir información consiste en exponer métodos públicos en el controlador destino.

Controlador destino:

```java
public class PanelDetalleController {

    public void cargarDatos(String valor) {
        tituloLabel.setText(valor);
    }
}
```

Controlador origen:

```java
FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/fxml/PanelDetalle.fxml")
);

Parent root = loader.load();

PanelDetalleController controller =
    loader.getController();

controller.cargarDatos("Ejemplo");
```

---

## Actualizar la interfaz desde tareas en segundo plano

Las actualizaciones visuales deben ejecutarse siempre en el hilo principal de JavaFX.

En ChuspaDB, la clase `EjecutorQueryAsync` utiliza `Platform.runLater()` para garantizar que las modificaciones de la interfaz se realicen de forma segura.

Ejemplo utilizado en el proyecto:

```java
Platform.runLater(() -> onSuccess.accept(result));
Platform.runLater(() -> onError.accept(error));
```

### ¿Por qué usar Platform.runLater()?

Cuando una operación se ejecuta en un hilo distinto al de la interfaz gráfica, JavaFX no permite modificar directamente los componentes visuales.

`Platform.runLater()` programa la ejecución del código en el hilo de JavaFX y evita errores relacionados con concurrencia y acceso a la UI.

Ejemplo genérico:

```java
new Thread(() -> {

    String resultado = realizarOperacion();

    Platform.runLater(() -> {
        resultadoLabel.setText(resultado);
    });

}).start();
```

---

## Agregar un nuevo panel

### 1. Crear el archivo FXML

Ubicar el archivo dentro de los recursos del proyecto:

```text
src/main/resources/fxml/NuevoPanel.fxml
```

### 2. Crear el controlador

```java
public class NuevoPanelController {

    @FXML
    private Label tituloLabel;

}
```

### 3. Asociar el controlador al FXML

```xml
<AnchorPane
    fx:controller="edu.sisinf.chuspadb.controller.NuevoPanelController">
</AnchorPane>
```

### 4. Cargar el panel desde Java

```java
FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/fxml/NuevoPanel.fxml")
);

Parent panel = loader.load();
```

### 5. Integrar el panel en la interfaz

El panel puede agregarse a un contenedor existente o incorporarse a la estructura principal de la aplicación según las necesidades del proyecto.

---

## Componentes identificados en el proyecto

Los siguientes archivos muestran el uso actual de los componentes documentados:

| Archivo | Componente |
|----------|----------|
| PanelArbolConexionesController | TreeView |
| PanelEditorSQLController | CodeArea |
| PanelHistorialController | ListView |
| PanelInfoTablaController | TableView |
| PanelResultadoQueryController | TableView |
| DialogoConfirmacionDML | TextArea |
| VentanaPrincipal.fxml | SplitPane |
| EjecutorQueryAsync | Platform.runLater |

---

## Recomendaciones

- Mantener separada la lógica de negocio de los controladores JavaFX.
- Utilizar FXML para definir la interfaz gráfica.
- Actualizar la UI únicamente desde el hilo principal de JavaFX.
- Emplear `Platform.runLater()` cuando una operación se ejecute en segundo plano.
- Mantener los controladores pequeños y enfocados en una única responsabilidad.
- Reutilizar componentes y vistas siempre que sea posible.