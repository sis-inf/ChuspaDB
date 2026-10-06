# Cifrado de contraseñas de conexión

## Descripción

Actualmente el proyecto **no cifra** las contraseñas de conexión. `RepositorioConexionesJSON` serializa cada `Conexion` con Jackson tal cual, por lo que el campo `password` queda guardado en **texto plano** dentro de `conexiones.json` (en `<user.home>/.estante/`).

Este estado es consistente con lo indicado en `docs/limitaciones.md`.

## Estado del cifrado

El cifrado de contraseñas no está implementado. Figura como **Planificado** en `docs/roadmap.md`.

La idea prevista es cifrar el password con AES antes de persistirlo, usando una clave derivada de un secreto del usuario o del entorno. Nada de esto existe todavía en el código.

## ¿Qué protege?

Por ahora, nada: cualquier persona con acceso de lectura a `conexiones.json` puede ver las contraseñas guardadas.

## Limitaciones

Mientras no exista el cifrado:

- Las contraseñas se guardan en disco en texto plano.
- La única protección es el permiso de lectura del archivo en el sistema operativo.
- No se deben compartir ni versionar copias de `conexiones.json`.

## Recomendaciones

- Utilizar claves robustas.
- Mantener las claves fuera del código fuente.
- Rotar periódicamente los secretos cuando sea necesario.
- Restringir el acceso a la configuración sensible.
