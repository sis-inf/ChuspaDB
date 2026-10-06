# Verificación de contraseñas comprometidas

## Descripción

La verificación de contraseñas comprometidas permite comprobar si una contraseña ha aparecido en filtraciones de datos conocidas utilizando el servicio Have I Been Pwned (HIBP).

El mecanismo protege la privacidad mediante el uso del modelo de k-anonimato, evitando enviar la contraseña completa al servicio externo.

## ¿Cómo funciona?

El proceso sigue estos pasos:

1. Se calcula el hash SHA-1 de la contraseña.
2. Solo se envían al servicio los primeros caracteres del hash.
3. El servicio responde con una lista de coincidencias posibles.
4. La comparación final se realiza localmente.
5. La contraseña completa nunca abandona la aplicación.

## ¿Qué es el k-anonimato?

El k-anonimato es una técnica que protege la privacidad enviando únicamente un prefijo del hash de la contraseña.

De esta forma:

- La contraseña nunca se transmite.
- El servicio externo no puede conocer la contraseña original.
- La verificación se realiza de manera segura.

## Activar la verificación

La verificación puede habilitarse mediante la configuración correspondiente del proyecto.

Cuando está activada:

- Se consulta el servicio HIBP.
- Se advierte si la contraseña aparece en una filtración conocida.

## Desactivar la verificación

Si no se desea realizar consultas externas, la funcionalidad puede deshabilitarse desde la configuración.

En ese caso:

- No se realizan consultas a HIBP.
- No se verifica si la contraseña fue comprometida.

## Consideraciones

- La contraseña completa nunca se envía al servicio externo.
- Solo se comparte un prefijo del hash.
- El mecanismo protege la privacidad del usuario durante la consulta.
