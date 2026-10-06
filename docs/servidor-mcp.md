# Servidor MCP para asistentes de IA

> **No implementado.** Esta funcionalidad está planificada pero aún no existe en el código del proyecto. Ver [roadmap.md](roadmap.md).

## Descripción

El servidor MCP (Model Context Protocol) permitirá que asistentes de inteligencia artificial interactúen con el proyecto mediante una interfaz segura. Por diseño, esta funcionalidad estará desactivada por defecto para evitar accesos no deseados.

## Características previstas

- Desactivado por defecto.
- Expondrá únicamente operaciones de lectura.
- No modificará archivos ni datos del proyecto.
- Podrá habilitarse de forma consciente cuando sea necesario.

## Operaciones previstas

Cuando el servidor esté implementado y habilitado, permitirá consultar información del proyecto mediante operaciones de solo lectura, como:

- Lectura de archivos.
- Consulta de documentación.
- Exploración de la estructura del proyecto.

No permitirá realizar operaciones de escritura.

## Seguridad

El servidor permanecerá desactivado por defecto como medida de seguridad, para reducir el riesgo de accesos accidentales.