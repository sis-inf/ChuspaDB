# Preguntas Frecuentes — ChuspaDB

## ¿Qué es ChuspaDB?

ChuspaDB es un gestor de base de datos liviano desarrollado en Java que permite ejecutar consultas SQL sobre bases de datos MySQL de forma programática, sin depender de herramientas externas de administración.

## ¿Qué motor de base de datos usa ChuspaDB?

ChuspaDB utiliza **MySQL** como motor de base de datos. La conexión se configura mediante la clase `DBConfig`, que requiere host, puerto, nombre de base de datos, usuario y contraseña.

## ¿Cómo se instala ChuspaDB?

Para instalar y ejecutar ChuspaDB se necesita:

1. Tener instalado **Java 21** o superior.
2. Tener instalado **Maven 3.8** o superior.
3. Clonar el repositorio y ejecutar:

```bash
mvn install
```

4. Para correr los tests:

```bash
mvn test
```

## ¿Qué operaciones soporta ChuspaDB?

ChuspaDB soporta las siguientes operaciones SQL:

- **SELECT**: consultas de lectura que retornan un `QueryResult` con columnas y filas.
- **INSERT**: inserción de nuevos registros.
- **UPDATE**: actualización de registros existentes.
- **DELETE**: eliminación de registros.

La clase `SqlValidator` permite verificar el tipo de operación antes de ejecutarla.

## ¿Cómo se configura la conexión a la base de datos?

La conexión se configura usando la clase `DBConfig`:

```java
DBConfig config = new DBConfig(
    "localhost",  // host
    3306,         // puerto
    "mi_base",    // nombre de la BD
    "usuario",    // usuario
    "contraseña"  // contraseña
);
```

La URL JDBC se genera automáticamente con el método `toJdbcUrl()`.

## ¿Cómo se puede contribuir al proyecto?

Para contribuir a ChuspaDB se debe seguir el **Forking Workflow**:

1. Hacer un fork del repositorio original.
2. Crear una rama con el nombre del issue (por ejemplo `feat/nueva-funcionalidad`).
3. Realizar los cambios y hacer commit con mensajes descriptivos.
4. Abrir un Pull Request referenciando el issue con `Closes #N`.

Consultar el archivo `docs/flujo-de-trabajo.md` para más detalles.

## ¿Por qué usar JDBC directo y no Hibernate?

ChuspaDB usa JDBC directo de forma intencional. El proyecto está orientado a estudiantes de ingeniería, por lo que se prioriza que el acceso a la base de datos sea explícito y trazable. Hibernate y otros ORM agregan una capa de abstracción que oculta el SQL generado, lo que dificulta el aprendizaje y el debugging en un contexto educativo.

Además, el alcance del proyecto es acotado: las operaciones SQL que maneja ChuspaDB no justifican la configuración adicional que implica un ORM.

## ¿Cómo agrego soporte para un nuevo motor de base de datos?

