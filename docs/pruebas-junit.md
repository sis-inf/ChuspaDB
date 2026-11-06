# Guía para escribir pruebas JUnit 5 con Mockito

## Introducción

ChuspaDB utiliza JUnit 5 para la creación de pruebas unitarias y Mockito para la simulación de dependencias durante la ejecución de pruebas. Estas herramientas permiten validar el comportamiento de las clases de manera aislada y automatizada.

Actualmente el proyecto contiene pruebas como `SqlValidatorTest`, `EjecutorQueryTest`, `DBConfigTest` y otras pruebas unitarias distribuidas en distintos paquetes.

---

# JUnit 5 básico

JUnit 5 es el framework principal utilizado para ejecutar pruebas automáticas.

## Uso de @Test

La anotación `@Test` indica que un método corresponde a una prueba.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraTest {

    @Test
    void debeSumarDosNumeros() {
        int resultado = 2 + 3;

        assertEquals(5, resultado);
    }
}
```

---

## Uso de @BeforeEach

La anotación `@BeforeEach` permite ejecutar código antes de cada prueba.

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsuarioTest {

    private String nombre;

    @BeforeEach
    void setUp() {
        nombre = "Ana";
    }

    @Test
    void nombreNoDebeSerNulo() {
        assertNotNull(nombre);
    }
}
```

---

## assertEquals

Verifica que dos valores sean iguales.

```java
assertEquals(10, resultado);
```

---

## assertNotNull

Verifica que un objeto no sea nulo.

```java
assertNotNull(conexion);
```

---

## assertThrows

Permite verificar que una operación genere una excepción.

```java
assertThrows(
        IllegalArgumentException.class,
        () -> {
            throw new IllegalArgumentException();
        }
);
```

---

# Mockito básico

Mockito permite crear objetos simulados (mocks) para evitar dependencias externas durante las pruebas.

## Uso de @Mock

La anotación `@Mock` crea un objeto simulado.

```java
@Mock
private Connection connection;
```

---

## Uso de @InjectMocks

La anotación `@InjectMocks` inyecta automáticamente los mocks en la clase bajo prueba.

```java
@InjectMocks
private EjecutorQuery ejecutorQuery;
```

---

## Uso de when().thenReturn()

Permite definir el comportamiento esperado de un mock.

```java
when(connection.isClosed())
        .thenReturn(false);
```

Cuando se invoque el método `isClosed()`, Mockito devolverá el valor configurado.

---

# Mockear JDBC

En pruebas relacionadas con bases de datos es común simular objetos JDBC para evitar conexiones reales.

Los objetos más utilizados son:

* Connection
* PreparedStatement
* Statement
* ResultSet

Ejemplo:

```java
Connection connection = mock(Connection.class);
PreparedStatement statement = mock(PreparedStatement.class);
ResultSet resultSet = mock(ResultSet.class);

when(connection.prepareStatement(anyString()))
        .thenReturn(statement);

when(statement.executeQuery())
        .thenReturn(resultSet);
```

Simular registros devueltos:

```java
when(resultSet.next())
        .thenReturn(true)
        .thenReturn(false);

when(resultSet.getString("nombre"))
        .thenReturn("Ana");
```

Este enfoque permite probar la lógica sin depender de una base de datos real.

---

# Ejemplo completo

Ejemplo simplificado de una prueba para `EjecutorQuery`.

```java
@Test
void debeEjecutarConsultaSelect() throws Exception {

    Connection connection = mock(Connection.class);
    Statement statement = mock(Statement.class);
    ResultSet resultSet = mock(ResultSet.class);

    when(connection.createStatement())
            .thenReturn(statement);

    when(statement.executeQuery("SELECT 1"))
            .thenReturn(resultSet);

    when(resultSet.next())
            .thenReturn(false);

    EjecutorQuery ejecutor = new EjecutorQuery();

    ResultadoQuery resultado =
            ejecutor.ejecutar(connection, "SELECT 1");

    assertNotNull(resultado);
}
```

El objetivo de esta prueba es verificar que la consulta se ejecute correctamente y genere un resultado válido sin necesidad de conectarse a una base de datos real.

---

# Ejecutar tests

## Ejecutar todas las pruebas

```bash
mvn test
```

---

## Ejecutar una clase específica

```bash
mvn -Dtest=EjecutorQueryTest test
```

---

## Ejecutar un método específico

```bash
mvn -Dtest=EjecutorQueryTest#debeEjecutarConsultaSelect test
```

---

# Buenas prácticas

* Mantener pruebas pequeñas y fáciles de entender.
* Utilizar nombres descriptivos para los métodos de prueba.
* Evitar dependencias externas cuando sea posible.
* Utilizar mocks para aislar comportamientos.
* Ejecutar la suite completa antes de abrir un Pull Request.
* Mantener una única responsabilidad por prueba.

Siguiendo estas recomendaciones es posible mantener una base de pruebas consistente y fácil de mantener dentro del proyecto ChuspaDB.
