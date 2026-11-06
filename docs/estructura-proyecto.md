# Estructura del Proyecto

## Introducción

El proyecto sigue una estructura basada en Maven para aplicaciones JavaFX. Esta organización separa el código fuente, los recursos, las pruebas y la documentación, facilitando el mantenimiento y la escalabilidad del sistema.

## Árbol de Directorios

```text
.
├── .github/
│   ├── ISSUE_TEMPLATE/
│   └── workflows/
├── data/
│   ├── analysis/
│   ├── processed/
│   └── raw/
├── docs/
├── examples/
├── security/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edu/sisinf/chuspadb/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── core/
│   │   │       ├── dao/
│   │   │       ├── dto/
│   │   │       ├── modelo/
│   │   │       ├── servicio/
│   │   │       ├── util/
│   │   │       └── vista/
│   │   └── resources/
│   │       ├── css/
│   │       └── fxml/
│   └── test/
│       └── java/
│           └── edu/sisinf/chuspadb/
├── tests/
│   ├── automatizados/
│   ├── casos/
│   ├── datos/
│   ├── manuales/
│   ├── plan/
│   └── reportes/
└── pom.xml
```

## Descripción de Directorios

### src/main/java

Contiene el código fuente principal de la aplicación.

#### config/

Clases de configuración general de la aplicación, conexiones y parámetros de ejecución.

#### controller/

Controladores JavaFX encargados de gestionar la interacción entre la interfaz gráfica y la lógica de negocio.

#### core/

Componentes centrales del sistema, manejo de errores, excepciones y funcionalidades compartidas.

#### dao/

Implementaciones de acceso a datos (Data Access Object). Gestionan la comunicación con la base de datos.

#### dto/

Objetos de transferencia de datos utilizados para intercambiar información entre capas.

#### modelo/

Entidades del dominio de la aplicación que representan los datos del sistema.

#### servicio/

Lógica de negocio de la aplicación. Coordina operaciones entre controladores, modelos y acceso a datos.

#### util/

Clases utilitarias y funciones de apoyo reutilizables en distintas partes del proyecto.

#### vista/

Elementos relacionados con la presentación e interfaz de usuario.

---

### src/main/resources

Contiene recursos utilizados por la aplicación.

#### fxml/

Archivos FXML que definen las vistas JavaFX.

#### css/

Hojas de estilo utilizadas para personalizar la apariencia de la interfaz.

#### imágenes y otros recursos

Recursos estáticos utilizados por la aplicación.

---

### src/test/java

Contiene las pruebas automatizadas desarrolladas con JUnit para validar el comportamiento del sistema.

---

### tests

Documentación y recursos de apoyo para pruebas.

#### automatizados/

Pruebas automáticas organizadas por tipo.

- unit/: pruebas unitarias.
- integration/: pruebas de integración.
- e2e/: pruebas de extremo a extremo.

#### casos/

Casos de prueba funcionales, de regresión y no funcionales.

#### datos/

Datos de entrada y resultados esperados para las pruebas.

#### manuales/

Checklists y evidencias de pruebas manuales.

#### plan/

Planificación, estrategia y criterios de salida de las pruebas.

#### reportes/

Plantillas y reportes de ejecución de pruebas.

---

### docs

Documentación técnica y funcional del proyecto.

---

### data

Archivos de datos utilizados para análisis, procesamiento o almacenamiento temporal.

---

### security

Documentación y recursos relacionados con la seguridad del proyecto.

---

### .github

Configuración de GitHub Actions, plantillas de issues y plantillas de Pull Request.

## Archivo pom.xml

Archivo principal de Maven. Define dependencias, plugins y configuración de construcción del proyecto.