# Guía para agregar un nuevo exportador

## Introducción

ChuspaDB permite exportar los resultados de consultas a distintos formatos. Actualmente cuenta con exportadores para CSV y JSON, y su diseño permite incorporar nuevos formatos -como Excel, SQL o Parquet- siguiendo una estructura común.

Esta guía explica el contrato que debe cumplir un nuevo explorador y cómo integrarlo con la interfaz de usuario.

---

# Flujo general

El proceso de exportación sigue este flujo:

```
Consulta SQL
        │
        ▼
Resultado de la consulta
        │
        ▼
Exportador seleccionado
        │
        ▼
Archivo generado
```

---

# Contrato de entrada

Todo exportador recibe como entrada:

- Los datos obtenidos de la consulta.
- El nombre o ruta del archivo destino.
- La configuración necesaria para escribir el formato correspondiente.

El exportador debe asumir que los datos ya fueron validados por la aplicación.

---

# Contrato de salida

Al finalizar la exportación el componente debe:

- Crear correctamente el archivo solicitado.
- Escribir todos los registros.
- Mantener el formato esperado.
- Informar cualquier error ocurrido durante la escritura.

---

# Estructura recomendada

Los exportadores existentes (CSV y JSON) sirven como referencia para implementar uno nuevo.

La estructura general consiste en:

1. Recibir los datos.
2. Procesarlos según el formato.
3. Escribir el archivo.
4. Notificar éxito o error.

---

# Integración con la interfaz

Para que el nuevo formato aparezca en la interfaz debe conectarse con el controlador:

```
PanelResultadoQueryController
```

Este controlador es el encargado de:

- Mostrar las opciones de exportación.
- Invocar el exportador seleccionado.
- Informar al usuario si la operación fue exitosa.

Al agregar un nuevo exportador también debe registrarse dentro del flujo utilizado por este controlador.

---

# Ejemplo de flujo

```
Usuario
   │
   ▼
PanelResultadoQueryController
   │
   ▼
Nuevo exportador
   │
   ▼
Archivo generado
```

---

# Buenas prácticas

- Mantener la misma interfaz utilizada por los exportadores existentes.
- Validar la ruta del archivo antes de escribir.
- Informar errores mediante excepciones o mensajes claros.
- Evitar duplicar lógica presente en otros exportadores.

---

# Referencias

Se recomienda revisar como ejemplo los exportadores existentes:

- ExportadorCSV
- ExportadorJSON

Estos componentes muestran el patrón recomendado para implementar nuevos formatos.
