# Limitaciones Técnicas del Sistema

Este documento describe las restricciones operativas, de diseño y arquitectónicas actuales de la aplicación, abarcando tanto el comportamiento de la interfaz gráfica (UI) como las capacidades de los motores de bases de datos soportados.

## 1. Limitaciones de la Interfaz Gráfica (JavaFX)

* **Capacidad del ResultSet:** La UI de JavaFX está optimizada para manejar un tamaño máximo recomendado de **10,000 filas** por consulta en las tablas de visualización. Superar este límite puede comprometer el rendimiento de la interfaz y la fluidez de la aplicación.
* **Ciclo de Vida del Historial:** El historial de consultas realizadas es volátil y **se pierde por completo al cerrar la aplicación**, debido a que actualmente se almacena en la memoria del entorno de ejecución.
* **Interfaz Visual:** La UI no cuenta con soporte para temas oscuros (*dark mode*), estando restringida únicamente a la paleta de colores del tema claro predeterminado.

## 2. Limitaciones de los Motores de Base de Datos y Persistencia

* **Procedimientos Almacenados:** El sistema no ofrece soporte para la ejecución, lectura ni gestión de procedimientos almacenados (*stored procedures*).
* **Concurrencia en SQLite:** El motor SQLite está limitado arquitectónicamente, por lo que **no soporta múltiples conexiones concurrentes** de escritura, bloqueando el archivo de base de datos durante transacciones simultáneas.
* **Seguridad y Credenciales:** No se dispone de un mecanismo de cifrado de contraseñas actualmente en los archivos de configuración local; las credenciales se manejan en texto plano dentro del entorno de desarrollo.

## 3. Limitaciones de Exportación

* **Exportación a Excel:** La funcionalidad de exportación a formato Excel (`.xlsx`) no se encuentra implementada en el ciclo actual del sistema. Únicamente están disponibles las exportaciones a CSV y JSON.
