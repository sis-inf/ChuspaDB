# API - Proyecto Estante

## Descripción
Este documento describe las clases principales del proyecto y sus métodos.

## Clase: ConnectionProvider
Encargada de establecer la conexión con la base de datos PostgreSQL.

### Métodos:

- *ConnectionProvider()*
Constructor de la clase.

- *connect()*
Establece la conexión con la base de datos.

- *executeQuery(String query)*
Ejecuta consultas SQL.

- *closeConnection()*
Cierra la conexión con la base de datos.

---

## Clase: EstanteService
Encargada de la lógica del sistema.

### Métodos:

- *getAll()*
Obtiene todos los registros.

- *save()*
Guarda un nuevo registro.

- *delete()*
Elimina un registro.

---

## Notas
Las clases pueden variar según la implementación final del proyecto.
