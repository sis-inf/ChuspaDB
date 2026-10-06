# Guía de Exportación de Datos

## Introducción

Estante dispone de mecanismos de exportación de resultados de consultas. Actualmente la aplicación soporta los formatos CSV y JSON para el intercambio y análisis de datos.

---

## Tabla comparativa de formatos

| Formato | Caso de uso                                                                | Limitaciones                                            |
| ------- | -------------------------------------------------------------------------- | ------------------------------------------------------- |
| CSV     | Intercambio de datos entre aplicaciones y hojas de cálculo.                | No conserva tipos de datos avanzados ni formato visual. |
| JSON    | Integración con aplicaciones y servicios que consumen datos estructurados. | Puede resultar menos legible para usuarios no técnicos. |
| Excel | No disponible actualmente en la aplicación. | La exportación a Excel no está implementada. |

---

# Exportar a CSV

## Cómo exportar

1. Ejecutar una consulta de lectura.
2. Seleccionar la opción de exportación.
3. Elegir la ubicación donde se guardará el archivo.
4. Confirmar la exportación.

## Formato del archivo

La primera fila contiene los nombres de las columnas obtenidas por la consulta.

Ejemplo:

```csv
id,nombre,edad
1,Ana,21
2,Carlos,25
3,María,22
```

## Separador utilizado

El formato CSV utiliza la coma (`,`) como separador de columnas.

Cuando un valor contiene comas, comillas o saltos de línea, el contenido se encapsula entre comillas para mantener la integridad de los datos.

Ejemplo:

```csv
id,descripcion
1,"Texto con, coma"
2,"Texto ""entre comillas"""
```

---

# Exportar a JSON

## Cómo exportar

1. Ejecutar una consulta de lectura.
2. Seleccionar la opción de exportación.
3. Elegir la ubicación del archivo.
4. Confirmar la operación.

## Estructura del archivo JSON

Los datos se exportan como un arreglo JSON donde cada fila del resultado corresponde a un objeto.

Ejemplo:

```json
[
  {
    "id": 1,
    "nombre": "Ana",
    "activo": true
  },
  {
    "id": 2,
    "nombre": "Carlos",
    "activo": false
  }
]
```

### Características

* Los números se exportan como valores numéricos.
* Los valores booleanos se exportan como `true` o `false`.
* Los valores nulos se exportan como `null`.
* El archivo utiliza codificación UTF-8.

---

# Exportar a Excel

Actualmente la exportación a Excel no está implementada en el sistema.
La aplicación no genera archivos Excel. La exportación disponible corresponde únicamente a los formatos CSV y JSON.

---

# Importar desde CSV

Para garantizar una importación correcta, se recomienda utilizar archivos CSV con la siguiente estructura:

```csv
id,nombre,correo
1,Ana,ana@mail.com
2,Luis,luis@mail.com
```

## Validaciones recomendadas:

* Mantener la misma cantidad de columnas en todas las filas.
* Utilizar codificación UTF-8.
* No dejar encabezados vacíos.
* Evitar filas incompletas.

Actualmente la importación de datos soporta archivos CSV.
Los archivos deben mantener una estructura con encabezados y columnas consistentes.


## Límites

No se define un tamaño máximo fijo para el ResultSet a exportar.

El tamaño recomendado dependerá de la memoria disponible y de la cantidad de filas retornadas por la consulta.

Para conjuntos de datos muy grandes se recomienda aplicar filtros, paginación o exportaciones parciales para evitar un consumo excesivo de recursos.
---

# Recomendaciones

* Utilizar CSV para intercambio simple de información.
* Utilizar JSON para integraciones y procesamiento automático de datos.
* Verificar los datos antes de compartir archivos exportados.
