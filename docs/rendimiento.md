# Consideraciones de Rendimiento para Queries Grandes

## Introducción

Las consultas que recuperan grandes volúmenes de información pueden afectar el rendimiento de la aplicación y aumentar el consumo de memoria. Este documento describe las consideraciones actuales de rendimiento de ChuspaDB y algunas recomendaciones para trabajar con conjuntos de datos extensos.

---

## Límites recomendados

Actualmente ChuspaDB no impone un límite máximo de filas sobre los resultados obtenidos desde la base de datos.

Los resultados recuperados son almacenados en memoria y posteriormente mostrados en la interfaz mediante una tabla de resultados.

Debido a ello, se recomienda evitar consultas que recuperen cantidades excesivas de registros en una sola operación.

Ejemplo recomendado:

```sql
SELECT *
FROM clientes
LIMIT 100;
```

Ejemplo no recomendado:

```sql
SELECT *
FROM clientes;
```

si la tabla contiene cientos de miles o millones de registros.

---

## LIMIT automático

Actualmente ChuspaDB no agrega cláusulas `LIMIT` de manera automática.

Las consultas SQL son ejecutadas exactamente como fueron escritas por el usuario.

Por este motivo, es responsabilidad del usuario controlar la cantidad de datos recuperados cuando trabaja con tablas de gran tamaño.

Ejemplo:

```sql
SELECT *
FROM productos
LIMIT 50;
```

---

## Ejecución async

La ejecución de consultas utiliza el componente `EjecutorQueryAsync`, que ejecuta las operaciones en un hilo de fondo utilizando `JavaFX Task`.

Este enfoque permite que la interfaz gráfica continúe respondiendo mientras una consulta se encuentra en ejecución.

Beneficios:

* La ventana principal permanece operativa.
* La interfaz no se bloquea durante consultas prolongadas.
* Los resultados son entregados a la interfaz una vez finalizada la ejecución.

Sin embargo, la ejecución asíncrona no reduce la cantidad de memoria utilizada por los resultados obtenidos.

---

## Uso de memoria

Los resultados de las consultas son almacenados en memoria antes de mostrarse en la interfaz.

El consumo de memoria depende de varios factores:

* Cantidad de filas recuperadas.
* Cantidad de columnas por fila.
* Tipo de datos almacenados.
* Longitud de los valores de texto.

Debido a que todos los registros recuperados son cargados en memoria, consultas con decenas o cientos de miles de filas pueden incrementar significativamente el uso de RAM de la aplicación.

Según la implementación actual revisada, los resultados son cargados completamente en memoria antes de mostrarse en la interfaz.

No se identificó un mecanismo de paginación o carga parcial de resultados en los componentes analizados.

---

## Recomendaciones

Para mejorar el rendimiento al trabajar con tablas grandes se recomienda:

* Utilizar `LIMIT` en consultas exploratorias.
* Evitar `SELECT *` cuando no sea necesario.
* Recuperar únicamente las columnas requeridas.
* Utilizar filtros mediante cláusulas `WHERE`.
* Dividir consultas muy grandes en consultas más pequeñas.
* Considerar la exportación de resultados cuando el conjunto de datos sea demasiado grande para analizarlo cómodamente en la interfaz.
* Verificar el tamaño esperado del conjunto de resultados antes de ejecutar consultas complejas.

Ejemplo recomendado:

```sql
SELECT id, nombre, correo
FROM clientes
WHERE estado = 'ACTIVO'
LIMIT 100;
```

Este enfoque reduce la cantidad de datos transferidos, almacenados y mostrados por la aplicación.
