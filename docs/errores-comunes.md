# Errores Comunes en ChuspaDB

## Introducción

Este documento recopila errores frecuentes encontrados por los usuarios de ChuspaDB, junto con sus causas y soluciones.

## 1. Communications link failure

*Mensaje:*
Communications link failure

*Causa:* MySQL no está ejecutándose o no es accesible.

*Solución:* Verificar que el servicio MySQL esté iniciado y que el puerto configurado sea correcto.

---

## 2. JDBC Driver not found

*Mensaje:*
No suitable driver found

*Causa:* El driver JDBC no está incluido en el proyecto.

*Solución:* Agregar la dependencia JDBC en Maven.

---

## 3. JavaFX runtime components are missing

*Mensaje:*
JavaFX runtime components are missing

*Causa:* JavaFX no está instalado o configurado.

*Solución:* Verificar dependencias y configuración de JavaFX.

---

## 4. NullPointerException al ejecutar consulta

*Mensaje:*
NullPointerException

*Causa:* Objeto Connection o ResultSet nulo.

*Solución:* Verificar inicialización antes de usar objetos.

---

## 5. Conexión cerrada inesperadamente

*Mensaje:*
Connection is closed

*Causa:* La conexión fue cerrada antes de completar la operación.

*Solución:* Mantener abierta la conexión mientras se utilice.

---

## 6. Caracteres especiales incorrectos en CSV

*Mensaje:*
Caracteres extraños o símbolos incorrectos.

*Causa:* Problemas de codificación.

*Solución:* Exportar utilizando UTF-8.

---

## 7. OutOfMemoryError con ResultSet grande

*Mensaje:*
java.lang.OutOfMemoryError

*Causa:* Consulta devuelve demasiados registros.

*Solución:* Utilizar paginación o limitar resultados.

---

## 8. Access denied for user

*Mensaje:*
Access denied for user

*Causa:* Usuario o contraseña incorrectos.

*Solución:* Verificar credenciales de base de datos.

---

## 9. Unknown database

*Mensaje:*
Unknown database

*Causa:* La base de datos no existe.

*Solución:* Crear la base de datos o corregir el nombre.

---

## 10. Table doesn't exist

*Mensaje:*
Table doesn't exist

*Causa:* La tabla requerida no existe.

*Solución:* Ejecutar scripts de creación de tablas.

---

## 11. BindException Address already in use

*Mensaje:*
Address already in use

*Causa:* El puerto está siendo utilizado por otro proceso.

*Solución:* Liberar el puerto o usar otro.

---

## 12. Maven dependency resolution failed

*Mensaje:*
Could not resolve dependencies

*Causa:* Dependencias no descargadas correctamente.

*Solución:* Ejecutar nuevamente:

bash
mvn clean install


---

## Recomendaciones

* Mantener Java actualizado.
* Utilizar Java 17 o superior.
* Verificar configuración de Maven.
* Comprobar la conexión a MySQL antes de ejecutar la aplicación.
* Revisar logs completos para identificar errores.
*
