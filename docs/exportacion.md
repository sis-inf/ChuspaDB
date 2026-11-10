# Guía de Exportación de Datos

## Introducción

ChuspaDB dispone de mecanismos de exportación de resultados de consultas. Actualmente la aplicación soporta los formatos CSV, JSON y Excel (`.xlsx`) para el intercambio y análisis de datos.

---

## Tabla comparativa de formatos

| Formato | Caso de uso                                                                | Limitaciones                                            |
| ------- | -------------------------------------------------------------------------- | ------------------------------------------------------- |
| CSV     | Intercambio de datos entre aplicaciones y hojas de cálculo.                | No conserva tipos de datos avanzados ni formato visual. |
| JSON    | Integración con aplicaciones y servicios que consumen datos estructurados. | Puede resultar menos legible para usuarios no técnicos. |
| Excel | Análisis y presentación en hojas de cálculo. | No es un formato de texto plano; requiere un programa compatible con `.xlsx`. |

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

El separador es fijo: la aplicación siempre utiliza la coma y no ofrece una opción para cambiarlo. El archivo se guarda con codificación UTF-8 (sin BOM) y saltos de línea `\n`.

Si al abrir el archivo en Excel los acentos no se ven correctamente, impórtelo desde **Datos → Desde texto/CSV** y seleccione la codificación UTF-8.

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
* Si el archivo de destino ya existe, se sobrescribe.

---

# Exportar a Excel

## Cómo exportar

1. Ejecutar una consulta de lectura.
2. Abrir el menú de exportación y elegir **Exportar a Excel...**.
3. Elegir la ubicación y el nombre del archivo (extensión `.xlsx`).
4. Confirmar la exportación.

## Formato del archivo

* El archivo contiene una sola hoja llamada `Resultado`.
* La primera fila contiene los nombres de las columnas.
* Los números se guardan como valores numéricos y los booleanos como valores lógicos.
* Los demás valores se guardan como texto.
* El ancho de las columnas se ajusta automáticamente al contenido.

Ejemplo del contenido de la hoja:

| id | nombre | edad |
| -- | ------ | ---- |
| 1  | Ana    | 21   |
| 2  | Carlos |      |

---

# Valores nulos y consultas vacías

## Valores nulos

| Formato | Representación de un valor nulo |
| ------- | ------------------------------- |
| CSV     | Celda vacía entre separadores (por ejemplo `2,Carlos,`) |
| JSON    | `null` |
| Excel   | Celda en blanco |

Ejemplo en CSV:

```csv
id,nombre,edad
1,Ana,21
2,Carlos,
```

Ejemplo en JSON:

```json
[
  {
    "id": 1,
    "nombre": "Ana",
    "edad": 21
  },
  {
    "id": 2,
    "nombre": "Carlos",
    "edad": null
  }
]
```

## Consultas vacías

Cuando la consulta no devuelve filas:

* **CSV:** el archivo contiene únicamente la fila de encabezados.
* **Excel:** la hoja `Resultado` contiene únicamente la fila de encabezados.
* **JSON:** el archivo contiene un arreglo vacío, con los corchetes en líneas separadas:

```json
[
]
```

Ejemplo en CSV:

```csv
id,nombre,edad
```

## Resultados que no se pueden exportar

Solo se exportan resultados de consultas de lectura. Si el resultado corresponde a otro tipo de operación, la aplicación muestra el mensaje «Solo se pueden exportar resultados de lectura».

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
