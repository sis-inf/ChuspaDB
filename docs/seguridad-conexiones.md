# Seguridad en el Manejo de Conexiones

## Introducción

ChuspaDB permite almacenar configuraciones de conexión a bases de datos para facilitar el acceso a distintos motores soportados por la aplicación. Estas configuraciones incluyen información como el host, puerto, base de datos, usuario y contraseña.

Debido a que las credenciales de acceso son información sensible, es importante comprender cómo se almacenan actualmente, cuáles son los riesgos asociados y qué medidas pueden tomarse para reducir posibles problemas de seguridad.

Este documento describe el estado actual del almacenamiento de conexiones en ChuspaDB y presenta recomendaciones para mejorar la protección de las credenciales.

---

## Estado actual

Actualmente ChuspaDB almacena las configuraciones de conexión en un archivo JSON ubicado dentro del directorio de configuración del usuario.

Ubicación habitual:

**Linux / macOS**

```text
~/.ChuspaDB/conexiones.json
```

**Windows**

```text
C:\Users\<usuario>\.ChuspaDB\conexiones.json
```

Las conexiones son gestionadas mediante el repositorio `RepositorioConexionesJSON` y utilizan el modelo `Conexion`.

La clase `Conexion` contiene, entre otros, los siguientes atributos:

```java
private String host;
private Integer puerto;
private String basedatos;
private String usuario;
private String password;
```
### Advertencia importante

Actualmente no existe evidencia en el código fuente de que las contraseñas almacenadas en `conexiones.json` sean cifradas antes de guardarse. Esto significa que la contraseña forma parte de la información persistida de cada conexión.

Por lo tanto, las credenciales deben considerarse almacenadas en texto plano o en un formato fácilmente recuperable por cualquier usuario que tenga acceso al archivo.

---

## Riesgos

Guardar credenciales en un archivo local presenta diversos riesgos de seguridad.

### Acceso por otros usuarios del sistema

Si varios usuarios utilizan el mismo equipo y tienen permisos para acceder al directorio personal donde se encuentra el archivo, podrían visualizar las credenciales almacenadas.

### Copias de respaldo

Las copias de seguridad automáticas del sistema operativo pueden incluir el archivo `conexiones.json`, exponiendo las credenciales en otros dispositivos o servicios de almacenamiento.

### Compartición accidental

Al compartir configuraciones, respaldos o directorios completos del usuario, existe el riesgo de divulgar información sensible de acceso a bases de datos.

### Exposición de credenciales privilegiadas

Si se utilizan cuentas administrativas de base de datos, el impacto de una filtración puede ser considerablemente mayor.

---

## Mitigaciones recomendadas

Aunque el almacenamiento actual presenta limitaciones, existen medidas que ayudan a reducir los riesgos.

### Restringir permisos del archivo

En sistemas Linux y macOS se recomienda permitir acceso únicamente al propietario del archivo:

```bash
chmod 600 ~/.ChuspaDB/conexiones.json
```

Con esta configuración solamente el usuario propietario podrá leer o modificar el archivo.

### Limitar permisos en Windows

En Windows se recomienda utilizar los permisos NTFS para restringir el acceso únicamente al usuario propietario de la cuenta.

### Utilizar usuarios con privilegios mínimos

Siempre que sea posible, las conexiones deben utilizar usuarios de base de datos con los permisos estrictamente necesarios para realizar sus tareas.

### Proteger respaldos

Los respaldos que contengan configuraciones de ChuspaDB deben almacenarse en ubicaciones seguras y con acceso restringido.

### Rotar credenciales periódicamente

Es recomendable cambiar contraseñas de forma periódica, especialmente en entornos compartidos o de producción.

---

## Conexiones SSL

La clase `Conexion` incluye una configuración denominada `usarSSL`, lo que permite indicar si una conexión debe utilizar SSL/TLS cuando el motor de base de datos lo soporte.

Las conexiones cifradas ayudan a proteger la información transmitida entre el cliente y el servidor frente a ataques de interceptación de tráfico.

Por ejemplo, en MySQL es habitual habilitar SSL mediante parámetros de conexión equivalentes a:

```text
useSSL=true
```

Cuando el servidor esté configurado correctamente, se recomienda utilizar conexiones seguras para proteger credenciales y datos transmitidos por la red.

Es importante recordar que SSL protege la comunicación entre el cliente y el servidor, pero no cifra automáticamente las credenciales almacenadas localmente en `conexiones.json`.

---

## Lo que NO se debe hacer

Para reducir riesgos de seguridad se recomienda evitar las siguientes prácticas:

### No hardcodear credenciales

No se deben escribir usuarios o contraseñas directamente dentro del código fuente.

Ejemplo no recomendado:

```java
String usuario = "root";
String password = "123456";
```

### No subir credenciales a repositorios

El archivo `conexiones.json` nunca debe ser incluido en commits o repositorios públicos.

### No compartir archivos de configuración

Antes de compartir configuraciones o respaldos debe verificarse que no contengan credenciales activas.

### No utilizar cuentas administrativas innecesarias

Las conexiones cotidianas no deberían utilizar usuarios con privilegios de administrador cuando existen alternativas más limitadas.

---

## Mejoras futuras

Una posible mejora para futuras versiones de ChuspaDB consiste en implementar mecanismos de protección de credenciales antes de almacenarlas.

Entre las alternativas posibles se encuentran:

* Cifrado de contraseñas antes de persistirlas.
* Integración con Java Keystore.
* Uso de almacenes seguros del sistema operativo.
* Protección mediante claves maestras definidas por el usuario.

Estas mejoras permitirían reducir significativamente el riesgo asociado al almacenamiento local de credenciales.

---

## Conclusión

ChuspaDB facilita la administración de conexiones a bases de datos mediante el almacenamiento de configuraciones persistentes. Sin embargo, las credenciales deben considerarse información sensible y protegerse adecuadamente.

Mientras no exista un mecanismo de cifrado integrado, es responsabilidad del usuario aplicar buenas prácticas de seguridad, restringir permisos de acceso, proteger respaldos y utilizar conexiones seguras cuando el motor de base de datos lo permita.

La aplicación ya ofrece soporte para configuraciones SSL y cuenta con una estructura que podría evolucionar hacia mecanismos más robustos de protección de credenciales en futuras versiones.

### Configuración Local de Base de Datos
Para el desarrollo local, se debe copiar el archivo de plantilla `src/main/resources/database.properties.example`, renombrarlo a `src/main/resources/database.properties` y configurar las credenciales correspondientes. Este archivo real (`.properties`) está excluido del control de versiones por seguridad.
