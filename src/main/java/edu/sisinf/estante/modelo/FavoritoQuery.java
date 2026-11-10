package edu.sisinf.chuspadb.modelo;

import java.time.LocalDateTime;

/**
 * Representa una consulta favorita guardada por el usuario.
 * Estructura de tipo record para garantizar la inmutabilidad de los datos.
 */
public record FavoritoQuery(
    String nombre,
    String sql,
    String motor,
    LocalDateTime fechaCreacion
) {
    public FavoritoQuery {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("La consulta SQL no puede estar vacía");
        }
        if (motor == null || motor.isBlank()) {
            throw new IllegalArgumentException("El motor de base de datos no puede estar vacío");
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
