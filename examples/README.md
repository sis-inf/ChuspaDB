# Ejemplos de configuración y datos de prueba

Esta carpeta contiene archivos de ejemplo para probar la aplicación rápidamente sin configurar manualmente una base de datos.

## Crear la base SQLite de prueba

Ejecutar el siguiente comando desde la raíz del proyecto:

```bash
sqlite3 prueba.db < examples/datos-prueba.sql
```

Esto creará un archivo `prueba.db` con tablas y datos de ejemplo.

## Usar el archivo de conexiones de ejemplo

Copiar el archivo:

```bash
examples/conexiones-ejemplo.json
```

a:

```bash
~/.ChuspaDB/conexiones.json
```

Luego iniciar la aplicación normalmente.

## Ajustar la ruta de SQLite

Si `prueba.db` se encuentra en otra ubicación, modificar el valor del campo:

```json
"archivo": "prueba.db"
```

en el archivo `conexiones.json`.

## Nota sobre MySQL

La conexión MySQL incluida es solo un ejemplo de configuración local.

Es posible que necesite ajustar:

- usuario
- password
- nombre de la base de datos
- puerto

según su entorno local.