1. Agrega el driver JDBC del nuevo motor como dependencia en el `pom.xml`. Por ejemplo, para PostgreSQL:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```

2. Actualiza la clase `DBConfig` para construir la URL JDBC correspondiente al nuevo motor en el método `toJdbcUrl()`.
3. Verifica que `SqlValidator` reconozca correctamente las sentencias del nuevo motor si tienen sintaxis particular.
4. Abre un issue describiendo el motor que deseas agregar antes de comenzar, para coordinar con el equipo.

## ¿Por qué `mvn javafx:run` falla con "JavaFX components not found"?

Este error ocurre generalmente por una de estas causas:

**La versión de Java no coincide.** El proyecto requiere Java 21. Verifica tu versión con:

```bash
java -version
```

Si la versión es inferior a 21, instala el JDK correcto y actualiza `JAVA_HOME`.

**Maven no está usando el JDK correcto.** Verifica con:

```bash
mvn -version
```

La línea `Java version` debe mostrar 21. Si no, revisa tu variable `JAVA_HOME`.

**El repositorio local de Maven está corrupto.** Fuerza la descarga de dependencias con:

```bash
mvn clean javafx:run -U
```

## ¿Cómo configuro `JAVA_HOME` en Windows?

1. Abre el menú Inicio y busca **"Variables de entorno"**.
2. Haz clic en **"Editar las variables de entorno del sistema"**.
3. En la sección **Variables del sistema**, haz clic en **Nueva** y agrega:
   - Nombre: `JAVA_HOME`
   - Valor: ruta de instalación del JDK, por ejemplo `C:\Program Files\Eclipse Adoptium\jdk-21`
4. Busca la variable `Path` en la misma sección, selecciónala y haz clic en **Editar**.
5. Agrega una nueva entrada: `%JAVA_HOME%\bin`
6. Acepta todos los diálogos y abre una nueva terminal para verificar:

```cmd
java -version
echo %JAVA_HOME%
```

## ¿Por qué los tests usan Mockito y no H2 (base de datos en memoria)?

Mockito permite aislar la lógica de negocio sin necesidad de levantar una base de datos real o en memoria. Esto hace que los tests sean más rápidos, predecibles y libres de dependencias externas.

H2 requiere que el esquema y los datos estén configurados correctamente antes de cada test, lo que agrega complejidad de mantenimiento. Con Mockito se simulan las respuestas de la capa de acceso a datos directamente, lo que es suficiente para verificar el comportamiento de los servicios y controladores sin involucrar SQL real.

## ¿Cómo depurar una `NullPointerException` en un controlador JavaFX?

Las `NullPointerException` en controladores JavaFX ocurren con frecuencia cuando un campo anotado con `@FXML` no fue inyectado correctamente. Sigue estos pasos:

1. **Verifica el `fx:id` en el FXML.** El valor del `fx:id` del componente debe coincidir exactamente con el nombre del campo en el controlador:

```xml
<Button fx:id="btnGuardar"/>
```

```java
@FXML
private Button btnGuardar;
```

2. **No accedas a campos `@FXML` en el constructor.** La inyección ocurre después de que el constructor se ejecuta. Usa el método `initialize()` para la lógica de inicialización:

```java
@FXML
public void initialize() {
    btnGuardar.setDisable(true); // correcto
}
```

3. **Revisa que el `fx:controller` del FXML apunte a la clase correcta.** Un controlador mal referenciado hace que ningún campo sea inyectado.

4. **Consulta el stack trace completo** para identificar en qué línea del controlador ocurre la excepción y qué campo es `null`.

## ¿Cómo ejecuto solo un test específico con Maven?

Para ejecutar un único test o clase de test sin correr toda la suite:

```bash
mvn test -Dtest=NombreDeLaClaseTest
```

Para ejecutar un método específico dentro de una clase:

```bash
mvn test -Dtest=NombreDeLaClaseTest#nombreDelMetodo
```

## ¿Qué hago si `mvn install` falla por un error de conexión al descargar dependencias?

Verifica tu conexión a internet y que Maven pueda acceder a Maven Central. Si estás detrás de un proxy, configúralo en el archivo `~/.m2/settings.xml`:

```xml
<proxies>
  <proxy>
    <id>mi-proxy</id>
    <active>true</active>
    <protocol>http</protocol>
    <host>proxy.ejemplo.com</host>
    <port>8080</port>
  </proxy>
</proxies>
```

Si el error persiste, limpia el caché local de Maven y reintenta:

```bash
mvn dependency:purge-local-repository
mvn install
```

## ¿Cómo verificar que Maven está correctamente instalado?

Ejecute:

```bash
mvn -version
```

Debería mostrarse información similar a:

```text
Apache Maven 3.x.x
Java version: 21
```

Si el comando no es reconocido, verifique que Maven esté instalado y agregado al PATH del sistema.
