# Casos de Falla

Comportamiento de ChuspaDB ante condiciones de error y como recuperarse.

## 1. Perdida de conexion durante una consulta larga

### Que ocurre

Si la conexion a la base de datos se pierde mientras se ejecuta una consulta larga, ChuspaDB lanza ErrorConexion y cancela la operacion. La consulta no se completa y los datos no se modifican si era una operacion de solo lectura. Para operaciones de escritura, el resultado depende de si la base de datos usa transacciones.

### Comportamiento esperado

- La interfaz muestra un mensaje de error indicando que la conexion fue interrumpida
- Los resultados parciales no se muestran
- La conexion queda marcada como inactiva en el panel de conexiones

### Como recuperarse

1. Verificar que el servidor de base de datos siga activo
2. En el panel de conexiones, hacer clic derecho en la conexion afectada
3. Seleccionar Reconectar
4. Volver a ejecutar la consulta

## 2. Servidor inalcanzable al conectar

### Que ocurre

Si el servidor no responde cuando se intenta establecer una conexion nueva, ChuspaDB lanza ErrorConexion con el mensaje Communications link failure. La aplicacion no se bloquea y el usuario puede intentar reconectarse.

### Comportamiento esperado

- Se muestra el dialogo de error con el mensaje del driver JDBC
- La conexion no se agrega a la lista de conexiones activas
- Las demas conexiones activas no se ven afectadas

### Como recuperarse

1. Verificar que MySQL o el motor de base de datos este ejecutandose:

    sudo systemctl status mysql

2. Verificar que el puerto sea correcto (por defecto 3306 para MySQL)
3. Verificar que el firewall no bloquee la conexion
4. Volver a intentar la conexion desde el dialogo de nueva conexion

## 3. Disco lleno durante una exportacion grande

### Que ocurre

Si el disco se llena mientras ChuspaDB exporta datos, la operacion se interrumpe y el archivo exportado queda incompleto. ChuspaDB lanza ErrorPersistencia y muestra un mensaje de error.

### Comportamiento esperado

- La exportacion se detiene inmediatamente
- Se muestra un mensaje indicando que no hay espacio en disco
- El archivo parcial puede quedar en el directorio de destino

### Como recuperarse

1. Liberar espacio en disco eliminando archivos innecesarios:

    df -h

2. Eliminar el archivo de exportacion incompleto
3. Elegir un directorio de destino con suficiente espacio
4. Volver a ejecutar la exportacion

## 4. Archivo de conexiones corrupto

### Que ocurre

Si el archivo donde ChuspaDB guarda las conexiones guardadas se corrompe (por ejemplo, por un cierre forzado), la aplicacion puede no cargar las conexiones al iniciar o mostrar un error de deserializacion.

### Comportamiento esperado

- Al iniciar, ChuspaDB muestra un mensaje indicando que no pudo cargar las conexiones guardadas
- La aplicacion inicia normalmente sin conexiones previas
- No se pierden datos de las bases de datos, solo la configuracion de conexiones

### Como recuperarse

1. Cerrar ChuspaDB
2. Localizar el archivo de conexiones (generalmente en el directorio de configuracion del usuario)
3. Eliminarlo o renombrarlo como respaldo
4. Reiniciar ChuspaDB — iniciara sin conexiones guardadas
5. Volver a configurar las conexiones manualmente
